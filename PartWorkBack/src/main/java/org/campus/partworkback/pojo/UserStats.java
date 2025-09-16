package org.campus.partworkback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
* CREATE TABLE user_stats (
  user_id BIGINT PRIMARY KEY,
  tasks_published INT NOT NULL DEFAULT 0,
  tasks_finished INT NOT NULL DEFAULT 0,
  avg_rating DECIMAL(3,2),
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
* */
public class UserStats {
    private Long userId;
    private Integer tasksPublished;
    private Integer tasksFinished;
    private BigDecimal avgRating;
    private LocalDateTime updatedAt = LocalDateTime.now();
}
