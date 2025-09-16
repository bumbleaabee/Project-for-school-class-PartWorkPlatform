package org.campus.partworkback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.campus.partworkback.pojo.ReviewData;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewGetDTO {
    private List<ReviewData> list;
    private Integer total;
    private Integer pageNo;
    private Integer pageSize;
}
