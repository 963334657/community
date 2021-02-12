package com.community.dao;

import com.community.entity.Comment;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface CommentMapper {

    List<Comment> selectCommenmtByEntity(@Param("entityType") int entityType,
                                         @Param("entityId") int entityId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    int selectCountByEntity(@Param("entityType") int entityType,
                            @Param("entityId") int entityId);

    int insertComment(Comment comment);
}

















