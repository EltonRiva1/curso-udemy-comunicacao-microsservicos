package br.com.cursoudemy.productapi.config.interceptor;

import br.com.cursoudemy.productapi.config.exception.ValidationException;
import br.com.cursoudemy.productapi.modules.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

import static org.springframework.util.ObjectUtils.isEmpty;

@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private static final String AUTHORIZATION = "Authorization", TRANSACTION_ID = "transactionid";
    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) {
        if (this.isOptions(httpServletRequest) || this.isPublicUrl(httpServletRequest.getRequestURI())) {
            return true;
        }
        if (isEmpty(httpServletRequest.getHeader(TRANSACTION_ID))) {
            throw new ValidationException("The transactionid header is required.");
        }
        var authorization = httpServletRequest.getHeader(AUTHORIZATION);
        this.jwtService.validateAuthorization(authorization);
        httpServletRequest.setAttribute("serviceid", UUID.randomUUID().toString());
        return true;
    }

    private boolean isPublicUrl(String url) {
        return Urls.PROTECTED_URLS.stream().noneMatch(url::contains);
    }

    private boolean isOptions(HttpServletRequest httpServletRequest) {
        return HttpMethod.OPTIONS.name().equals(httpServletRequest.getMethod());
    }
}
