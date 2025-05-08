package cn.edu.nju.TomatoMall.configure;

import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final SecurityUtil securityUtil;

    private static final Map<String, String> WHITELIST = new HashMap<>();

    static {
        WHITELIST.put("/api/users/register", "POST");
        WHITELIST.put("/api/users/login", "POST");
        WHITELIST.put("/api/accounts/login", "POST");
        WHITELIST.put("/api/accounts/register", "POST");
        WHITELIST.put("/api/alipay/notify", "POST");
        WHITELIST.put("/api/shipping", "POST");
    }

    private boolean isWhitelisted(String path, String method) {
        return WHITELIST.entrySet().stream().anyMatch(e -> e.getKey().startsWith(path) && e.getValue().equals(method));
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

        // 使用token进行身份验证
        String token = request.getHeader("token");
        if (token != null && securityUtil.verifyToken(token)) {
            request.getSession().setAttribute("currentUser", securityUtil.getUser(token));
            return true;
        }

        throw TomatoMallException.notLogin(request.getServletPath());
    }

}
