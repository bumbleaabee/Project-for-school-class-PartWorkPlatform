package org.campus.partworkback.Service.impl;

import org.campus.partworkback.DTO.ReviewDTO;
import org.campus.partworkback.Service.UserService;
import org.campus.partworkback.mapper.UserMapper;
import org.campus.partworkback.pojo.Review;
import org.campus.partworkback.pojo.User;
import org.campus.partworkback.pojo.UserStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User getById(Long id) {
        return userMapper.getById(id);
    }

    @Override
    public void updateById(User user) {
        userMapper.updateById(user);
    }

    @Override
    public UserStats getUsById(Long userId) {
        return userMapper.getUsById(userId);
    }

    @Override
    public ReviewDTO getReviewById(Integer pageNo, Integer pageSize, Long id) {
        Integer offset = (pageNo - 1) * pageSize;
        List<Review> list = userMapper.getReviewById(offset, pageSize, id);
        Integer total = userMapper.getCountReview(id);
        return new ReviewDTO(list, total, pageNo, pageSize);
    }

    @Override
    public void addAvatarUrl(Long userId, String relativePath) {
        userMapper.addAvatarUrl(userId, relativePath);
    }
}
