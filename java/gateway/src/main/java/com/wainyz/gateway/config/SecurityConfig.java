package com.wainyz.gateway.config;

//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // 配置 URL 权限
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers(UrlConfiguration.PUBLIC_PREFIX+"**", "/error").permitAll() // 公开访问
//                        .anyRequest().authenticated() // 其他请求需认证
//                )
//                // 表单登录配置
//                .formLogin(form -> form
//                        .loginPage("/login") // 自定义登录页
//                        .loginProcessingUrl(LoginController.LOGIN_URL) // 表单提交路径
//                        .defaultSuccessUrl("/home", true) // 强制跳转到首页
//                        .failureUrl("/login?error=true") // 登录失败页
//                )
//                // 注销配置
//                .logout(logout -> logout
//                        .logoutUrl("/logout") // 登出路径
//                        .logoutSuccessUrl("/login?logout") // 登出成功页
//                        .deleteCookies(GatewayConsistent.TOKEN_HEADER) // 删除 Cookie
//                )
//                // 异常处理
//                .exceptionHandling(exception -> exception
//                        .accessDeniedPage("/403") // 权限不足页
//                )
//                // 禁用 CSRF（仅用于非浏览器客户端）
//                .csrf(AbstractHttpConfigurer::disable);
//
//        return http.build();
//    }
//}