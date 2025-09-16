package org.campus.partworkback.Service;

import org.campus.partworkback.DTO.FileDTO;
import org.campus.partworkback.DTO.PasswordDTO;
import org.campus.partworkback.DTO.TokenDTO;
import org.campus.partworkback.pojo.Account;

public interface LoginService {
    Long getAccount(Account account);

    FileDTO getAvatar(Long userId);

    TokenDTO addAccount(Account account, String email);

    void setPassword(Long userId, PasswordDTO passwordDTO);
}
