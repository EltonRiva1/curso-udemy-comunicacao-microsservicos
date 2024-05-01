package br.com.cursoudemy.productapi.config.interceptor;

import br.com.cursoudemy.productapi.modules.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {
    private static final String AUTHORIZATION = "Authorization";
    @Autowired
    private JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if (this.isOptions(httpServletRequest)) {
            return true;
        }
        var authorization = httpServletRequest.getHeader(AUTHORIZATION);
        this.jwtService.validateAuthorization(authorization);
        return true;
    }

    private boolean isOptions(HttpServletRequest httpServletRequest) {
        return HttpMethod.OPTIONS.name().equals(httpServletRequest.getMethod());
    }
}
