package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.Resume;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResumeMapper {
    String COLUMNS = "id, student_id, source_resume_id, generated_by_teacher_id, template_id, version_type, title, name, phone, email, target_position, education, experience, skills, awards, self_evaluation, status, teacher_comment, frozen, freeze_reason, create_time, update_time";

    @Select("SELECT " + COLUMNS + " FROM resume WHERE student_id = #{studentId} ORDER BY update_time DESC LIMIT 1")
    Resume findLatestByStudentId(Long studentId);

    @Select("SELECT " + COLUMNS + " FROM resume WHERE student_id = #{studentId} ORDER BY update_time DESC")
    List<Resume> findByStudentId(Long studentId);

    @Select("SELECT " + COLUMNS + " FROM resume ORDER BY update_time DESC")
    List<Resume> findAll();

    @Select("SELECT " + COLUMNS + " FROM resume WHERE id = #{id}")
    Resume findById(Long id);

    @Select("SELECT " + COLUMNS + " FROM resume WHERE source_resume_id = #{sourceResumeId} ORDER BY update_time DESC")
    List<Resume> findVersionsBySourceResumeId(Long sourceResumeId);

    @Select("SELECT " + COLUMNS + " FROM resume WHERE status = 'SUBMITTED' ORDER BY update_time ASC")
    List<Resume> findSubmitted();

    @Select("SELECT COUNT(*) FROM resume")
    int countAll();

    @Select("SELECT COUNT(*) FROM resume WHERE frozen = 1")
    int countFrozen();

    @Insert("INSERT INTO resume(student_id, source_resume_id, generated_by_teacher_id, template_id, version_type, title, name, phone, email, target_position, education, experience, skills, awards, self_evaluation, status, teacher_comment, frozen, freeze_reason) VALUES(#{studentId}, #{sourceResumeId}, #{generatedByTeacherId}, #{templateId}, #{versionType}, #{title}, #{name}, #{phone}, #{email}, #{targetPosition}, #{education}, #{experience}, #{skills}, #{awards}, #{selfEvaluation}, #{status}, #{teacherComment}, #{frozen}, #{freezeReason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Resume resume);

    @Update("UPDATE resume SET template_id = #{templateId}, title = #{title}, name = #{name}, phone = #{phone}, email = #{email}, target_position = #{targetPosition}, education = #{education}, experience = #{experience}, skills = #{skills}, awards = #{awards}, self_evaluation = #{selfEvaluation}, status = 'DRAFT', update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateContent(Resume resume);

    @Update("UPDATE resume SET status = 'SUBMITTED', update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int submit(Long id);

    @Update("UPDATE resume SET status = #{status}, teacher_comment = #{teacherComment}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int review(@Param("id") Long id, @Param("status") String status, @Param("teacherComment") String teacherComment);

    @Update("UPDATE resume SET frozen = 1, freeze_reason = #{reason}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int freeze(@Param("id") Long id, @Param("reason") String reason);

    @Update("UPDATE resume SET frozen = 0, freeze_reason = NULL, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int unfreeze(Long id);

    @Delete("DELETE FROM resume WHERE id = #{id}")
    int deleteById(Long id);
}
