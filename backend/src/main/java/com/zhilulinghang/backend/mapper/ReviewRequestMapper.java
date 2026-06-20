package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.ReviewRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ReviewRequestMapper {
    String COLUMNS = "rr.id, rr.source_resume_id, rr.student_id, rr.teacher_id, rr.assign_mode, rr.status, rr.student_message, rr.teacher_reply, rr.decline_reason, rr.decline_suggestion, rr.withdraw_reason, rr.admin_decision, rr.admin_comment, rr.admin_id, rr.admin_time, rr.create_time, rr.update_time, r.title AS source_resume_title, tp.display_name AS teacher_display_name, u.username AS student_username";
    String FROM_JOIN = " FROM review_request rr JOIN resume r ON rr.source_resume_id = r.id LEFT JOIN teacher_profile tp ON rr.teacher_id = tp.teacher_id LEFT JOIN `user` u ON rr.student_id = u.id";

    @Insert("INSERT INTO review_request(source_resume_id, student_id, teacher_id, assign_mode, status, student_message) VALUES(#{sourceResumeId}, #{studentId}, #{teacherId}, #{assignMode}, #{status}, #{studentMessage})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReviewRequest request);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " WHERE rr.id = #{id}")
    ReviewRequest findById(Long id);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " WHERE rr.student_id = #{studentId} ORDER BY rr.update_time DESC")
    List<ReviewRequest> findByStudentId(Long studentId);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " WHERE rr.teacher_id = #{teacherId} ORDER BY rr.update_time DESC")
    List<ReviewRequest> findByTeacherId(Long teacherId);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " ORDER BY rr.update_time DESC")
    List<ReviewRequest> findAll();

    @Select("SELECT " + COLUMNS + FROM_JOIN + " WHERE rr.status = 'WITHDRAW_PENDING' ORDER BY rr.update_time ASC")
    List<ReviewRequest> findWithdrawPending();

    @Select("SELECT COUNT(*) FROM review_request")
    int countAll();

    @Select("SELECT COUNT(*) FROM review_request WHERE status = #{status}")
    int countByStatus(String status);

    @Select("SELECT COUNT(*) FROM review_request WHERE source_resume_id = #{sourceResumeId} AND teacher_id = #{teacherId} AND status IN ('PENDING', 'ACCEPTED', 'WITHDRAW_PENDING', 'COMPLETED')")
    int countNonRepeatable(@Param("sourceResumeId") Long sourceResumeId, @Param("teacherId") Long teacherId);

    @Update("UPDATE review_request SET status = 'ACCEPTED', teacher_reply = #{teacherReply}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int accept(@Param("id") Long id, @Param("teacherReply") String teacherReply);

    @Update("UPDATE review_request SET status = 'DECLINED', decline_reason = #{declineReason}, decline_suggestion = #{declineSuggestion}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int decline(@Param("id") Long id, @Param("declineReason") String declineReason, @Param("declineSuggestion") String declineSuggestion);

    @Update("UPDATE review_request SET status = 'COMPLETED', teacher_reply = #{teacherReply}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int complete(@Param("id") Long id, @Param("teacherReply") String teacherReply);

    @Update("UPDATE review_request SET status = 'WITHDRAW_PENDING', withdraw_reason = #{reason}, admin_decision = NULL, admin_comment = NULL, admin_id = NULL, admin_time = NULL, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int requestWithdraw(@Param("id") Long id, @Param("reason") String reason);

    @Update("UPDATE review_request SET status = 'WITHDRAWN', admin_decision = 'APPROVED', admin_comment = #{comment}, admin_id = #{adminId}, admin_time = CURRENT_TIMESTAMP, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int approveWithdraw(@Param("id") Long id, @Param("adminId") Long adminId, @Param("comment") String comment);

    @Update("UPDATE review_request SET status = 'ACCEPTED', admin_decision = 'REJECTED', admin_comment = #{comment}, admin_id = #{adminId}, admin_time = CURRENT_TIMESTAMP, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int rejectWithdraw(@Param("id") Long id, @Param("adminId") Long adminId, @Param("comment") String comment);
}
