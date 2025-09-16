package org.campus.partworkback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
* CREATE TABLE review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  reviewer_id BIGINT NOT NULL,
  target_user_id BIGINT NOT NULL,
  rating TINYINT NOT NULL,
  content VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_task_reviewer (task_id, reviewer_id),
  INDEX idx_review_target (target_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
* */
public class Review {
    private Long id;
    private Long taskId;
    private Long reviewerId;
    private Long targetUserId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();
}
