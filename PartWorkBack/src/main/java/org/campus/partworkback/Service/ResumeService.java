package org.campus.partworkback.Service;

import org.campus.partworkback.DTO.FileDTO;

public interface ResumeService {
    void addResumeUrl(Long userId, String relativePath);

    FileDTO getResumeUrl(Long userId);
}
