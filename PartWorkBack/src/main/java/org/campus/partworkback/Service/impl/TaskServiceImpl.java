package org.campus.partworkback.Service.impl;

import org.campus.partworkback.DTO.RequestDTO;
import org.campus.partworkback.DTO.TaskApplicationDTO;
import org.campus.partworkback.DTO.TaskDTO;
import org.campus.partworkback.Service.TaskService;
import org.campus.partworkback.constant.HttpCode;
import org.campus.partworkback.exception.BizException;
import org.campus.partworkback.mapper.TaskMapper;
import org.campus.partworkback.pojo.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;

    @Override
    public Long addTask(Task task) {
        taskMapper.addTask(task);
        return task.getId();
    }

    @Override
    public TaskDTO getTask(Integer pageNo, Integer pageSize, String keyWord, Integer status) {
        List<Task> list = taskMapper.getTask((pageNo - 1) * pageSize, pageSize, keyWord, status);
        Integer total = taskMapper.getTaskCount(keyWord, status);
        return new TaskDTO(list, total, pageNo, pageSize);
    }

    @Override
    public Task getTaskById(Long id) {
        return taskMapper.getTaskById(id);
    }

    @Override
    public void cancelTask(Long id, Long userId) {
        List<Long> list = taskMapper.getAllTaskById(userId);
        int flag = 1;
        for (Long i : list) {
            if(Objects.equals(i, id)){
                taskMapper.cancelTask(id);
                flag = 0;
                break;
            }
        }
        if(flag == 1){
            throw new BizException(HttpCode.FORBIDDEN.getCode(), "没有相应权限");
        }
    }

    @Override
    public void addApplication(Long id, String message, Long applicantId) {
        taskMapper.addApplication(id, message, applicantId);
    }

    @Override
    public List<TaskApplicationDTO> getTaskApplication(Long id) {
        return taskMapper.getTaskApplication(id);
    }

    @Override
    public void updateTaskById(Long id, Long applicationId) {
        /*
        * 0 = 申请中（待处理）
        1 = 被拒绝（发布者未选择该申请）
        2 = 撤回（申请人主动撤回这条申请）
        3 = 被选中（发布者选择该申请人作为执行人）
        * */
        taskMapper.updateTaskById(id, applicationId);
        taskMapper.updateTaskByIdRefuse(id, applicationId);
        /*
        * UPDATE task_application
SET status = 2
WHERE id = #{applicationId};

UPDATE task_application
SET status = 1
WHERE task_id = #{id} AND id <> #{applicationId} AND status = 0;
        * */
    }

    @Override
    public void updateTaskFinish(Long id) {
        taskMapper.updateTaskFinish(id);
    }

    @Override
    public void withdrawById(Long id, Long currentUserId) {
        taskMapper.withdrawById(id, currentUserId);
    }

    @Override
    public TaskDTO getTaskByPublisherId(Long currentUserId, Integer pageNo, Integer pageSize) {
        List<Task> list = taskMapper.getTaskByPublisherId(currentUserId, (pageNo - 1) * pageSize, pageSize);
        Integer total = taskMapper.getTaskCountById(currentUserId);
        return new TaskDTO(list, total, pageNo, pageSize);
    }

    @Override
    public TaskDTO getTaskInProgress(Long userId, Integer pageNo, Integer pageSize) {
        List<Task> list = taskMapper.getTaskInProgress(userId, (pageNo - 1) * pageSize, pageSize);
        Integer total = taskMapper.getAllTaskInProgress(userId);
        TaskDTO taskDTO = new TaskDTO(list, total, pageNo, pageSize);
        return taskDTO;
    }

    @Override
    public void acceptTask(Long id) {
        taskMapper.acceptTask(id);
        taskMapper.acceptApplyFinish(id);
    }

    @Override
    public TaskDTO getTaskAccepted(Long userId, Integer pageNo, Integer pageSize) {
        List<Task> list = taskMapper.getTaskAccepted(userId, (pageNo - 1) * pageSize, pageSize);
        Integer total = taskMapper.getTaskAcceptedCount(userId);
        TaskDTO taskDTO = new TaskDTO(list, total, pageNo, pageSize);
        return taskDTO;
    }

    @Override
    public void applyFinish(Long userId, Long id) {
        boolean flag = taskMapper.hasFinishApply(id);
        if(flag){
            throw new BizException(HttpCode.TASK_STATUS_INVALID.getCode(), "任务已申请完成");
        }
        taskMapper.applyFinish(userId, id);
    }

    @Override
    public List<Long> getChangeButton(Long userId) {
        return taskMapper.getChangeButton(userId);
    }

    @Override
    public List<RequestDTO> getApplyRequestById(Long userId) {
        return taskMapper.getApplyRequestById(userId);
    }
}
