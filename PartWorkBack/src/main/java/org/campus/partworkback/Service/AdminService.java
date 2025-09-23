package org.campus.partworkback.Service;

import org.campus.partworkback.DTO.TableDTO;
import org.campus.partworkback.DTO.TaskDTO;
import org.campus.partworkback.pojo.Account;

import java.util.List;
import java.util.Map;

public interface AdminService {
    TableDTO getTask(Integer pageNo, Integer pageSize, String type);

    List<Map<String, Object>> selectAll(String type);

    List<Account> getAllAdmin();

    void addAdmin(Long userId);

    void removeAdmin(Long userId);
}
