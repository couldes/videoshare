// 路径: web/src/main/java/com/videoshare/web/service/impl/CommentServiceImpl.java
package com.videoshare.web.service.impl;

import com.videoshare.common.entity.CommentInfo;
import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.query.CommentQuery;
import com.videoshare.common.vo.CommentVO;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.web.mapper.CommentMapper;
import com.videoshare.web.mapper.UserActionMapper;
import com.videoshare.web.mapper.UserInfoMapper;
import com.videoshare.web.mapper.VideoInfoMapper;
import com.videoshare.web.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource private CommentMapper    commentMapper;
    @Resource private VideoInfoMapper  videoInfoMapper;
    @Resource private UserInfoMapper   userInfoMapper;
    @Resource private UserActionMapper userActionMapper;

    // ============================================================
    //  评论列表（顶级评论分页，每条顶级携带最多3条回复）
    // ============================================================
    @Override
    public PaginationResultVO<CommentVO> getCommentList(String videoId,
                                                        Integer pageNum,
                                                        Integer pageSize) {
        // 1. 查顶级评论（pCommentId = 0）
        CommentQuery query = new CommentQuery();
        query.setVideoId(videoId);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setStatus(1); // 只查已通过的

        List<CommentInfo> topList = commentMapper.selectTopComments(query);
        Integer total = commentMapper.countTopComments(videoId, 1);

        if (topList.isEmpty()) {
            return new PaginationResultVO<>(total, pageSize, pageNum, Collections.emptyList());
        }

        // 2. 批量收集需要查的用户ID
        Set<String> userIds = new HashSet<>();
        for (CommentInfo c : topList) {
            userIds.add(c.getUserId());
            if (c.getReplyUserId() != null) userIds.add(c.getReplyUserId());
        }

        // 3. 批量查每条顶级评论的回复（最多3条）
        Map<Long, List<CommentInfo>> repliesMap = new HashMap<>();
        for (CommentInfo top : topList) {
            List<CommentInfo> replies = commentMapper.selectReplies(top.getCommentId(), 1);
            // 收集回复里的用户ID
            replies.forEach(r -> {
                userIds.add(r.getUserId());
                if (r.getReplyUserId() != null) userIds.add(r.getReplyUserId());
            });
            repliesMap.put(top.getCommentId(), replies);
        }

        // 4. 批量查用户信息（一次查询）
        Map<String, UserInfo> userMap = batchGetUsers(userIds);

        // 5. 组装 VO
        List<CommentVO> voList = topList.stream()
                .map(top -> {
                    CommentVO vo = toVO(top, userMap);
                    List<CommentInfo> replyList = repliesMap.getOrDefault(top.getCommentId(),
                            Collections.emptyList());
                    // 最多展示3条回复，防止接口数据过大
                    List<CommentVO> replyVoList = replyList.stream()
                            .limit(3)
                            .map(r -> toVO(r, userMap))
                            .collect(Collectors.toList());
                    vo.setReplies(replyVoList);
                    return vo;
                })
                .collect(Collectors.toList());

        return new PaginationResultVO<>(total, pageSize, pageNum, voList);
    }

    // ============================================================
    //  发布评论（含回复）
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO postComment(String userId, String videoId, String content,
                                 Long pCommentId, String replyUserId) {
        // 1. 校验内容
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new BusinessException("评论内容不能超过500字");
        }

        // 2. 如果是回复，校验父评论存在
        if (pCommentId != null && pCommentId > 0) {
            CommentInfo parent = commentMapper.selectByCommentId(pCommentId);
            if (parent == null || parent.getStatus() != 1) {
                throw new BusinessException("被回复的评论不存在");
            }
        }

        // 3. 构建并插入评论
        CommentInfo comment = new CommentInfo();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setPCommentId(pCommentId != null ? pCommentId : 0L);
        comment.setReplyUserId(replyUserId);
        comment.setContent(content.trim());
        comment.setLikeCount(0);
        comment.setTopType(0);
        comment.setStatus(1); // 简化：直接通过审核
        commentMapper.insert(comment);

        // 4. 更新视频评论数（只有顶级评论才+1，回复不计入）
        if (pCommentId == null || pCommentId == 0) {
            videoInfoMapper.updateCommentCount(videoId, 1);
        }

        // 5. 返回刚插入的评论VO（含昵称，供前端立即展示）
        UserInfo user = userInfoMapper.selectByUserId(userId);
        CommentVO vo = toVO(comment, Collections.singletonMap(userId, user));
        // 如果是回复，填充被回复者昵称
        if (replyUserId != null) {
            UserInfo replyUser = userInfoMapper.selectByUserId(replyUserId);
            if (replyUser != null) {
                vo.setReplyNickName(replyUser.getNickName());
            }
        }
        return vo;
    }

    // ============================================================
    //  点赞评论（Toggle）
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(String userId, Long commentId) {
        // actionType=3 表示点赞评论
        Integer exists = userActionMapper.checkAction(userId, String.valueOf(commentId), 3);
        boolean willAdd = (exists == 0);

        if (willAdd) {
            userActionMapper.insert(userId, String.valueOf(commentId), 3);
            commentMapper.updateLikeCount(commentId, 1);
        } else {
            userActionMapper.delete(userId, String.valueOf(commentId), 3);
            commentMapper.updateLikeCount(commentId, -1);
        }
        return willAdd;
    }

    // ============================================================
    //  删除评论（逻辑删除，只允许本人操作）
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, String userId) {
        CommentInfo comment = commentMapper.selectByCommentId(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人评论");
        }
        // status=2 表示逻辑删除
        commentMapper.updateStatus(commentId, 2);

        // 顶级评论删除，视频评论数-1
        if (comment.getPCommentId() == 0) {
            videoInfoMapper.updateCommentCount(comment.getVideoId(), -1);
        }
    }

    // ============================================================
    //  私有工具方法
    // ============================================================

    /** 批量查询用户信息 */
    private Map<String, UserInfo> batchGetUsers(Set<String> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        return userIds.stream()
                .filter(Objects::nonNull)
                .map(userInfoMapper::selectByUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserInfo::getUserId, u -> u, (a, b) -> a));
    }

    /** CommentInfo → CommentVO */
    private CommentVO toVO(CommentInfo c, Map<String, UserInfo> userMap) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(c.getCommentId());
        vo.setVideoId(c.getVideoId());
        vo.setUserId(c.getUserId());
        vo.setPCommentId(c.getPCommentId());
        vo.setReplyUserId(c.getReplyUserId());
        vo.setContent(c.getContent());
        vo.setLikeCount(c.getLikeCount());
        vo.setTopType(c.getTopType());
        vo.setStatus(c.getStatus());
        vo.setCreateTime(c.getCreateTime());

        // 填充评论者昵称
        UserInfo author = userMap.get(c.getUserId());
        if (author != null) vo.setNickName(author.getNickName());

        // 填充被回复者昵称
        if (c.getReplyUserId() != null) {
            UserInfo replyUser = userMap.get(c.getReplyUserId());
            if (replyUser != null) vo.setReplyNickName(replyUser.getNickName());
        }
        return vo;
    }
}