package com.zhilulinghang.backend.mapper;

import com.zhilulinghang.backend.model.TeacherProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TeacherProfileMapper {
    String COLUMNS = "id, teacher_id, display_name, department, title, bio, expertise_tags, available, approval_status, approval_comment, approved_by, approved_time, create_time, update_time";

    @Insert("INSERT INTO teacher_profile(teacher_id, display_name, department, title, bio, expertise_tags, available, approval_status) VALUES(#{teacherId}, #{displayName}, #{department}, #{title}, #{bio}, #{expertiseTags}, #{available}, #{approvalStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TeacherProfile profile);

    @Select("SELECT " + COLUMNS + " FROM teacher_profile WHERE teacher_id = #{teacherId}")
    TeacherProfile findByTeacherId(Long teacherId);

    @Select("SELECT " + COLUMNS + " FROM teacher_profile WHERE id = #{id}")
    TeacherProfile findById(Long id);

    @Select("SELECT " + COLUMNS + " FROM teacher_profile WHERE approval_status = 'APPROVED' AND available = 1 ORDER BY update_time DESC")
    List<TeacherProfile> findAvailableApproved();

    @Select("SELECT " + COLUMNS + " FROM teacher_profile WHERE approval_status = 'APPROVED' AND available = 1 ORDER BY RAND() LIMIT 1")
    TeacherProfile findRandomAvailableApproved();

    @Select("SELECT " + COLUMNS + " FROM teacher_profile ORDER BY update_time DESC")
    List<TeacherProfile> findAll();

    @Select("SELECT " + COLUMNS + " FROM teacher_profile WHERE approval_status = 'PENDING' ORDER BY update_time ASC")
    List<TeacherProfile> findPending();

    @Select("SELECT COUNT(*) FROM teacher_profile WHERE approval_status = #{status}")
    int countByApprovalStatus(String status);

    @Update("UPDATE teacher_profile SET display_name = #{displayName}, department = #{department}, title = #{title}, bio = #{bio}, expertise_tags = #{expertiseTags}, available = #{available}, approval_status = #{approvalStatus}, approval_comment = #{approvalComment}, update_time = CURRENT_TIMESTAMP WHERE teacher_id = #{teacherId}")
    int updateByTeacherId(TeacherProfile profile);

    @Update("UPDATE teacher_profile SET approval_status = 'APPROVED', approval_comment = NULL, approved_by = #{adminId}, approved_time = CURRENT_TIMESTAMP, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int approve(@Param("id") Long id, @Param("adminId") Long adminId);

    @Update("UPDATE teacher_profile SET approval_status = 'REJECTED', approval_comment = #{comment}, available = 0, approved_by = #{adminId}, approved_time = CURRENT_TIMESTAMP, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int reject(@Param("id") Long id, @Param("adminId") Long adminId, @Param("comment") String comment);

    @Update("UPDATE teacher_profile SET available = 0, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int forceCloseAvailable(Long id);

    @Update("UPDATE teacher_profile SET available = #{available}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int setAvailable(@Param("id") Long id, @Param("available") Boolean available);
}
