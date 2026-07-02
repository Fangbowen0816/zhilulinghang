package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.Notification;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper {
    String COLUMNS = "id, user_id, type, title, content, read_status, related_type, related_id, create_time, read_time";

    @Insert("INSERT INTO notification(user_id, type, title, content, read_status, related_type, related_id) VALUES(#{userId}, #{type}, #{title}, #{content}, 'UNREAD', #{relatedType}, #{relatedId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    @Select("SELECT " + COLUMNS + " FROM notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND read_status = 'UNREAD'")
    int countUnread(Long userId);

    @Update("UPDATE notification SET read_status = 'READ', read_time = CURRENT_TIMESTAMP WHERE id = #{id} AND user_id = #{userId}")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notification SET read_status = 'READ', read_time = CURRENT_TIMESTAMP WHERE user_id = #{userId} AND read_status = 'UNREAD'")
    int markAllRead(Long userId);
}
