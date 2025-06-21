package cn.edu.nju.TomatoMall.configure;

import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final SecurityUtil securityUtil;

    private static final Map<String, String> WHITELIST = new HashMap<>();
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    static {
        WHITELIST.put("/api/users/register", "POST");
        WHITELIST.put("/api/users/login", "POST");
        WHITELIST.put("/api/accounts/login", "POST");
        WHITELIST.put("/api/accounts/register", "POST");
        WHITELIST.put("/api/stores", "GET");
        WHITELIST.put("/api/stores/{storeId}", "GET");
        WHITELIST.put("/api/products", "GET");
        WHITELIST.put("/api/products/store/{storeId}", "GET");
        WHITELIST.put("/api/products/{productId}", "GET");
        WHITELIST.put("/api/search/products", "GET");
        WHITELIST.put("/api/search/stores", "GET");
        WHITELIST.put("/api/alipay/notify", "POST");
        WHITELIST.put("/api/shipping/{trackingNo}/update", "POST");
        WHITELIST.put("/api/shipping/{trackingNo}/confirm-delivery", "POST");
    }

    /**
     * 检查路径和方法是否在白名单中
     *
     * @param path   请求路径
     * @param method HTTP方法
     * @return 是否在白名单中
     */
    private boolean isWhitelisted(String path, String method) {
        // 移除查询参数
        String cleanPath = path.split("\\?")[0];

        // 遍历白名单进行匹配
        return WHITELIST.entrySet().stream().anyMatch(entry -> {
            String whitelistPath = entry.getKey();
            String whitelistMethod = entry.getValue();

            // 方法必须匹配（忽略大小写）
            if (!whitelistMethod.equalsIgnoreCase(method)) {
                return false;
            }

            // 处理路径参数的情况
            if (whitelistPath.contains("{")) {
                return pathMatcher.match(whitelistPath, cleanPath);
            } else {
                // 精确匹配
                return whitelistPath.equals(cleanPath);
            }
        });
    }

    public LoginInterceptor(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getRequestURI().startsWith("/error")) {
            throw TomatoMallException.pathOrParamError();
        }

        if (isWhitelisted(request.getRequestURI(), request.getMethod())) {
            return true;
        }

        // 使用Authorization进行身份验证
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (securityUtil.verifyToken(token)) {
                request.getSession().setAttribute("currentUser", securityUtil.getUser(token));
                return true;
            }
        }

        // 使用Cookie进行身份验证
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    if (securityUtil.verifyCookie(cookie)) {
                        request.getSession().setAttribute("currentUser", securityUtil.getUser(cookie.getValue()));
                        return true;
                    }
                }
            }
        }

        throw TomatoMallException.notLogin(request.getServletPath());
    }

}
