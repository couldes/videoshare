package com.videoshare.web.controller;

import com.videoshare.web.service.CommentService;
import com.videoshare.common.vo.ResponseVO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 评论接口
 *   GET  /comment/list   → 视频评论列表（分页，支持二级回复）
 *   POST /comment/post   → 发布评论（需登录）
 *   POST /comment/action → 点赞评论（需登录）
 *   DELETE /comment/{id} → 删除评论（本人或管理员）
 */
@RestController
@RequestMapping("/comment")
public class CommentController extends ABaseController {

    @Resource
    private CommentService commentService;

    /** 视频评论列表（顶级评论分页，每条顶级带最多3条回复） */
    @GetMapping("/list")
    public ResponseVO getCommentList(
            @RequestParam String  videoId,
            @RequestParam(defaultValue = "1")  Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return getSuccessResponseVO(commentService.getCommentList(videoId, pageNum, pageSize));
    }

    /** 发布评论（pCommentId=0 表示顶级评论，>0 表示回复某条评论）*/
    @PostMapping("/post")
    public ResponseVO postComment(
            @RequestParam String videoId,
            @RequestParam String content,
            @RequestParam(defaultValue = "0") Long   pCommentId,
            @RequestParam(required = false)   String replyUserId,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(
                commentService.postComment(userId, videoId, content, pCommentId, replyUserId)
        );
    }

    /** 点赞 / 取消点赞评论 */
    @PostMapping("/action")
    public ResponseVO likeComment(
            @RequestParam Long commentId,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        boolean active = commentService.toggleLike(userId, commentId);
        return getSuccessResponseVO(active);
    }

    /** 删除评论（只有本人或管理员可删）*/
    @DeleteMapping("/{commentId}")
    public ResponseVO deleteComment(
            @PathVariable Long commentId,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        commentService.deleteComment(commentId, userId);
        return getSuccessResponseVO(null);
    }
}