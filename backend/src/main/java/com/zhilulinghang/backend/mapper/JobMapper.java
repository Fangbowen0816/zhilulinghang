package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobMapper {
    String COLUMNS = "id, title, company, industry, city, salary_range, requirement, description, status, create_time, update_time";

    @Select("SELECT " + COLUMNS + " FROM job " +
            "WHERE (#{keyword} IS NULL OR #{keyword} = '' OR title LIKE CONCAT('%', #{keyword}, '%') OR company LIKE CONCAT('%', #{keyword}, '%') OR requirement LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{industry} IS NULL OR #{industry} = '' OR industry = #{industry}) " +
            "AND (#{city} IS NULL OR #{city} = '' OR city = #{city}) " +
            "AND (#{status} IS NULL OR #{status} = '' OR status = #{status}) " +
            "ORDER BY status DESC, update_time DESC")
    List<Job> search(@Param("keyword") String keyword, @Param("industry") String industry, @Param("city") String city, @Param("status") String status);

    @Select("SELECT " + COLUMNS + " FROM job WHERE id = #{id}")
    Job findById(Long id);
}
