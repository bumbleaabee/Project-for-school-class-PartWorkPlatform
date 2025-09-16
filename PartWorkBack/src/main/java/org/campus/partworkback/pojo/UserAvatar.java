package org.campus.partworkback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
* CREATE TABLE IF NOT EXISTS user_avatar (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  url         VARCHAR(512) NOT NULL,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_avatar_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT uq_user_avatar_user UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
* */
public class UserAvatar {
    private Long id;
    private Long user_id;
    private String url;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
