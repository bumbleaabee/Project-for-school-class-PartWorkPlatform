package org.campus.partworkback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
* CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(100) UNIQUE,
  phone VARCHAR(20) UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  avatar_url VARCHAR(255),
  school VARCHAR(100),
  grade VARCHAR(20),
  skills VARCHAR(255),
  credit_score INT NOT NULL DEFAULT 60,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
* */
public class User {
    private Long id;
    private String email;
    private String phone;
    private String passwordHash;
    private String nickname;
    private String school;
    private String grade;
    private String skills;
    private Integer creditScore = 60;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
