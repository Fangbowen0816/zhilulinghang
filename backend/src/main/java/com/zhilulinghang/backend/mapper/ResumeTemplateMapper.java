package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.ResumeTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ResumeTemplateMapper {
    String COLUMNS = "id, name, industry, job_type, structure, style, enabled, create_time, update_time";

    @Select("SELECT " + COLUMNS + " FROM resume_template WHERE enabled = 1 ORDER BY id ASC")
    List<ResumeTemplate> findEnabled();

    @Select("SELECT " + COLUMNS + " FROM resume_template WHERE id = #{id}")
    ResumeTemplate findById(Long id);

    @Select("SELECT " + COLUMNS + " FROM resume_template WHERE enabled = 1 ORDER BY id ASC LIMIT 1")
    ResumeTemplate findDefault();
}
