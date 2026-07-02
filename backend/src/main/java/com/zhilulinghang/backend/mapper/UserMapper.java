package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT id, username, password, role, enabled, create_time FROM `user` WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT id, username, password, role, enabled, create_time FROM `user` WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT id, username, NULL AS password, role, enabled, create_time FROM `user` ORDER BY create_time DESC")
    List<User> findAll();

    @Select("SELECT id, username, NULL AS password, role, enabled, create_time FROM `user` WHERE role = #{role} AND enabled = 1 ORDER BY create_time DESC")
    List<User> findByRole(String role);

    @Select("SELECT COUNT(*) FROM `user` WHERE role = #{role}")
    int countByRole(String role);

    @Insert("INSERT INTO `user`(username, password, role, enabled) VALUES(#{username}, #{password}, #{role}, COALESCE(#{enabled}, 1))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE `user` SET enabled = #{enabled} WHERE id = #{id}")
    int updateEnabled(@Param("id") Long id, @Param("enabled") Boolean enabled);

    @Update("UPDATE `user` SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
