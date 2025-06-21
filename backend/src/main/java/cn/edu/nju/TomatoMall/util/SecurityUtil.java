package cn.edu.nju.TomatoMall.util;

import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.UserRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class SecurityUtil {

    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000; // 1 day
    private static final Logger logger = Logger.getLogger(SecurityUtil.class.getName());

    @Autowired
    UserRepository userRepository;

    @Autowired
    HttpServletRequest httpServletRequest;

    /**
     * 获取当前用户，支持JWT Bearer Token和Cookie两种方式
     * 优先级：1. Session中的用户 2. Authorization头中的Bearer Token 3. Cookie中的Token
     */
    public User getCurrentUser() {
        // 优先从Session中获取用户（如果拦截器已经设置）
        User sessionUser = (User) httpServletRequest.getSession().getAttribute("currentUser");
        if (sessionUser != null) {
            return sessionUser;
        }

        // 从Authorization头获取Bearer Token
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            User user = getUser(token);
            if (user != null) {
                // 将用户信息缓存到Session中
                httpServletRequest.getSession().setAttribute("currentUser", user);
                return user;
            }
        }

        // 从Cookie中获取Token（向后兼容）
        Cookie tokenCookie = getTokenCookie();
        if (tokenCookie != null) {
            User user = getUser(tokenCookie.getValue());
            if (user != null) {
                // 将用户信息缓存到Session中
                httpServletRequest.getSession().setAttribute("currentUser", user);
                return user;
            }
        }

        return null;
    }

    /**
     * 从Token中获取User信息
     */
    @Cacheable(value = "users", key = "#token")
    public User getUser(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return null;
            }

            // 验证token是否有效
            if (!verifyToken(token)) {
                return null;
            }

            // 从JWT中提取用户ID
            Integer userId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
            Optional<User> userOptional = userRepository.findById(userId);

            User user = userOptional.orElse(null);
            if (user != null) {
                logger.info("Successfully retrieved user: " + user.getUsername() + " (ID: " + user.getId() + ")");
            } else {
                logger.warning("User not found for ID: " + userId);
            }

            return user;
        } catch (Exception e) {
            logger.warning("Failed to get user from token: " + e.getMessage());
            return null;
        }
    }

    /**
     * 生成Cookie并设置相关属性
     */
    public Cookie getCookie(String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/"); // 设置Cookie适用的路径
        cookie.setHttpOnly(true); // 防止JavaScript访问
        cookie.setMaxAge(60 * 60 * 24); // 设置过期时间为24小时
        return cookie;
    }

    /**
     * 从请求中获取token Cookie
     */
    private Cookie getTokenCookie() {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * 获取当前请求的Token（从Authorization头或Cookie）
     */
    public String getCurrentToken() {
        // 优先从Authorization头获取
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 从Cookie获取
        Cookie tokenCookie = getTokenCookie();
        if (tokenCookie != null) {
            return tokenCookie.getValue();
        }

        return null;
    }

    /**
     * 根据用户生成JWT token
     */
    public String getToken(User user) {
        try {
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME); // 设置过期时间
            String token = JWT.create()
                    .withAudience(String.valueOf(user.getId())) // 将用户ID存储在JWT的受众字段中
                    .withExpiresAt(date) // 设置过期时间
                    .withIssuedAt(new Date()) // 设置签发时间
                    .sign(Algorithm.HMAC256(user.getPassword())); // 使用用户密码的哈希值作为签名

            logger.info("Generated token for user: " + user.getUsername() + " (ID: " + user.getId() + ")");
            return token;
        } catch (Exception e) {
            logger.severe("Failed to generate token for user: " + user.getUsername() + ", error: " + e.getMessage());
            throw new RuntimeException("Token generation failed", e);
        }
    }

    /**
     * 验证token是否有效
     */
    public boolean verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.warning("Token is null or empty");
            return false;
        }

        try {
            // 从JWT中提取用户ID
            Integer userId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                logger.warning("User not found for token verification, userId: " + userId);
                return false; // 用户不存在
            }
            User user = userOptional.get();

            // 创建JWT验证器
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
            jwtVerifier.verify(token); // 验证JWT

            logger.fine("Token verification successful for user: " + user.getUsername());
            return true;
        } catch (JWTVerificationException e) {
            logger.warning("JWT verification failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            logger.warning("Invalid user ID in token: " + e.getMessage());
        } catch (Exception e) {
            logger.warning("Token validation failed: " + e.getMessage());
        }
        return false; // 验证失败
    }

    /**
     * 验证Cookie中的token是否有效
     */
    public boolean verifyCookie(Cookie cookie) {
        return cookie != null && verifyToken(cookie.getValue());
    }

    /**
     * 清除当前用户的Session信息
     */
    public void clearCurrentUser() {
        httpServletRequest.getSession().removeAttribute("currentUser");
    }

    /**
     * 获取当前用户ID（便捷方法）
     */
    public Integer getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * 检查当前用户是否已登录
     */
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * 检查当前用户是否有指定角色
     */
    public boolean hasRole(String role) {
        User currentUser = getCurrentUser();
        return currentUser != null && role.equals(currentUser.getRole());
    }
}