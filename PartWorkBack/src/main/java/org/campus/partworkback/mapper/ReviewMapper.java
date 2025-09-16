package org.campus.partworkback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.campus.partworkback.pojo.Review;
import org.campus.partworkback.pojo.ReviewData;

import java.util.List;

@Mapper
public interface ReviewMapper {
    void addReview(Review review);

    List<ReviewData> getReview(long id, Integer offset, Integer pageSize);

    Integer getAllReview(long id);

    Long getTargetId(Long taskId);

    List<Long> getAllApplicantId(Long reviewerId);

    Long getPublisherId(Long taskId);

    Integer getTaskStatus(Long taskId);
}
