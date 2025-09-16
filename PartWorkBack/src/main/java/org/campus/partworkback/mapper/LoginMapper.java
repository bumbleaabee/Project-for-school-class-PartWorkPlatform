package org.campus.partworkback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.campus.partworkback.pojo.Account;

import java.util.List;

@Mapper
public interface LoginMapper {

    Long getAccount(Account account);

    String getAvatar(Long userId);

    void addAccount(Account account);

    List<String> getAllUsername();

    String getPassword(Long userId);

    void setPassword(Long userId, String newPassword);
}
