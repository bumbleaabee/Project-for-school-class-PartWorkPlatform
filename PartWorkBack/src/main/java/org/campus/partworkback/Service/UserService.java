package org.campus.partworkback.Service;

import org.campus.partworkback.DTO.ReviewDTO;
import org.campus.partworkback.pojo.User;
import org.campus.partworkback.pojo.UserStats;

public interface UserService {

    User getById(Long id);

    void updateById(User user);

    UserStats getUsById(Long userId);

    ReviewDTO getReviewById(Integer pageNo, Integer pageSize, Long id);

    void addAvatarUrl(Long userId, String relativePath);
}
