package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.ApplicationExperience;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ApplicationExperienceMapper {
    String COLUMNS = "id, application_id, student_id, stage, content, create_time, update_time";

    @Select("SELECT " + COLUMNS + " FROM application_experience WHERE application_id = #{applicationId} ORDER BY create_time DESC")
    List<ApplicationExperience> findByApplicationId(Long applicationId);

    @Select("SELECT " + COLUMNS + " FROM application_experience WHERE id = #{id}")
    ApplicationExperience findById(Long id);

    @Insert("INSERT INTO application_experience(application_id, student_id, stage, content) VALUES(#{applicationId}, #{studentId}, #{stage}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ApplicationExperience experience);

    @Update("UPDATE application_experience SET stage = #{stage}, content = #{content}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(@Param("id") Long id, @Param("stage") String stage, @Param("content") String content);

    @Delete("DELETE FROM application_experience WHERE id = #{id}")
    int deleteById(Long id);
}
