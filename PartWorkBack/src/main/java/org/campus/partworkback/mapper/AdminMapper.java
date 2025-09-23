package org.campus.partworkback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.campus.partworkback.pojo.Account;
import org.campus.partworkback.pojo.Task;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {

    List<Object> getTask(Integer offset, Integer pageSize, String type);

    Integer getTaskCnt(String type);

    List<Map<String, Object>> selectAll(String type);

    List<Account> getAllAdmin();

    void addAdmin(Long userId);

    void removeAdmin(Long userId);
}
