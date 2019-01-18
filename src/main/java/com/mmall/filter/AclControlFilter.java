package com.mmall.filter;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.mmall.common.ApplicationContextHelper;
import com.mmall.common.JsonData;
import com.mmall.common.RequestHolder;
import com.mmall.model.SysUser;
import com.mmall.service.SysCoreService;
import com.mmall.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.StringHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class AclControlFilter implements Filter {

    private static Set<String> exclusionUrlSet = Sets.newConcurrentHashSet();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String exclusionUrls = filterConfig.getInitParameter("exclusionUrls");
        List<String> exclusionUrlList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(exclusionUrls);
        exclusionUrlSet = Sets.newConcurrentHashSet(exclusionUrlList);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        String servletPath = request.getServletPath();
        Map requestMap = request.getParameterMap();

        if(exclusionUrlSet.contains(servletPath)){
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }

        SysUser sysUser = RequestHolder.getCurrentUser();
        if(sysUser == null){
            log.info("someone visit {}, but no login, parameter:{}",servletPath, JsonMapper.obj2String(requestMap));
            noAuth(request,response);
            return;
        }
        //这个filter不是Spring管理的
        SysCoreService sysCoreService = ApplicationContextHelper.popBean(SysCoreService.class);
        if(!sysCoreService.hasUrlAcl(servletPath)){
            log.info("{} visit {}, but no login, parameter:{}",JsonMapper.obj2String(sysUser),servletPath, JsonMapper.obj2String(requestMap));
            noAuth(request,response);
            return;
        }

        filterChain.doFilter(servletRequest,servletResponse);
        return;
    }

    private void noAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /*String servletPath = request.getServletPath();
        if (servletPath.endsWith(".json")) {
            JsonData jsonData = JsonData.fail("没有访问权限，如需要访问，请联系管理员");
            response.setHeader("Content-Type", "application/json");
            response.getWriter().print(JsonMapper.obj2String(jsonData));
            return;
        } else {
            clientRedirect(noAuthUrl, response);
            return;
        }*/
    }

    @Override
    public void destroy() {

    }
}
