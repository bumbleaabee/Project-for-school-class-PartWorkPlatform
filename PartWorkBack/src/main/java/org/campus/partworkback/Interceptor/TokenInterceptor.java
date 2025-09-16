package org.campus.partworkback.Interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.campus.partworkback.constant.HttpCode;
import org.campus.partworkback.pojo.Result;
import org.campus.partworkback.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        /*
        * ApiResponse<Object> apiResponse = new ApiResponse<>(401, "令牌已过期，请重新登录", null);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(apiResponse);

        response.getWriter().write(json);
        * */

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(Result.error(HttpCode.UNAUTHORIZED.getCode(), "Unauthorized"));

            response.getWriter().write(json);
            return false;
        }

        String token = authHeader.substring(7);
        // 4. 验证Token有效性
        if (!jwtTokenUtil.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(Result.error(HttpCode.FORBIDDEN.getCode(), "TOKEN_INVALID"));

            response.getWriter().write(json);
            return false;
        }

        // 5. 解析Token中的用户信息
        Claims claims = jwtTokenUtil.getClaimsFromToken(token);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        Long userId = claims.get("userId", Long.class);
        log.info("username is {} and userId is {}", username, userId);

        // 6. 将用户信息存入请求属性，供后续使用
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUsername", username);
        request.setAttribute("userClaims", claims);

        return true; // 验证通过，继续执行
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
