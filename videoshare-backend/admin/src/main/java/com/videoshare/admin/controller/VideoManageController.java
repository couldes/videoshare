
package com.videoshare.admin.controller;

import com.videoshare.admin.service.AdminVideoService;
import com.videoshare.common.vo.ResponseVO;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.query.VideoQuery;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理端视频管理接口（全部需要 admin token，拦截器统一验证）
 *   GET    /admin/video/list              → 视频列表（分页+多条件）
 *   GET    /admin/video/{videoId}          → 视频详情
 *   POST   /admin/video/updateStatus      → 上架/下架
 *   DELETE /admin/video/delete/{videoId}  → 删除视频
 *   GET    /admin/video/stats             → 视频统计（Dashboard 扩展）
 */
@RestController
@RequestMapping("/admin/video")
public class VideoManageController extends ABaseController {

    @Resource
    private AdminVideoService adminVideoService;

    @GetMapping("/list")
    public ResponseVO<Object> getVideoList(VideoQuery query) {
        try {
            return success(adminVideoService.getVideoList(query));
        } catch (Exception e) {
            return error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{videoId}")
    public ResponseVO<Object> getVideoDetail(@PathVariable String videoId) {
        try {
            return success(adminVideoService.getVideoDetail(videoId));
        } catch (BusinessException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("查询失败");
        }
    }

    @PostMapping("/updateStatus")
    public ResponseVO<Void> updateStatus(
            @RequestParam String  videoId,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        try {
            adminVideoService.updateVideoStatus(videoId, status, remark);
            return success();
        } catch (BusinessException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("操作失败");
        }
    }

    @DeleteMapping("/delete/{videoId}")
    public ResponseVO<Void> deleteVideo(@PathVariable String videoId) {
        try {
            adminVideoService.deleteVideo(videoId);
            return success();
        } catch (BusinessException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除失败");
        }
    }

    @GetMapping("/stats")
    public ResponseVO<Object> getVideoStats() {
        try {
            return success(adminVideoService.getVideoStats());
        } catch (Exception e) {
            return error("获取统计失败");
        }
    }

    /** 全量重建 ES 索引 */
    @PostMapping("/reindex")
    public ResponseVO<String> reindex() {
        try {
            int count = adminVideoService.reindexAll();
            return success("重建索引完成，共 " + count + " 条");
        } catch (Exception e) {
            return error("重建索引失败：" + e.getMessage());
        }
    }
}