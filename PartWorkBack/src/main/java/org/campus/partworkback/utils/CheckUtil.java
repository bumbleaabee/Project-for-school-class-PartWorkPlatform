package org.campus.partworkback.utils;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CheckUtil {
    public boolean checkUsername(String username, List<String> list) {
        for (String s : list) {
            if (username.equals(s)) {
                return true;
            }
        }
        return false;
    }
}
