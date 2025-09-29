package org.campus.partworkback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.campus.partworkback.Service.ReviewService;
import org.campus.partworkback.pojo.Result;
import org.campus.partworkback.pojo.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Transactional
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    ReviewService reviewService;

    @PostMapping
    public Result addReview(@RequestBody Review review, HttpServletRequest request) {
        // reviewerId 从 Token 解析，避免前端必须传递且绕过鉴权
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        review.setReviewerId(currentUserId);
        reviewService.addReview(review);
        return Result.success();
    }

//    @GetMapping("/{id}/reviews")
//    public Result getReviews(@PathVariable long id, @RequestParam Integer pageNo, @RequestParam Integer pageSize) {
//        ReviewGetDTO reviewGetDTO = reviewService.getReview(id, pageNo, pageSize);
//        return Result.success(reviewGetDTO);
//    }
}
