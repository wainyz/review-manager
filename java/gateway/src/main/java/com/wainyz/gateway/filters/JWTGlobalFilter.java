package com.wainyz.gateway.filters;

import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.exception.MyJwtException;
import com.wainyz.commons.utils.JwtUtils;
import com.wainyz.core.config.WebSocketConfig;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import static com.wainyz.commons.config.UrlConfiguration.PUBLIC_PREFIX;

/**
 * 校验所有的请求，放行public 的请求
 * @author Yanion_gwgzh
 */
@Component
public class JWTGlobalFilter implements Filter {
    public static final Logger LOGGER = LoggerFactory.getLogger(JWTGlobalFilter.class);
    private final JwtUtils jwtUtils;
    public JWTGlobalFilter(@Autowired JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        try{
            // 获取请求头中的token
            HttpServletRequest request = ((HttpServletRequest) servletRequest);
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            //  如果是放行公开资源 OPTIONS 以及 登录 注册 图片验证码
            if (request.getServletPath().startsWith(PUBLIC_PREFIX)
                    || HttpMethod.OPTIONS.name().equals(request.getMethod())
                    || request.getServletPath().startsWith(WebSocketConfig.APP_PREFIX)
                    || request.getServletPath().startsWith(WebSocketConfig.WEBSOCKET_URI)

            ) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            String token = request.getHeader(GatewayConsistent.TOKEN_HEADER);
            // 开始检验token
            if (token == null || token.isEmpty()) {
                response.setContentType("application/json");
                response.setStatus(401);
                response.getWriter().print("缺少token");
                return;
            }
            Claims claims = jwtUtils.validate(token);
            if (claims != null) {
                //验证通过，设置用户信息
                request.setAttribute(GatewayConsistent.USER_NAME, claims.get(GatewayConsistent.USER_NAME));
                request.setAttribute(GatewayConsistent.USER_ID, claims.get(GatewayConsistent.USER_ID));
                request.setAttribute(GatewayConsistent.USER_EMAIL, claims.get(GatewayConsistent.USER_EMAIL));
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            // token无效
            response.setStatus(401);

        }catch (MyJwtException e){
            LOGGER.info("[62]token invalidate: {}", e.getMessage());
            ((HttpServletResponse) servletResponse).setStatus(401);
        }
        catch (Exception e){
            LOGGER.warn("[32]JWTGlobalFilter error, servlet request to http servlet request fail: ", e);
            ((HttpServletResponse) servletResponse).setStatus(401);
        }
    }
    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
