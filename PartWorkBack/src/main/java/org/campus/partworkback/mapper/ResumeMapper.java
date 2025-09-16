package org.campus.partworkback.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResumeMapper {
    void addResumeUrl(Long userId, String url);

    String getResumeUrl(Long userId);
}
