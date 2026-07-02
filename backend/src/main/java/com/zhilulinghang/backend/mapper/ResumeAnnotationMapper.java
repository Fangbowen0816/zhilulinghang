package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.ResumeAnnotation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ResumeAnnotationMapper {
    String COLUMNS = "ra.id, ra.request_id, ra.resume_id, ra.teacher_id, ra.student_id, ra.field_name, ra.mark_type, ra.content, ra.create_time, ra.update_time, r.title AS resume_title, tp.display_name AS teacher_display_name, u.username AS student_username";
    String FROM_JOIN = " FROM resume_annotation ra JOIN resume r ON ra.resume_id = r.id LEFT JOIN teacher_profile tp ON ra.teacher_id = tp.teacher_id LEFT JOIN `user` u ON ra.student_id = u.id";

    @Insert("INSERT INTO resume_annotation(request_id, resume_id, teacher_id, student_id, field_name, mark_type, content) VALUES(#{requestId}, #{resumeId}, #{teacherId}, #{studentId}, #{fieldName}, #{markType}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ResumeAnnotation annotation);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " WHERE ra.id = #{id}")
    ResumeAnnotation findById(Long id);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " WHERE ra.request_id = #{requestId} ORDER BY ra.create_time ASC")
    List<ResumeAnnotation> findByRequestId(Long requestId);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " ORDER BY ra.update_time DESC")
    List<ResumeAnnotation> findAll();

    @Delete("DELETE FROM resume_annotation WHERE id = #{id}")
    int deleteById(Long id);
}
