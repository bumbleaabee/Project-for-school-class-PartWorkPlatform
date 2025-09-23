package org.campus.partworkback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.campus.partworkback.pojo.Review;
import org.campus.partworkback.pojo.User;
import org.campus.partworkback.pojo.UserStats;

import java.util.List;

@Mapper
public interface UserMapper {
    public User getById(Long id);

    public void updateById(User user);

    public UserStats getUsById(Long userId);

    public List<Review> getReviewById(Integer offset, Integer pageSize, Long id);

    Integer getCountReview(Long id);

    void addAvatarUrl(Long userId, String url);

    void addUser(User user);

    String getDescription(Long userId);
}
