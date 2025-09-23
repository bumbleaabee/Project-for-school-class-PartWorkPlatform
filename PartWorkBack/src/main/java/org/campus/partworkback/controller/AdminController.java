package org.campus.partworkback.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.campus.partworkback.DTO.TableDTO;
import org.campus.partworkback.Service.AdminService;
import org.campus.partworkback.constant.HttpCode;
import org.campus.partworkback.exception.BizException;
import org.campus.partworkback.pojo.Account;
import org.campus.partworkback.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("/table")
    public Result getTask(@RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestParam String type) {
        TableDTO tableDTO = adminService.getTask(pageNo, pageSize, type);
        return Result.success(tableDTO);
    }

    @GetMapping("/export")
    public void export(@RequestParam String type, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> rows = adminService.selectAll(type);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + type + ".csv");

        PrintWriter writer = response.getWriter();
        if (!rows.isEmpty()) {
            Set<String> headers = rows.get(0).keySet();
            writer.println(String.join(",", headers));
            for (Map<String, Object> row : rows) {
                List<String> values = headers.stream()
                        .map(h -> Optional.ofNullable(row.get(h)).orElse("").toString())
                        .map(v -> "\"" + v.replace("\"", "\"\"") + "\"")
                        .collect(Collectors.toList());
                writer.println(String.join(",", values));
            }
        }
        writer.flush();
    }

    @GetMapping("/list")
    public Result GetAllAdmin(){
        List<Account> list = adminService.getAllAdmin();
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result addAdmin(@RequestBody Map<String, Long> requestMap){
        Long userId = requestMap.get("userId");
        adminService.addAdmin(userId);
        return Result.success();
    }

    @PostMapping("/remove")
    public Result removeAdmin(@RequestBody Map<String, Long> requestMap){
        Long userId = requestMap.get("userId");
        if(userId == 1L){
            throw new BizException(HttpCode.FORBIDDEN.getCode(), HttpCode.FORBIDDEN.getMessage());
        }
        adminService.removeAdmin(userId);
        return Result.success();
    }
}
