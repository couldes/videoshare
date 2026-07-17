package com.videoshare.web.controller;

import com.videoshare.common.vo.ResponseVO;
import com.videoshare.web.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController extends ABaseController {

    @Resource
    private AnalyticsService analyticsService;

    @GetMapping("/overview")
    public ResponseVO getOverview(
            @RequestParam(defaultValue = "30") Integer days,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(analyticsService.getOverview(userId, days));
    }

    @GetMapping("/videos")
    public ResponseVO getVideoStats(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(analyticsService.getVideoStats(userId, pageNum, pageSize));
    }

    @GetMapping("/audience")
    public ResponseVO getAudience(HttpServletRequest request) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(analyticsService.getAudience(userId));
    }
}
