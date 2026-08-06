package com.videoshare.admin.client;

import com.videoshare.common.entity.CommentInfo;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.ResponseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * admin → web 评论内部接口 Feign 客户端（经 Nacos 服务发现按服务名 web 解析实例）
 * 对应 web 侧 CommentInnerApiController（/innerApi/comment/**，Gateway 已拦截外部访问）
 */
@FeignClient(name = "web")
public interface CommentInnerApiClient {

    /** 评论列表（admin 用）：videoId/status 可选 */
    @GetMapping("/innerApi/comment/list")
    ResponseVO<PaginationResultVO<CommentInfo>> getCommentList(
            @RequestParam("videoId") String videoId,
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam("status") Integer status);

    /** 审核评论：status=1 通过 / status=2 删除 */
    @PostMapping("/innerApi/comment/updateStatus")
    ResponseVO<Void> updateStatus(@RequestParam("commentId") Long commentId,
                                  @RequestParam("status") Integer status);

    /** 强制删除评论 */
    @PostMapping("/innerApi/comment/delete")
    ResponseVO<Void> delete(@RequestParam("commentId") Long commentId);
}
