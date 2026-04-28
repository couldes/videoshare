// 路径: admin/src/main/java/com/videoshare/admin/controller/CommentManageController.java
package com.videoshare.admin.controller;

import com.videoshare.common.vo.ResponseVO;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.admin.mapper.CommentMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理端评论管理接口
 *   GET    /admin/comment/list        → 评论列表（指定视频或全局）
 *   POST   /admin/comment/updateStatus→ 审核通过/拒绝（status: 1=通过 2=删除）
 *   DELETE /admin/comment/{id}        → 强制删除评论
 *
 * 注意：管理端删除不校验 userId，可删任何用户评论
 */
@RestController
@RequestMapping("/admin/comment")
public class CommentManageController extends ABaseController {

    // 管理端复用 web 的 CommentMapper（操作同一张表）
    @Resource
    private CommentMapper commentMapper;

    /**
     * 按视频ID查评论列表（简化版，直接返回原始 CommentInfo）
     * GET /admin/comment/list?videoId=xxx&pageNum=1&pageSize=20&status=0
     */
    @GetMapping("/list")
    public ResponseVO<Object> getCommentList(
            @RequestParam String  videoId,
            @RequestParam(defaultValue = "1")  Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false)    Integer status) {
        try {
            com.videoshare.common.query.CommentQuery query = new com.videoshare.common.query.CommentQuery();
            query.setVideoId(videoId);
            query.setPageNum(pageNum);
            query.setPageSize(pageSize);
            query.setStatus(status); // null=全部状态
            query.setPCommentId(0L); // 只查顶级评论

            java.util.List<?> list = commentMapper.selectTopComments(query);
            Integer total = commentMapper.countTopComments(videoId, status);
            return success(new com.videoshare.common.vo.PaginationResultVO<>(total, pageSize, pageNum, list));
        } catch (Exception e) {
            return error("查询失败");
        }
    }

    /**
     * 修改评论状态（审核通过/删除）
     * POST /admin/comment/updateStatus
     * status：1=审核通过 2=删除
     */
    @PostMapping("/updateStatus")
    public ResponseVO<Void> updateStatus(
            @RequestParam Long    commentId,
            @RequestParam Integer status) {
        try {
            if (status != 1 && status != 2) {
                return error("非法状态值：1=通过 2=删除");
            }
            commentMapper.updateStatus(commentId, status);
            return success();
        } catch (Exception e) {
            return error("操作失败");
        }
    }

    /**
     * 强制删除评论（管理员权限，不校验是否本人）
     * DELETE /admin/comment/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ResponseVO<Void> deleteComment(@PathVariable Long commentId) {
        try {
            commentMapper.updateStatus(commentId, 2);
            return success();
        } catch (Exception e) {
            return error("删除失败");
        }
    }
}