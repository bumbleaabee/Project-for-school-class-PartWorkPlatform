package org.campus.partworkback.Service.impl;

import org.campus.partworkback.DTO.ReviewGetDTO;
import org.campus.partworkback.Service.ReviewService;
import org.campus.partworkback.constant.HttpCode;
import org.campus.partworkback.exception.BizException;
import org.campus.partworkback.mapper.ReviewMapper;
import org.campus.partworkback.pojo.Review;
import org.campus.partworkback.pojo.ReviewData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    ReviewMapper reviewMapper;

    @Override
    public void addReview(Review review) {
        // 方向判定：通过 content/rating 之外还需知道评价对象是谁。
        // 这里按约定：买家(接单人)评价卖家 => reviewer 必须是 chosen_applicant_id，target=publisher_id
        //           卖家(发布者)评价买家 => reviewer 必须是 publisher_id，target=chosen_applicant_id
        // 前端不传明文方向，这里根据 reviewer 身份自动判断。

        Long taskId = review.getTaskId();
        if (taskId == null || review.getReviewerId() == null) {
            throw new BizException(HttpCode.PARAM_ERROR.getCode(), "参数缺失");
        }

        Integer status = reviewMapper.getTaskStatus(taskId);
        if (status == null) {
            throw new BizException(HttpCode.TASK_NOT_FOUND.getCode(), "任务不存在");
        }
        // 仅允许对已完成任务进行评价（按现有业务约定：5=已完成）
        if (status != 5) {
            throw new BizException(HttpCode.TASK_STATUS_INVALID.getCode(), "任务未完成，无法评价");
        }

        Long publisherId = reviewMapper.getPublisherId(taskId);
        Long assigneeId = reviewMapper.getTargetId(taskId); // 注意：这里复用现有方法，返回的是 chosen_applicant_id

        Long reviewerId = review.getReviewerId();
        if (assigneeId != null && reviewerId.equals(assigneeId)) {
            // 买家评价卖家
            review.setTargetUserId(publisherId);
        } else if (reviewerId.equals(publisherId)) {
            // 卖家评价买家
            if (assigneeId == null) {
                throw new BizException(HttpCode.PARAM_ERROR.getCode(), "任务无接单人，无法评价");
            }
            review.setTargetUserId(assigneeId);
        } else {
            // 既不是该任务的发布者，也不是该任务的接单人，无权评价
            throw new BizException(HttpCode.FORBIDDEN.getCode(), "无法评价该用户");
        }

        reviewMapper.addReview(review);
    }

    @Override
    public ReviewGetDTO getReview(long id, Integer pageNo, Integer pageSize) {
        List<ReviewData> list = reviewMapper.getReview(id, (pageNo - 1) * pageSize, pageSize);
        Integer total = reviewMapper.getAllReview(id);
        return new ReviewGetDTO(list, total, pageNo, pageSize);
    }
}
