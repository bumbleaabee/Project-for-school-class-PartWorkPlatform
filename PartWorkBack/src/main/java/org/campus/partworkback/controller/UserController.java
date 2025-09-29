package org.campus.partworkback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.campus.partworkback.DTO.FileDTO;
import org.campus.partworkback.DTO.ReviewDTO;
import org.campus.partworkback.Service.UserService;
import org.campus.partworkback.constant.HttpCode;
import org.campus.partworkback.exception.FileException;
import org.campus.partworkback.pojo.Result;
import org.campus.partworkback.pojo.User;
import org.campus.partworkback.pojo.UserStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Transactional
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Result findOthers(@PathVariable("id") Long id){
        User user = userService.getById(id);
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result updateUser(@RequestBody User user, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("currentUserId");
        user.setId(userId);
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/skills")
    public Result updateUserSkills(@RequestBody User user, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("currentUserId");
        user.setId(userId);
        userService.updateById(user);
        return Result.success();
    }

    @GetMapping("/{id}/stats")
    public Result getStats(@PathVariable("id") Long id){
        UserStats userStats = userService.getUsById(id);
        return Result.success(userStats);
    }

    @GetMapping("/{id}/reviews")
    public Result getReviews(@RequestParam Integer pageNo, @RequestParam Integer pageSize, @PathVariable("id") Long id){
        ReviewDTO ReviewDTO = userService.getReviewById(pageNo, pageSize, id);
        return Result.success(ReviewDTO);
    }

    @PutMapping("/avatar")
    public Result uploadAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()){
            throw new FileException(HttpCode.PARAM_ERROR.getCode(), "文件为空");
        }
        // generate new file name
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = "u1_" + System.currentTimeMillis() + extension;

        String projectPath = System.getProperty("user.dir"); // 获取项目根目录
        String savePath = projectPath + File.separator + "uploads" + File.separator + "avatar";
        File dir = new File(savePath);
        if (!dir.exists()) {
            dir.mkdirs(); // 如果目录不存在则创建
        }

        File destFile = new File(dir, fileName);
        file.transferTo(destFile);

        String relativePath = "/uploads/avatar/" + fileName;
        FileDTO fileDTO = new FileDTO(relativePath);

        Long userId = (Long) request.getAttribute("currentUserId");
        userService.addAvatarUrl(userId, relativePath);
        return Result.success(fileDTO);
    }
}
