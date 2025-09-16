package org.campus.partworkback.Service;

import org.campus.partworkback.DTO.ReviewGetDTO;
import org.campus.partworkback.pojo.Review;

public interface ReviewService {
    void addReview(Review review);

    ReviewGetDTO getReview(long id, Integer pageNo, Integer pageSize);
}
