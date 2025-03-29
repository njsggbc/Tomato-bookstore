package cn.edu.nju.TomatoMall.configure;

import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final SecurityUtil securityUtil;

    public LoginInterceptor(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // HACK: 兼容测试用
        if (request.getServletPath().startsWith("/api/accounts/login") ||
                (request.getServletPath().startsWith("/api/accounts") && request.getMethod().equals("POST")))
        {
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
