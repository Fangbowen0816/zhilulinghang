package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.PlatformSetting;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PlatformSettingMapper {
    String COLUMNS = "id, setting_key, setting_value, description, update_time";

    @Select("SELECT " + COLUMNS + " FROM platform_setting ORDER BY setting_key ASC")
    List<PlatformSetting> findAll();

    @Select("SELECT " + COLUMNS + " FROM platform_setting WHERE id = #{id}")
    PlatformSetting findById(Long id);

    @Select("SELECT " + COLUMNS + " FROM platform_setting WHERE setting_key = #{settingKey}")
    PlatformSetting findByKey(String settingKey);

    @Insert("INSERT INTO platform_setting(setting_key, setting_value, description) VALUES(#{settingKey}, #{settingValue}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PlatformSetting setting);

    @Update("UPDATE platform_setting SET setting_value = #{settingValue}, description = #{description}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(@Param("id") Long id, @Param("settingValue") String settingValue, @Param("description") String description);
}
