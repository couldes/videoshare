package com.videoshare.web.controller;

import com.videoshare.common.vo.ResponseVO;
import com.videoshare.web.service.WatchHistoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserHistoryController extends ABaseController {

    @Resource
    private WatchHistoryService watchHistoryService;

    /** 分页查询观看历史 */
    @GetMapping("/history")
    public ResponseVO getHistory(HttpServletRequest request,
                                  @RequestParam(defaultValue = "1")  Integer pageNum,
                                  @RequestParam(defaultValue = "20") Integer pageSize) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(watchHistoryService.getHistory(userId, pageNum, pageSize));
    }

    /** 搜索观看历史 */
    @GetMapping("/history/search")
    public ResponseVO searchHistory(HttpServletRequest request,
                                     @RequestParam String keyword,
                                     @RequestParam(defaultValue = "1")  Integer pageNum,
                                     @RequestParam(defaultValue = "20") Integer pageSize) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(watchHistoryService.searchHistory(userId, keyword, pageNum, pageSize));
    }

    /** 清空观看历史 */
    @DeleteMapping("/history")
    public ResponseVO clearHistory(HttpServletRequest request) {
        String userId = requireLogin(request);
        watchHistoryService.clearAll(userId);
        return getSuccessResponseVO(null);
    }

    /** 删除单条观看记录 */
    @DeleteMapping("/history/{videoId}")
    public ResponseVO deleteRecord(HttpServletRequest request,
                                    @PathVariable String videoId) {
        String userId = requireLogin(request);
        watchHistoryService.deleteRecord(userId, videoId);
        return getSuccessResponseVO(null);
    }
}
