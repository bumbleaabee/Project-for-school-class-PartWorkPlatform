package org.campus.partworkback.Service.impl;

import org.campus.partworkback.DTO.FileDTO;
import org.campus.partworkback.DTO.PasswordDTO;
import org.campus.partworkback.DTO.TokenDTO;
import org.campus.partworkback.Service.LoginService;
import org.campus.partworkback.constant.HttpCode;
import org.campus.partworkback.exception.BizException;
import org.campus.partworkback.mapper.LoginMapper;
import org.campus.partworkback.mapper.UserMapper;
import org.campus.partworkback.pojo.Account;
import org.campus.partworkback.pojo.User;
import org.campus.partworkback.utils.CaptchaUtil;
import org.campus.partworkback.utils.CheckUtil;
import org.campus.partworkback.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CheckUtil checkUtil;

    @Autowired
    private CaptchaUtil captchaUtil;

    @Override
    public Long getAccount(Account account) {
        return loginMapper.getAccount(account);
    }

    @Override
    public FileDTO getAvatar(Long userId) {
        String url = loginMapper.getAvatar(userId);
        FileDTO fileDTO = new FileDTO(url);
        return fileDTO;
    }

    @Override
    public TokenDTO addAccount(Account account, String email) {
        String username = account.getUsername();

        List<String> list = loginMapper.getAllUsername();

        boolean flag = checkUtil.checkUsername(username, list);

        if(flag){
            throw new BizException(HttpCode.PARAM_ERROR.getCode(), "用户已存在");
        }

        User user = new User();
        user.setNickname(username);
        user.setEmail(email);
        userMapper.addUser(user);

        account.setId(user.getId());
        loginMapper.addAccount(account);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", account.getUsername()); // 标准主题字段（建议保留）
        claims.put("userId", account.getId());    // 用户ID

        String token = jwtTokenUtil.generateToken(claims);

        TokenDTO tokenDTO = new TokenDTO(token);
        return tokenDTO;
    }

    @Override
    public void setPassword(Long userId, PasswordDTO passwordDTO) {
        String oldPassword = loginMapper.getPassword(userId);
        if(!oldPassword.equals(passwordDTO.getOldPassword())) {
            throw new BizException(HttpCode.PARAM_ERROR.getCode(), "密码错误");
        }
        loginMapper.setPassword(userId, passwordDTO.getNewPassword());
    }
}
