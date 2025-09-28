package org.campus.partworkback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.campus.partworkback.DTO.*;
import org.campus.partworkback.Service.TaskService;
import org.campus.partworkback.pojo.Result;
import org.campus.partworkback.pojo.Task;
import org.campus.partworkback.pojo.TaskApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping
    public Result publishTask(@RequestBody Task task, HttpServletRequest request) {
        Long publisherId = (Long) request.getAttribute("currentUserId");
        task.setPublisherId(publisherId);
        TaskPublishDTO publishDTO = new TaskPublishDTO(taskService.addTask(task));
        return Result.success(publishDTO);
    }

    @GetMapping
    public Result getTasks(@RequestParam Integer pageNo,
                           @RequestParam Integer pageSize,
                           @RequestParam String keyWord,
                           @RequestParam(required=false) Integer status,
                           @RequestParam String tag) {
        TaskDTO taskDTO = taskService.getTask(pageNo, pageSize, keyWord, status);
        return Result.success(taskDTO);
    }

    @GetMapping("/{id}")
    public Result getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return Result.success(task);
    }

    @PutMapping("/{id}/cancel")
    public Result cancelTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        taskService.cancelTask(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/apply")
    public Result applyTask(@PathVariable Long id,
                            @RequestBody TaskApplication taskApplication,
                            HttpServletRequest request) {
        taskService.addApplication(id, taskApplication.getMessage(), (Long) request.getAttribute("currentUserId"));
        return Result.success();
    }

    @GetMapping("/{id}/application")
    public Result getTaskApplication(@PathVariable Long id) {
        List<TaskApplicationDTO> list = taskService.getTaskApplication(id);
        return Result.success(list);
    }

    @PutMapping("/{id}/choose")
    public Result chooseApplicant(@PathVariable Long id, @RequestBody Map<String, Long> requestBody) {
        Long applicationId = requestBody.get("applicationId");
        taskService.updateTaskById(id, applicationId);
        return Result.success();
    }

    @PutMapping("/{id}/finish")
    public Result finishTask(@PathVariable Long id){
        taskService.updateTaskFinish(id);
        return Result.success();
    }

    @PutMapping("/{id}/withdraw")
    public Result withdrawApplication(@PathVariable Long id,
                                      // @RequestBody Map<String, Long> requestBody,
                                      HttpServletRequest request){
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        taskService.withdrawById(id, currentUserId);
        return Result.success();
    }

    @GetMapping("/mine")
    public Result getMine(HttpServletRequest request, @RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        TaskDTO taskDTO = taskService.getTaskByPublisherId(currentUserId, pageNo, pageSize);
        return Result.success(taskDTO);
    }

    @GetMapping("in-progress")
    public Result getTaskInProgress(HttpServletRequest request,
                                    @RequestParam Integer pageNo,
                                    @RequestParam Integer pageSize) {
        Long userId = (Long) request.getAttribute("currentUserId");
        TaskDTO taskDTO = taskService.getTaskInProgress(userId, pageNo, pageSize);
        return Result.success(taskDTO);
    }

    @PutMapping("/{id}/accept")
    public Result acceptTask(@PathVariable Long id){
        taskService.acceptTask(id);
        return Result.success();
    }

    @GetMapping("/accepted")
    public Result getTaskAccepted(HttpServletRequest request,
                                  @RequestParam Integer pageNo,
                                  @RequestParam Integer pageSize) {
        Long userId = (Long) request.getAttribute("currentUserId");
        TaskDTO taskDTO = taskService.getTaskAccepted(userId, pageNo, pageSize);
        return Result.success(taskDTO);
    }

    @PutMapping("/{id}/apply-finish")
    public Result applyFinish(HttpServletRequest request, @PathVariable Long id){
        Long userId = (Long) request.getAttribute("currentUserId");
        taskService.applyFinish(userId, id);
        return Result.success();
    }

    @GetMapping("/my-applied-ids")
    public Result getChangeButton(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("currentUserId");
        List<Long> list = taskService.getChangeButton(userId);
        return Result.success(list);
    }

    @GetMapping("/finish-requests")
    public Result getFinishRequest(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("currentUserId");
        List<RequestDTO> list = taskService.getApplyRequestById(userId);
        return Result.success(list);
    }
}
