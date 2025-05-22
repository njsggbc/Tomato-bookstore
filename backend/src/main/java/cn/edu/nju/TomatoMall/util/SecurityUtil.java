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
     * 获取当前用户，使用JWT而非HttpSession
     * 使用缓存来减少数据库查询
     */
    public User getCurrentUser() {
        Cookie tokenCookie = getTokenCookie();
        if (tokenCookie == null) {
            return null;
        }
        return getUser(tokenCookie.getValue());
    }

    /**
     * 从Cookie中获取User信息
     * 使用缓存来提高效率
     */
    @Cacheable(value = "users", key = "#cookie.value")
    public User getUser(String token) {
        Integer userId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElse(null); // 如果用户不存在返回null
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
     * 根据用户生成JWT token
     */
    public String getToken(User user) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME); // 设置过期时间
        return JWT.create()
                .withAudience(String.valueOf(user.getId())) // 将用户ID存储在JWT的受众字段中
                .withExpiresAt(date) // 设置过期时间
                .sign(Algorithm.HMAC256(user.getPassword())); // 使用用户密码的哈希值作为签名
    }

    /**
     * 验证token是否有效
     */
    public boolean verifyToken(String token) {
        if (token == null) {
            return false;
        }

        try {
            // 从JWT中提取用户ID
            Integer userId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                return false; // 用户不存在
            }
            User user = userOptional.get();

            // 创建JWT验证器
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
            jwtVerifier.verify(token); // 验证JWT
            return true;
        } catch (JWTVerificationException e) {
            logger.warning("JWT verification failed: " + e.getMessage());
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
}
