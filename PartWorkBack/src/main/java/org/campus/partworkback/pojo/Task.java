package org.campus.partworkback.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
* CREATE TABLE task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  publisher_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  description TEXT NOT NULL,
  budget_min DECIMAL(10,2),
  budget_max DECIMAL(10,2),
  deadline DATETIME NOT NULL,
  tags VARCHAR(255),
  status TINYINT NOT NULL,
  chosen_applicant_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_task_publisher (publisher_id),
  INDEX idx_task_status_deadline (status, deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
*
* */
public class Task {
    private Long id;
    private Long publisherId;
    private String title;
    private String description;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    private String tags;
    private Integer status;
    private Long chosenApplicantId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    private String publisherName;
}
