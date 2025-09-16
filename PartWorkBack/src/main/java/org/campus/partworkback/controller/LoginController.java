package org.campus.partworkback.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.campus.partworkback.DTO.AccountDTO;
import org.campus.partworkback.DTO.FileDTO;
import org.campus.partworkback.DTO.PasswordDTO;
import org.campus.partworkback.DTO.TokenDTO;
import org.campus.partworkback.Service.LoginService;
import org.campus.partworkback.Service.UserService;
import org.campus.partworkback.constant.HttpCode;
import org.campus.partworkback.pojo.Account;
import org.campus.partworkback.pojo.CaptchaInfo;
import org.campus.partworkback.pojo.Result;
import org.campus.partworkback.pojo.User;
import org.campus.partworkback.utils.CaptchaUtil;
import org.campus.partworkback.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class LoginController {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaUtil captchaUtil;

    @PostMapping("/login")
    public Result login(@RequestBody AccountDTO accountDTO) {
        String captchaId = accountDTO.getCaptchaId();
        String captchaCode = accountDTO.getCaptchaCode();

        if (!captchaUtil.validateCaptcha(captchaId, captchaCode)) {
            return Result.error(HttpCode.LOGIN_FAILED.getCode(), "验证码错误或已失效");
        }

        Account account = new Account();
        account.setUsername(accountDTO.getUsername());
        account.setPassword(accountDTO.getPassword());

        Long id = loginService.getAccount(account);
        account.setId(id);

        if(id == null) {
            return Result.error(HttpCode.LOGIN_FAILED.getCode(), "login failed");
        }


        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", account.getUsername()); // 标准主题字段（建议保留）
        claims.put("userId", account.getId());    // 用户ID

        String token = jwtTokenUtil.generateToken(claims);

        TokenDTO tokenDTO = new TokenDTO(token);
        return Result.success(tokenDTO);
    }

    @GetMapping("/me")
    public Result me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
//        String username = (String) request.getAttribute("currentUsername");
//        Claims claims = (Claims) request.getAttribute("userClaims");
        if(userId == null) {
            return Result.error(HttpCode.LOGIN_FAILED.getCode(), "me failed");
        }
        log.info("current user id is {}", userId);
        User user = userService.getById(userId);
        return Result.success(user);
    }

    @GetMapping("/profile")
    public Result getAvatar(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("currentUserId");
        FileDTO fileDTO = loginService.getAvatar(userId);
        return Result.success(fileDTO);
    }

    @PostMapping("/register")
    public Result register(@RequestBody AccountDTO accountDTO) {
        String captchaId = accountDTO.getCaptchaId();
        String captchaCode = accountDTO.getCaptchaCode();

        if (!captchaUtil.validateCaptcha(captchaId, captchaCode)) {
            return Result.error(2001, "验证码错误或已失效");
        }

        Account account = new Account();
        account.setUsername(accountDTO.getUsername());
        account.setPassword(accountDTO.getPassword());
        TokenDTO tokenDTO = loginService.addAccount(account, accountDTO.getEmail());
        return Result.success(tokenDTO);
    }

    @GetMapping("/captcha/image")
    public Result getCaptcha(){
        CaptchaInfo info = captchaUtil.generateAndStoreCaptcha(4, 5 * 60 * 1000);
        // 返回格式：{captchaId, imageBase64}
        return Result.success(Map.of("captchaId", info.getCaptchaId(), "imageBase64", info.getImageBase64()));
    }

    @PutMapping("/password")
    public Result updatePassword(HttpServletRequest request, @RequestBody PasswordDTO passwordDTO) {
        Long userId = (Long) request.getAttribute("currentUserId");
        loginService.setPassword(userId, passwordDTO);
        return Result.success();
    }
}
