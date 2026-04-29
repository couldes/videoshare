package com.videoshare.admin.controller;

import com.videoshare.common.vo.ResponseVO;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.admin.service.AdminCommentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理端评论管理接口
 *   GET    /admin/comment/list        → 评论列表（指定视频或全局）
 *   POST   /admin/comment/updateStatus→ 审核通过/拒绝（status: 1=通过 2=删除）
 *   DELETE /admin/comment/{id}        → 强制删除评论
 */
@RestController
@RequestMapping("/admin/comment")
public class CommentManageController extends ABaseController {

    @Resource
    private AdminCommentService adminCommentService;

    @GetMapping("/list")
    public ResponseVO<Object> getCommentList(
            @RequestParam(required = false) String  videoId,
            @RequestParam(defaultValue = "1")  Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false)    Integer status) {
        try {
            return success(adminCommentService.getCommentList(videoId, pageNum, pageSize, status));
        } catch (Exception e) {
            return error("查询失败");
        }
    }

    @PostMapping("/updateStatus")
    public ResponseVO<Void> updateStatus(
            @RequestParam Long    commentId,
            @RequestParam Integer status) {
        try {
            adminCommentService.updateCommentStatus(commentId, status);
            return success();
        } catch (BusinessException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("操作失败");
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseVO<Void> deleteComment(@PathVariable Long commentId) {
        try {
            adminCommentService.deleteComment(commentId);
            return success();
        } catch (BusinessException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除失败");
        }
    }
}
