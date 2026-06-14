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
    String COLUMNS = "id, student_id, title, name, phone, email, target_position, education, experience, skills, awards, self_evaluation, status, teacher_comment, create_time, update_time";

    @Select("SELECT " + COLUMNS + " FROM resume WHERE student_id = #{studentId} ORDER BY update_time DESC LIMIT 1")
    Resume findLatestByStudentId(Long studentId);

    @Select("SELECT " + COLUMNS + " FROM resume WHERE student_id = #{studentId} ORDER BY update_time DESC")
    List<Resume> findByStudentId(Long studentId);

    @Select("SELECT " + COLUMNS + " FROM resume WHERE id = #{id}")
    Resume findById(Long id);

    @Select("SELECT " + COLUMNS + " FROM resume WHERE status = 'SUBMITTED' ORDER BY update_time ASC")
    List<Resume> findSubmitted();

    @Insert("INSERT INTO resume(student_id, title, name, phone, email, target_position, education, experience, skills, awards, self_evaluation, status, teacher_comment) VALUES(#{studentId}, #{title}, #{name}, #{phone}, #{email}, #{targetPosition}, #{education}, #{experience}, #{skills}, #{awards}, #{selfEvaluation}, #{status}, #{teacherComment})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Resume resume);

    @Update("UPDATE resume SET title = #{title}, name = #{name}, phone = #{phone}, email = #{email}, target_position = #{targetPosition}, education = #{education}, experience = #{experience}, skills = #{skills}, awards = #{awards}, self_evaluation = #{selfEvaluation}, status = 'DRAFT', update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateContent(Resume resume);

    @Update("UPDATE resume SET status = 'SUBMITTED', update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int submit(Long id);

    @Update("UPDATE resume SET status = #{status}, teacher_comment = #{teacherComment}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int review(@Param("id") Long id, @Param("status") String status, @Param("teacherComment") String teacherComment);

    @Delete("DELETE FROM resume WHERE id = #{id}")
    int deleteById(Long id);
}
