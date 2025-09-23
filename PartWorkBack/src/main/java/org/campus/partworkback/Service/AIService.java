package org.campus.partworkback.Service;

import org.campus.partworkback.DTO.MatchDTO;

import java.util.List;

public interface AIService {
    List<MatchDTO> matchTask(Long userId);
}
