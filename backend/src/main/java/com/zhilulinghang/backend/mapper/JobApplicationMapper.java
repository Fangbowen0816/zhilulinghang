package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.JobApplication;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface JobApplicationMapper {
    String COLUMNS = "id, student_id, resume_id, job_id, status, apply_time, update_time";
    String DETAIL_COLUMNS = "ja.id, ja.student_id, ja.resume_id, ja.job_id, ja.status, ja.apply_time, ja.update_time, j.title AS job_title, j.company, j.industry, j.city, j.salary_range, r.title AS resume_title";

    @Insert("INSERT INTO job_application(student_id, resume_id, job_id, status) VALUES(#{studentId}, #{resumeId}, #{jobId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(JobApplication application);

    @Select("SELECT " + COLUMNS + " FROM job_application WHERE student_id = #{studentId} AND job_id = #{jobId}")
    JobApplication findByStudentAndJob(@Param("studentId") Long studentId, @Param("jobId") Long jobId);

    @Select("SELECT " + DETAIL_COLUMNS + " FROM job_application ja " +
            "JOIN job j ON ja.job_id = j.id " +
            "JOIN resume r ON ja.resume_id = r.id " +
            "WHERE ja.student_id = #{studentId} " +
            "ORDER BY ja.update_time DESC")
    List<JobApplication> findByStudentId(Long studentId);

    @Select("SELECT " + DETAIL_COLUMNS + " FROM job_application ja " +
            "JOIN job j ON ja.job_id = j.id " +
            "JOIN resume r ON ja.resume_id = r.id " +
            "WHERE ja.id = #{id}")
    JobApplication findDetailById(Long id);

    @Select("SELECT " + COLUMNS + " FROM job_application WHERE id = #{id}")
    JobApplication findById(Long id);

    @Update("UPDATE job_application SET status = #{status}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
