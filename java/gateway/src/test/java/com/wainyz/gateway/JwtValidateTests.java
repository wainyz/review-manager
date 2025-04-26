package com.wainyz.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.utils.JwtUtils;
import com.wainyz.gateway.filters.JWTGlobalFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 验证token校验是否有效
 */
@SpringBootTest
@ActiveProfiles("test")
public class JwtValidateTests {
    @Autowired
    JWTGlobalFilter filter;
    @Autowired
    ObjectMapper objectMapper;
    Logger logger = org.slf4j.LoggerFactory.getLogger(JwtValidateTests.class);
    /**
     * 模拟放行请求
     */
    @Test
    public void login() throws Exception {
        // 模拟正常登录请求, 以及公开资源

        //- 创建 HttpServletRequest 的 Mock 对象
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        //- 模拟请求的方法
        when(request.getMethod()).thenReturn("POST");

        //- 模拟请求的 URI
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getServletPath()).thenReturn("/login");

//        //- 模拟请求参数
//        LoginDO loginDO = new LoginDO();
//        loginDO.setEmail("player_simple@163.com");
//        loginDO.setPassword("***REMOVED***");
//        loginDO.setImageCode("1f68");
//        loginDO.setImageId("185033");
//        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(objectMapper.writeValueAsString(loginDO))));

        //- 验证模拟的行为
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getRequestURI()).isEqualTo("/login");

        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request,response, chain);
        //- 检查response是否通过
        assert response.getStatus() != 401;
        //- 模拟请求的 URI
        when(request.getRequestURI()).thenReturn("/public/");
        when(request.getServletPath()).thenReturn("/public/");
        filter.doFilter(request,response, chain);
        assert response.getStatus() != 401;
    }
    /**
     * 模拟token错误的请求
     */
    @Test
    public void errorToken() {
        // 模拟正常登录请求, 以及公开资源

        //- 创建 HttpServletRequest 的 Mock 对象
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);


        //- 模拟请求的方法
        when(request.getMethod()).thenReturn("POST");

        //- 模拟请求的 URI
        when(request.getRequestURI()).thenReturn("/xxx/login/");
        when(request.getServletPath()).thenReturn("/xxx/login/");

        //- token 错误token
        when(request.getHeader(GatewayConsistent.TOKEN_HEADER)).thenReturn("123sopaijfioajsf");

//        //- 模拟请求参数
//        LoginDO loginDO = new LoginDO();
//        loginDO.setEmail("player_simple@163.com");
//        loginDO.setPassword("***REMOVED***");
//        loginDO.setImageCode("1f68");
//        loginDO.setImageId("185033");
//        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(objectMapper.writeValueAsString(loginDO))));

        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request,response, chain);
        //- 检查response是否通过
        logger.info("response.getStatus() = " + response.getStatus());
        // 验证 setStatus 是否被调用且参数为 401
        verify(response, times(1)).setStatus(401);
        //- 模拟请求的 URI
        when(request.getHeader(GatewayConsistent.TOKEN_HEADER)).thenReturn(null);
        filter.doFilter(request,response, chain);
        verify(response,times(2)).setStatus(401);
    }
    /**
     * 正常token请求
     */
    @Autowired
    JwtUtils jwtUtils;
    @Test
    public void normalToken() {
        // 模拟正常登录请求, 以及公开资源

        //- 创建 HttpServletRequest 的 Mock 对象
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);


        //- 模拟请求的方法
        when(request.getMethod()).thenReturn("POST");

        //- 模拟请求的 URI
        when(request.getRequestURI()).thenReturn("/xxx/login/");
        when(request.getServletPath()).thenReturn("/xxx/login/");

        //- token 正确token
        Map<String,String> testData = Map.of(
                GatewayConsistent.USER_ID,"1",
                GatewayConsistent.USER_EMAIL,"player_simple@163.com",
                GatewayConsistent.USER_NAME,"player_simple"
        );
        Claims claims = new DefaultClaims(testData);
        when(request.getHeader(GatewayConsistent.TOKEN_HEADER)).thenReturn(
                jwtUtils.generateToken(claims)
        );
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request,response, chain);
        verify(request,times(1)).setAttribute(GatewayConsistent.USER_ID,"1");
    }
}
