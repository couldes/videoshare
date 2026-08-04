package com.videoshare.web.mapper;

import com.videoshare.common.entity.TranscodeJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TranscodeJobMapper {
    Integer insert(TranscodeJob job);

    Integer updateStatus(@Param("jobId") Long jobId,
                         @Param("status") int status,
                         @Param("errorMsg") String errorMsg);

    TranscodeJob selectByVideoId(@Param("videoId") String videoId);

    /** 对账：status=0 卡死超阈值且未达重试上限的待重触发任务 */
    List<TranscodeJob> selectStalePending(@Param("staleMinutes") int staleMinutes,
                                          @Param("maxRetries") int maxRetries);

    /** 对账：status=0 且重试已达上限的任务 */
    List<TranscodeJob> selectMaxedPending(@Param("maxRetries") int maxRetries);

    /** 对账：status=1 超过超时阈值仍未完成的任务 */
    List<TranscodeJob> selectStaleProcessing(@Param("timeoutMinutes") int timeoutMinutes);

    /** 对账：条件递增重试计数并刷新最后流转时间，返回受影响行数（1=认领成功） */
    int claimPending(@Param("jobId") Long jobId, @Param("maxRetries") int maxRetries);

    /** 对账：仅当任务仍处于 expectedStatus 时置为失败(3)，返回受影响行数（0=已被流转，跳过） */
    int markFailed(@Param("jobId") Long jobId,
                   @Param("expectedStatus") int expectedStatus,
                   @Param("errorMsg") String errorMsg);
}
