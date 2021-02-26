package com.community.dao.elasticsearch;

import com.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

//repository是spring针对数据访问层的注解，mapper是针对MyBatis的注解
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer>{
}
