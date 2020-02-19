package stoner.tstomp.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.dao.DataAccessException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截requestMapping方法 可进行参数绑定等
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        //防sql注入 参数处理
        binder.registerCustomEditor(String.class, new StringEscapeEditor(true));
    }

    @ExceptionHandler(value = AuthorizationException.class)
    @ResponseBody
    public String handler(AuthorizationException e) {
        log.error("没有通过权限验证:{}", e.getMessage());
        return "没有通过权限验证";
    }

    @ExceptionHandler(value = AuthenticationException.class)
    @ResponseBody
    public String handler(AuthenticationException e) {
        log.error("没有通过认证: {}", e.getMessage());
        return "用户名或密码错误";
    }

    @ExceptionHandler(value = UnknownAccountException.class)
    @ResponseBody
    public String handler(UnknownAccountException e) {
        log.error("用户名错误，没有通过认证: {}", e.getMessage());
        return "用户名或密码错误";
    }

    @ExceptionHandler(value = IncorrectCredentialsException.class)
    @ResponseBody
    public String handler(IncorrectCredentialsException e) {
        log.error("密码错误，没有通过认证: {}", e.getMessage());
        return "用户名或密码错误";
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public String handler (HttpServletRequest request, Exception e) {
        log.error("error uri: {}", request.getRequestURI());
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(DataAccessException.class)
    public String handSql(HttpServletRequest request, DataAccessException e){
        log.error("error uri: {}", request.getRequestURI());
        log.error("SQL Exception {}", e.getMessage());
        return "SQL异常";
    }

    @ResponseBody
    @ExceptionHandler(value = ServiceException.class)
    public String handler (HttpServletRequest request, ServiceException e) {
        //ServiceException 不抛出 不打印日志
        log.error("error uri: {}", request.getRequestURI());
        log.error("service Exception {}", e.getMessage());
        return e.getMessage();
    }
}
