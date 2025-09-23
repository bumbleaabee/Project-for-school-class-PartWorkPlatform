package org.campus.partworkback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.campus.partworkback.DTO.TaskApplicationDTO;
import org.campus.partworkback.VO.TaskVO;
import org.campus.partworkback.pojo.Task;

import java.util.List;

@Mapper
public interface TaskMapper {
    Integer addTask(Task task);

    List<Long> getAllTaskById(Long userId);

    List<Task> getTask(Integer offset, Integer pageSize, String keyWord, Integer status);

    Task getTaskById(Long id);

    void cancelTask(Long id);

    void addApplication(Long id, String message, Long applicantId);

    List<TaskApplicationDTO> getTaskApplication(Long id);

    void updateTaskById(Long id, Long applicationId);

    void updateTaskFinish(Long id);

    void withdrawById(Long id, Long currentUserId);

    List<Task> getTaskByPublisherId(Long currentUserId, Integer offset, Integer pageSize);

    Integer getTaskCount(String keyWord, Integer status);

    Integer getTaskCountById(Long currentUserId);

    List<Task> getTaskInProgress(Long userId, Integer offset, Integer pageSize);

    Integer getAllTaskInProgress(Long userId);

    void acceptTask(Long id);

    List<Task> getTaskAccepted(Long userId, Integer offset, Integer pageSize);

    Integer getTaskAcceptedCount(Long userId);

    void applyFinish(Long userId, Long id);

    boolean hasFinishApply(Long id);

    void acceptApplyFinish(Long id);

    List<Long> getChangeButton(Long userId);

    void updateTaskByIdRefuse(Long id, Long applicationId);

    List<TaskVO> getTaskList(Long userId);
}
