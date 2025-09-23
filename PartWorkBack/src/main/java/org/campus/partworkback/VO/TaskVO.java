package org.campus.partworkback.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskVO {
    private Long id;
    private String description;
    private String tags;

    @Override
    public String toString() {
        return "id=" + id + ", description='" + description + "', tags='" + tags + "'" + '\n';
    }
}
