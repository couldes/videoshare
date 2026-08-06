package com.videoshare.web.controller;

import com.videoshare.common.entity.CommentInfo;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.ResponseVO;
import com.videoshare.web.service.CommentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 评论内部接口（仅供 admin 服务间调用，Gateway 已拦截 /innerApi 外部访问）
 * 不加 @RequireLogin，允许无 token 的服务间调用。
 * 与用户端 /comment/** 不同：列表支持全局查询（videoId/status 可选），审核/删除不校验评论归属。
 */
@RestController
@RequestMapping("/innerApi/comment")
public class CommentInnerApiController {

    @Resource
    private CommentService commentService;

    /** 评论列表（admin 用）：videoId/status 可选，仅顶级评论，分页 */
    @GetMapping("/list")
    public ResponseVO<PaginationResultVO<CommentInfo>> getCommentList(
            @RequestParam(required = false) String videoId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        return ResponseVO.success(commentService.getCommentListForAdmin(
                videoId, pageNum, pageSize, status));
    }

    /** 审核评论（admin 用）：status=1 通过 / status=2 删除 */
    @PostMapping("/updateStatus")
    public ResponseVO<Void> updateStatus(@RequestParam Long commentId,
                                         @RequestParam Integer status) {
        commentService.updateCommentStatusForAdmin(commentId, status);
        return ResponseVO.success();
    }

    /** 强制删除评论（admin 用，不校验归属） */
    @PostMapping("/delete")
    public ResponseVO<Void> delete(@RequestParam Long commentId) {
        commentService.deleteCommentForAdmin(commentId);
        return ResponseVO.success();
    }
}
