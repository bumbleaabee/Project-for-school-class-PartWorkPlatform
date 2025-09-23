package org.campus.partworkback.Service.impl;

import org.campus.partworkback.DTO.TableDTO;
import org.campus.partworkback.DTO.TaskDTO;
import org.campus.partworkback.Service.AdminService;
import org.campus.partworkback.mapper.AdminMapper;
import org.campus.partworkback.pojo.Account;
import org.campus.partworkback.pojo.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminMapper adminMapper;

    @Override
    public TableDTO getTask(Integer pageNo, Integer pageSize, String type) {
        List<Object> list = adminMapper.getTask((pageNo - 1) * pageSize, pageSize, type);
        Integer total = adminMapper.getTaskCnt(type);
        TableDTO tableDTO = new TableDTO(list, total, pageNo, pageSize);
        return tableDTO;
    }

    @Override
    public List<Map<String, Object>> selectAll(String type) {
        return adminMapper.selectAll(type);
    }

    @Override
    public List<Account> getAllAdmin() {
        return adminMapper.getAllAdmin();
    }

    @Override
    public void addAdmin(Long userId) {
        adminMapper.addAdmin(userId);
    }

    @Override
    public void removeAdmin(Long userId) {
        adminMapper.removeAdmin(userId);
    }
}
