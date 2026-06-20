package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.ReviewRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ReviewRecordMapper {
    String COLUMNS = "rr.id, rr.request_id, rr.source_resume_id, rr.returned_resume_id, rr.student_id, rr.teacher_id, rr.action, rr.comment, rr.teacher_deleted, rr.create_time, src.title AS source_resume_title, returned.title AS returned_resume_title, tp.display_name AS teacher_display_name";
    String FROM_JOIN = " FROM review_record rr JOIN resume src ON rr.source_resume_id = src.id LEFT JOIN resume returned ON rr.returned_resume_id = returned.id LEFT JOIN teacher_profile tp ON rr.teacher_id = tp.teacher_id";

    @Insert("INSERT INTO review_record(request_id, source_resume_id, returned_resume_id, student_id, teacher_id, action, comment, teacher_deleted) VALUES(#{requestId}, #{sourceResumeId}, #{returnedResumeId}, #{studentId}, #{teacherId}, #{action}, #{comment}, #{teacherDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReviewRecord record);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " WHERE rr.student_id = #{studentId} ORDER BY rr.create_time DESC")
    List<ReviewRecord> findByStudentId(Long studentId);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " ORDER BY rr.create_time DESC")
    List<ReviewRecord> findAll();

    @Select("SELECT " + COLUMNS + FROM_JOIN + " WHERE rr.teacher_id = #{teacherId} AND rr.teacher_deleted = 0 ORDER BY rr.create_time DESC")
    List<ReviewRecord> findVisibleByTeacherId(Long teacherId);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " WHERE rr.id = #{id}")
    ReviewRecord findById(Long id);

    @Select("SELECT COUNT(*) FROM review_record")
    int countAll();

    @Update("UPDATE review_record SET teacher_deleted = 1 WHERE id = #{id} AND teacher_id = #{teacherId}")
    int hideForTeacher(@Param("id") Long id, @Param("teacherId") Long teacherId);
}
