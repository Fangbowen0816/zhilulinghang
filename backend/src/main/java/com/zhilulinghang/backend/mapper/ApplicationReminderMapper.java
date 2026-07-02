package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.ApplicationReminder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ApplicationReminderMapper {
    String COLUMNS = "id, application_id, student_id, remind_type, remind_time, content, status, create_time, update_time";
    String DETAIL_COLUMNS = "ar.id, ar.application_id, ar.student_id, ar.remind_type, ar.remind_time, ar.content, ar.status, ar.create_time, ar.update_time, j.title AS job_title, j.company";

    @Select("SELECT " + COLUMNS + " FROM application_reminder WHERE application_id = #{applicationId} ORDER BY remind_time ASC")
    List<ApplicationReminder> findByApplicationId(Long applicationId);

    @Select("SELECT " + DETAIL_COLUMNS + " FROM application_reminder ar " +
            "JOIN job_application ja ON ar.application_id = ja.id " +
            "JOIN job j ON ja.job_id = j.id " +
            "WHERE ar.student_id = #{studentId} AND ar.status = 'PENDING' " +
            "ORDER BY ar.remind_time ASC")
    List<ApplicationReminder> findPendingByStudentId(Long studentId);

    @Select("SELECT " + COLUMNS + " FROM application_reminder WHERE id = #{id}")
    ApplicationReminder findById(Long id);

    @Insert("INSERT INTO application_reminder(application_id, student_id, remind_type, remind_time, content, status) VALUES(#{applicationId}, #{studentId}, #{remindType}, #{remindTime}, #{content}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ApplicationReminder reminder);

    @Update("UPDATE application_reminder SET status = 'DONE', update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int markDone(Long id);

    @Delete("DELETE FROM application_reminder WHERE id = #{id}")
    int deleteById(Long id);
}
