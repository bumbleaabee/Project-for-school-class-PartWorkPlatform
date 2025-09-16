package org.campus.partworkback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
* CREATE TABLE task_application (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  applicant_id BIGINT NOT NULL,
  message VARCHAR(255),
  status TINYINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_task_applicant (task_id, applicant_id),
  INDEX idx_app_task (task_id),
  INDEX idx_app_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
* */
public class TaskApplication {
    private Long id;
    private Long taskId;
    private Long applicantId;
    private String message;
    private Integer status;
    private LocalDateTime createdAt = LocalDateTime.now();
}
