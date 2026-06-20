package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.AdminActionLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminActionLogMapper {
    String COLUMNS = "id, admin_id, admin_username, action, target_type, target_id, detail, create_time";

    @Select("SELECT " + COLUMNS + " FROM admin_action_log ORDER BY create_time DESC, id DESC LIMIT 500")
    List<AdminActionLog> findRecent();

    @Insert("INSERT INTO admin_action_log(admin_id, admin_username, action, target_type, target_id, detail) VALUES(#{adminId}, #{adminUsername}, #{action}, #{targetType}, #{targetId}, #{detail})")
    int insert(AdminActionLog log);
}
