package com.videoshare.admin.service.impl;

import com.videoshare.admin.mapper.CommentMapper;
import com.videoshare.admin.service.AdminCommentService;
import com.videoshare.common.entity.CommentInfo;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.query.CommentQuery;
import com.videoshare.common.vo.PaginationResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AdminCommentServiceImpl implements AdminCommentService {

    @Resource
    private CommentMapper commentMapper;

    @Override
    public PaginationResultVO<CommentInfo> getCommentList(String videoId, Integer pageNum,
                                                           Integer pageSize, Integer status) {
        CommentQuery query = new CommentQuery();
        query.setVideoId(videoId);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setStatus(status);
        query.setPCommentId(0L);

        List<CommentInfo> list = commentMapper.selectTopComments(query);
        Integer total = commentMapper.countTopComments(videoId, status);
        return new PaginationResultVO<>(total, pageSize, pageNum, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommentStatus(Long commentId, Integer status) {
        if (status != 1 && status != 2) {
            throw new BusinessException("非法状态值：1=通过 2=删除");
        }
        Integer rows = commentMapper.updateStatus(commentId, status);
        if (rows == 0) throw new BusinessException("评论不存在");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        CommentInfo comment = commentMapper.selectByCommentId(commentId);
        if (comment == null) throw new BusinessException("评论不存在");
        Integer rows = commentMapper.updateStatus(commentId, 2);
        if (rows == 0) throw new BusinessException("删除失败");
    }
}
