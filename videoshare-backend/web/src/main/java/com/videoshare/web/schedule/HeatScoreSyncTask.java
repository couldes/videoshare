package com.videoshare.web.schedule;

import com.videoshare.web.mapper.VideoInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class HeatScoreSyncTask {

    private static final Logger log = LoggerFactory.getLogger(HeatScoreSyncTask.class);

    @Resource
    private VideoInfoMapper videoInfoMapper;

    /** 每 10 分钟全量重算所有已发布视频的热度分 */
    @Scheduled(fixedRate = 600000)
    public void syncAllHeat() {
        try {
            int updated = videoInfoMapper.updateAllHeat();
            log.info("Heat score sync completed, {} videos updated", updated);
        } catch (Exception e) {
            log.error("Heat score sync failed", e);
        }
    }
}
