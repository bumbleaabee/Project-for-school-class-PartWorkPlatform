package org.campus.partworkback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.campus.partworkback.pojo.Applicant;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskApplicationDTO {
    private Long id;
    private Long taskId;
    private Applicant applicant;
    private String message;
    private Integer status;
    private LocalDateTime createdAt = LocalDateTime.now();
}
