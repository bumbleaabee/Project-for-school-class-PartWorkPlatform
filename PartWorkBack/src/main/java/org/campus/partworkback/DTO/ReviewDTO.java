package org.campus.partworkback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.campus.partworkback.pojo.Review;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private List<Review> list;
    private Integer total;
    private Integer pageNo;
    private Integer pageSize;
}
