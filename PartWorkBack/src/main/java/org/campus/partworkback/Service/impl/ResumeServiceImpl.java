package org.campus.partworkback.Service.impl;

import org.campus.partworkback.DTO.FileDTO;
import org.campus.partworkback.Service.ResumeService;
import org.campus.partworkback.mapper.ResumeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResumeServiceImpl implements ResumeService {
    @Autowired
    private ResumeMapper resumeMapper;

    @Override
    public void addResumeUrl(Long userId, String relativePath) {
        resumeMapper.addResumeUrl(userId, relativePath);
    }

    @Override
    public FileDTO getResumeUrl(Long userId) {
        String url = resumeMapper.getResumeUrl(userId);
        return new FileDTO(url);
    }
}
