package org.campus.partworkback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewData {
    private Long id;
    private Long taskId;
    private Long fromUserId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}
