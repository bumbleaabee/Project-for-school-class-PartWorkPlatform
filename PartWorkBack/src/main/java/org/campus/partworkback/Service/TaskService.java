package org.campus.partworkback.Service;

import org.campus.partworkback.DTO.RequestDTO;
import org.campus.partworkback.DTO.TaskApplicationDTO;
import org.campus.partworkback.DTO.TaskDTO;
import org.campus.partworkback.pojo.Task;

import java.util.List;

public interface TaskService {
    Long addTask(Task task);

    TaskDTO getTask(Integer pageNo, Integer pageSize, String keyWord, Integer status);

    Task getTaskById(Long id);

    void cancelTask(Long id, Long userId);

    void addApplication(Long id, String message, Long applicantId);

    List<TaskApplicationDTO> getTaskApplication(Long id);

    void updateTaskById(Long id, Long applicationId);

    void updateTaskFinish(Long id);

    void withdrawById(Long id, Long currentUserId);

    TaskDTO getTaskByPublisherId(Long currentUserId, Integer pageNo, Integer pageSize);

    TaskDTO getTaskInProgress(Long userId, Integer pageNo, Integer pageSize);

    void acceptTask(Long id);

    TaskDTO getTaskAccepted(Long userId, Integer pageNo, Integer pageSize);

    void applyFinish(Long userId, Long id);

    List<Long> getChangeButton(Long userId);

    List<RequestDTO> getApplyRequestById(Long userId);
}
