package stoner.tstomp.security;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import static stoner.tstomp.service.impl.IndexServiceImpl.KICKOUT_KEY;

@Slf4j
@Data
public class KickoutSessionFilter extends AccessControlFilter {
    //静态注入
    static String kickoutUrl;

    @Override
    protected boolean isAccessAllowed(ServletRequest request,
                                      ServletResponse response, Object mappedValue) throws Exception {

        HttpServletRequest httpRequest = ((HttpServletRequest)request);
        String url = httpRequest.getRequestURI();
        Subject subject = getSubject(request, response);
        //如果是相关目录 or 如果没有登录 就直接return true
        if(url.startsWith("/open/") || (!subject.isAuthenticated() && !subject.isRemembered())){
            return Boolean.TRUE;
        }
        Session session = subject.getSession();
        Boolean marker = (Boolean)session.getAttribute(KICKOUT_KEY);
        return  null == marker || !marker;
    }
    @Override
    protected boolean onAccessDenied(ServletRequest request,
                                     ServletResponse response) throws Exception {

        //退出
        Subject subject = getSubject(request, response);
        subject.logout();
        WebUtils.getSavedRequest(request);
        return false;
    }
}
