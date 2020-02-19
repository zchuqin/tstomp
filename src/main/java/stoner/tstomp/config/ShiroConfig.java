package stoner.tstomp.config;

import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import stoner.tstomp.security.CustomRealm;
import stoner.tstomp.security.KickoutSessionFilter;
import stoner.tstomp.security.MyFormAuthenticationFilter;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ShiroConfig {

    //不加这个注解不生效，具体不详
    @Bean
    @ConditionalOnMissingBean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAAP = new DefaultAdvisorAutoProxyCreator();
        defaultAAP.setProxyTargetClass(true);
        return defaultAAP;
    }

    @Bean
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        System.out.println("ShiroConfiguration.shirFilter()");
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //过滤器
        HashMap<String, Filter> stringFilterHashMap = new HashMap<>();
        stringFilterHashMap.put("kickout", new KickoutSessionFilter());
        shiroFilterFactoryBean.setFilters(stringFilterHashMap);

        Map<String, String> map = new LinkedHashMap<>();
        //注意过滤器配置顺序 不能颠倒
        //不使用shiro提供的退出过滤器,自己实现退出
//        map.put("/logout", "logout");
        map.put("/logout", "anon");
        // 配置不会被拦截的链接 顺序判断
        map.put("/static/**", "anon");
        map.put("/ajaxLogin", "anon");
        map.put("/login", "anon,kickout");
        map.put("/index", "authc,kickout");
        map.put("/index1", "user,kickout");
        map.put("/index2", "anon,roles[100002],perms[权限添加]");
        map.put("/index3", "noSessionCreation");
        map.put("/**", "authc");
        //配置shiro默认登录界面地址，前后端分离中登录界面跳转应由前端路由控制，后台仅返回json数据
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/index");
        //未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/error/403");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);
        return shiroFilterFactoryBean;
    }

    @Bean
    public CustomRealm customRealm() {
        return new CustomRealm();
    }

    @Bean
    public EhCacheManager ehCacheManager() {
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
        return ehCacheManager;
    }

    @Bean
    public SimpleCookie simpRememberMeCookie() {
        SimpleCookie simpRememberMeCookie = new SimpleCookie("SIMP_REMEMBER_ME_COOKIE");
        simpRememberMeCookie.setMaxAge(-1);
        simpRememberMeCookie.setHttpOnly(true);
        return simpRememberMeCookie;
    }

    @Bean
    public SimpleCookie simpSessionIdCookie() {
        SimpleCookie simpSessionIdCookie = new SimpleCookie("SIMP_SESSION_ID_COOKIE");
        simpSessionIdCookie.setPath("/");
        simpSessionIdCookie.setHttpOnly(true);
        return simpSessionIdCookie;
    }

    @Bean
    public CookieRememberMeManager cookieRememberMeManager(SimpleCookie simpRememberMeCookie) {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        //长度16位的byte[]
        cookieRememberMeManager.setCipherKey(Base64.decode("3AvVhMFLIs0KTA3Kprsdag=="));
        cookieRememberMeManager.setCookie(simpRememberMeCookie);
        return cookieRememberMeManager;
    }

    @Bean
    public DefaultWebSessionManager defaultWebSessionManager(SimpleCookie simpSessionIdCookie) {
        DefaultWebSessionManager defaultWebSessionManager = new DefaultWebSessionManager();
        defaultWebSessionManager.setGlobalSessionTimeout(3600000);
        defaultWebSessionManager.setSessionIdCookie(simpSessionIdCookie);
        defaultWebSessionManager.setSessionIdUrlRewritingEnabled(false);
        return defaultWebSessionManager;
    }

    @Bean
    public SecurityManager securityManager(
            CustomRealm customRealm,EhCacheManager ehCacheManager,
            CookieRememberMeManager cookieRememberMeManager,
            DefaultWebSessionManager defaultWebSessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(customRealm);
        securityManager.setCacheManager(ehCacheManager);
        securityManager.setRememberMeManager(cookieRememberMeManager);
        securityManager.setSessionManager(defaultWebSessionManager);
        return securityManager;
    }

    @Bean
    public FormAuthenticationFilter formAuthenticationFilter() {
        //注入自定义 FormAuthenticationFilter  DefaultFilter.authc
        return new MyFormAuthenticationFilter();
    }

    /**
     * 开启shiro aop注解支持.
     * 使用代理方式;所以需要开启代码支持;
     *
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
}
