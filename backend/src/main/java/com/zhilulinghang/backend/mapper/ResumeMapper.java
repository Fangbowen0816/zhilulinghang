package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.Resume;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResumeMapper {
    @Select("SELECT id, student_id, name, education, experience, skills, status, teacher_comment, create_time, update_time FROM resume WHERE student_id = #{studentId} ORDER BY update_time DESC LIMIT 1")
    Resume findLatestByStudentId(Long studentId);

    @Select("SELECT id, student_id, name, education, experience, skills, status, teacher_comment, create_time, update_time FROM resume WHERE id = #{id}")
    Resume findById(Long id);

    @Select("SELECT id, student_id, name, education, experience, skills, status, teacher_comment, create_time, update_time FROM resume WHERE status = 'SUBMITTED' ORDER BY update_time ASC")
    List<Resume> findSubmitted();

    @Insert("INSERT INTO resume(student_id, name, education, experience, skills, status, teacher_comment) VALUES(#{studentId}, #{name}, #{education}, #{experience}, #{skills}, #{status}, #{teacherComment})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Resume resume);

    @Update("UPDATE resume SET name = #{name}, education = #{education}, experience = #{experience}, skills = #{skills}, status = 'DRAFT', update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateContent(Resume resume);

    @Update("UPDATE resume SET status = 'SUBMITTED', update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int submit(Long id);

    @Update("UPDATE resume SET status = #{status}, teacher_comment = #{teacherComment}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int review(@Param("id") Long id, @Param("status") String status, @Param("teacherComment") String teacherComment);
}
