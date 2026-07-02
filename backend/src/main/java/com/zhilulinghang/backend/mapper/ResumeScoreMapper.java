package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.ResumeScore;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResumeScoreMapper {
    String COLUMNS = "rs.id, rs.request_id, rs.resume_id, rs.teacher_id, rs.student_id, rs.score, rs.remark, rs.create_time, rs.update_time, r.title AS resume_title, tp.display_name AS teacher_display_name, u.username AS student_username";
    String FROM_JOIN = " FROM resume_score rs JOIN resume r ON rs.resume_id = r.id LEFT JOIN teacher_profile tp ON rs.teacher_id = tp.teacher_id LEFT JOIN `user` u ON rs.student_id = u.id";

    @Insert("INSERT INTO resume_score(request_id, resume_id, teacher_id, student_id, score, remark) VALUES(#{requestId}, #{resumeId}, #{teacherId}, #{studentId}, #{score}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ResumeScore score);

    @Update("UPDATE resume_score SET score = #{score}, remark = #{remark}, update_time = CURRENT_TIMESTAMP WHERE request_id = #{requestId}")
    int updateByRequestId(ResumeScore score);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " WHERE rs.request_id = #{requestId}")
    ResumeScore findByRequestId(Long requestId);

    @Select("SELECT " + COLUMNS + FROM_JOIN + " ORDER BY rs.update_time DESC")
    List<ResumeScore> findAll();
}
