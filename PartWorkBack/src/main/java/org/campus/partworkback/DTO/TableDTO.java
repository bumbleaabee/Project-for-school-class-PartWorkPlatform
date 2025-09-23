package org.campus.partworkback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableDTO {
    private List<Object> list;
    private Integer total;
    private Integer pageNo;
    private Integer pageSize;
}
