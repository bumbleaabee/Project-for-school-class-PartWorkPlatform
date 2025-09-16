package org.campus.partworkback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.campus.partworkback.pojo.Task;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {
    private List<Task> list;
    private Integer total;
    private Integer pageNo;
    private Integer pageSize;
}
