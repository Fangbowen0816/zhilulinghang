package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT id, username, password, role, create_time FROM `user` WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT id, username, password, role, create_time FROM `user` WHERE id = #{id}")
    User findById(Long id);
}
