package com.videoshare.web.mapper;

import com.videoshare.common.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    Integer insert(Notification notification);

    List<Notification> selectByUserId(@Param("userId") String userId,
                                      @Param("offset") Integer offset,
                                      @Param("pageSize") Integer pageSize);

    Integer countByUserId(@Param("userId") String userId);

    Integer countUnreadByUserId(@Param("userId") String userId);

    Integer markAsRead(@Param("id") Long id, @Param("userId") String userId);

    Integer markAllAsRead(@Param("userId") String userId);
}
