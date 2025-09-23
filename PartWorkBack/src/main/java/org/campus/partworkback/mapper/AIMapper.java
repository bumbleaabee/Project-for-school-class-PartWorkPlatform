package org.campus.partworkback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.campus.partworkback.DTO.MatchDTO;

import java.util.List;

@Mapper
public interface AIMapper {

    List<MatchDTO> getMatchTask(int[] arr);
}
