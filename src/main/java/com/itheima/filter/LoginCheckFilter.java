package com.itheima.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        String[] urls = {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/**",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };
        boolean check = check(urls, requestURI);
        if (check) {
            filterChain.doFilter(request, response);
            //log.info("端口--->" + requestURI);
            return;
        }
        if (request.getSession().getAttribute("employee") != null) {
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setId(empId);
            filterChain.doFilter(request, response);
            return;
        }
        log.info("id----->" + request.getSession().getAttribute(("user")));
        if (request.getSession().getAttribute("user") != null) {
            Long useerId = (Long) request.getSession().getAttribute("user");
            BaseContext.setId(useerId);
            filterChain.doFilter(request, response);
            return;
        }
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) return true;
        }
        //log.info("错误---" + requestURI);
        return false;
    }
}
