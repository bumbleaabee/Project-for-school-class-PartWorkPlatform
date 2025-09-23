package org.campus.partworkback.Service.impl;

import org.campus.partworkback.DTO.MatchDTO;
import org.campus.partworkback.Service.AIService;
import org.campus.partworkback.VO.TaskVO;
import org.campus.partworkback.mapper.AIMapper;
import org.campus.partworkback.mapper.TaskMapper;
import org.campus.partworkback.mapper.UserMapper;
import org.campus.partworkback.utils.AIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AIServiceImpl implements AIService {
    @Autowired
    private AIUtil aiUtil;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AIMapper aiMapper;

    @Override
    public List<MatchDTO> matchTask(Long userId) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("现在他会的技能是：\n");

        String description = userMapper.getDescription(userId);
        promptBuilder.append(description);
        promptBuilder.append('\n');

        promptBuilder.append("任务列表：\n");

        List<TaskVO> taskList = taskMapper.getTaskList(userId);
        for (TaskVO taskVO : taskList) {
            promptBuilder.append(taskVO.toString());
        }
        String prompt = promptBuilder.toString();

        // System.out.println(prompt);

        String ans = aiUtil.chat(prompt);
        System.out.println(ans);

        int[] arr = Arrays.stream(ans.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

        if(arr.length == 1 && arr[0] == -1) {
            return new ArrayList<MatchDTO>();
        }

        List<MatchDTO> list = aiMapper.getMatchTask(arr);
        return list;
    }

    /*
    * public Flux<String> chat(String prompt){
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
    * */
}
