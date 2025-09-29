package org.campus.partworkback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.campus.partworkback.DTO.FileDTO;
import org.campus.partworkback.Service.ResumeService;
import org.campus.partworkback.constant.HttpCode;
import org.campus.partworkback.exception.FileException;
import org.campus.partworkback.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Transactional
@RestController
@RequestMapping("/api/resume")
public class ResumeController {
    @Autowired
    private ResumeService resumeService;

    /*
    * CREATE TABLE resume (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    file_path VARCHAR(256) NOT NULL,
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES user(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
    * */

    @PostMapping("/upload")
    public Result uploadResume(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        if(file.isEmpty()) {
            throw new FileException(HttpCode.PARAM_ERROR.getCode(), "文件为空");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new FileException(HttpCode.PARAM_ERROR.getCode(), "请上传pdf文件");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = "u2_" + System.currentTimeMillis() + extension;

        String projectPath = System.getProperty("user.dir"); // 获取项目根目录
        String savePath = projectPath + File.separator + "uploads" + File.separator + "resume";
        File dir = new File(savePath);
        if (!dir.exists()) {
            dir.mkdirs(); // 如果目录不存在则创建
        }

        File destFile = new File(dir, fileName);
        file.transferTo(destFile);

        String relativePath = "/uploads/resume/" + fileName;
        // AvatarDTO avatarDTO = new AvatarDTO(relativePath);
        FileDTO fileDTO = new FileDTO(relativePath);

        Long userId = (Long) request.getAttribute("currentUserId");
        // userService.addAvatarUrl(userId, relativePath);
        // return Result.success(avatarDTO);
        resumeService.addResumeUrl(userId, relativePath);
        return Result.success(fileDTO);
    }

    /*
    * @PutMapping("/avatar")
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
        AvatarDTO avatarDTO = new AvatarDTO(relativePath);

        Long userId = (Long) request.getAttribute("currentUserId");
        userService.addAvatarUrl(userId, relativePath);
        return Result.success(avatarDTO);
    }
    * */

    @GetMapping("/{userId}")
    public Result getResume(@PathVariable Long userId) {
        FileDTO fileDTO = resumeService.getResumeUrl(userId);
        return Result.success(fileDTO);
    }
}
