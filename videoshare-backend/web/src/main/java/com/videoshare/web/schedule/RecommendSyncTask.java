package com.videoshare.web.schedule;

import com.videoshare.web.component.RedisComponent;
import com.videoshare.web.mapper.UserActionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 协同过滤定时任务：每 30 分钟计算视频相似度并写入 Redis 缓存。
 *
 * 算法：基于用户的点赞/收藏行为，计算视频共现矩阵 + 余弦归一化。
 * 对每个视频保留 Top 20 相似视频。
 */
@Component
public class RecommendSyncTask {

    private static final Logger log = LoggerFactory.getLogger(RecommendSyncTask.class);
    private static final int TOP_N = 20;

    @Resource
    private UserActionMapper userActionMapper;

    @Resource
    private RedisComponent redisComponent;

    @Scheduled(fixedRate = 1800000)
    public void computeSimilarities() {
        long start = System.currentTimeMillis();
        try {
            // 1. 读取所有互动数据
            List<Map<String, Object>> interactions = userActionMapper.selectAllInteractions();
            if (interactions.isEmpty()) {
                log.info("RecommendSync: no interaction data, skip");
                return;
            }

            // 2. 按用户分组：Map<userId, Set<videoId>>
            Map<String, Set<String>> userVideos = new HashMap<>();
            for (Map<String, Object> row : interactions) {
                String userId = (String) row.get("user_id");
                String targetId = (String) row.get("target_id");
                userVideos.computeIfAbsent(userId, k -> new HashSet<>()).add(targetId);
            }

            // 3. 统计每个视频的交互用户数
            Map<String, Integer> videoUserCount = new HashMap<>();
            for (Set<String> videos : userVideos.values()) {
                for (String vid : videos) {
                    videoUserCount.merge(vid, 1, Integer::sum);
                }
            }

            // 4. 构建共现矩阵：Map<videoA, Map<videoB, coOccurrenceCount>>
            Map<String, Map<String, Integer>> coOccurrence = new HashMap<>();
            for (Set<String> videos : userVideos.values()) {
                List<String> list = new ArrayList<>(videos);
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    String a = list.get(i);
                    for (int j = i + 1; j < size; j++) {
                        String b = list.get(j);
                        coOccurrence.computeIfAbsent(a, k -> new HashMap<>())
                                .merge(b, 1, Integer::sum);
                        coOccurrence.computeIfAbsent(b, k -> new HashMap<>())
                                .merge(a, 1, Integer::sum);
                    }
                }
            }

            // 5. 计算相似度并写 Redis
            int videoCount = 0;
            for (Map.Entry<String, Map<String, Integer>> entry : coOccurrence.entrySet()) {
                String videoA = entry.getKey();
                Map<String, Integer> coMap = entry.getValue();
                int usersA = videoUserCount.getOrDefault(videoA, 1);

                // 计算相似度并取 TopN
                Map<String, Double> similarities = new HashMap<>();
                for (Map.Entry<String, Integer> co : coMap.entrySet()) {
                    String videoB = co.getKey();
                    int usersB = videoUserCount.getOrDefault(videoB, 1);
                    double score = co.getValue() / Math.sqrt((double) usersA * usersB);
                    similarities.put(videoB, score);
                }

                // 保留 Top N
                Map<String, Double> topN = similarities.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .limit(TOP_N)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (a, b) -> a, LinkedHashMap::new));

                redisComponent.saveVideoSimilarities(videoA, topN);
                videoCount++;
            }

            long elapsed = System.currentTimeMillis() - start;
            log.info("RecommendSync completed: {} videos processed, {} users, {}ms",
                    videoCount, userVideos.size(), elapsed);

        } catch (Exception e) {
            log.error("RecommendSync failed", e);
        }
    }
}
