package com.example.myself_resume_analyzer.interceptor;

import com.example.myself_resume_analyzer.utils.JwTUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Resource
    private JwTUtils jwTUtils;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");

        // 检查是否有 Authorization 头，且以 "Bearer " 开头
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "未登录或token已过期");
            return false;
        }

        // 截取 token（去掉 "Bearer " 前缀，7个字符）
        String token = authorization.substring(7);

        // 验证 token 是否有效（JWT签名和过期时间）
        if (!jwTUtils.validateToken(token)) {
            sendUnauthorizedResponse(response, "token无效或已过期");
            return false;
        }

        // 解析 userId
        Long userId = jwTUtils.getUserIdFromToken(token);

        // 检查 Redis 中 token 是否存在（是否被登出）
        String storedToken = (String) redisTemplate.opsForValue().get("user:token:" + userId);
        if (storedToken == null || !storedToken.equals(token)) {
            sendUnauthorizedResponse(response, "已登出或token已失效");
            return false;
        }

        // 将 userId 存入 request，供后续 Controller 使用
        request.setAttribute("userId", userId);

        return true;
    }

    /**
     * 返回 401 未授权的 JSON 响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
