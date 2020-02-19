package stoner.tstomp.config;

import com.alibaba.fastjson.JSON;
import com.sun.security.auth.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import stoner.tstomp.bean.FrameVo;
import stoner.tstomp.util.SessionUtil;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * 开启WebSocket支持
 */
@Configuration
@EnableWebSocketMessageBroker  //注解开启STOMP协议来传输基于代理的消息，此时控制器支持使用
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private Environment env;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        /*基于mq实现stomp代理，适用于集群。
         * 以/topic和/queue开头的消息会发送到stomp代理中:mq等。
         * 每个mq适用的前缀不一样且有限制。activemq支持stomp的端口为61613
         */
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(env.getProperty("spring.rabbitmq.host"))
                .setRelayPort(61613)
                .setSystemLogin(env.getProperty("spring.rabbitmq.username"))
                .setSystemPasscode(env.getProperty("spring.rabbitmq.password"))
                .setClientLogin(env.getProperty("spring.rabbitmq.username"))
                .setClientPasscode(env.getProperty("spring.rabbitmq.password"))
                .setSystemHeartbeatSendInterval(5000)
                .setSystemHeartbeatReceiveInterval(4000);
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(notifyHandshakeInterceptor())
                .setHandshakeHandler(notifyDefaultHandshakeHandler())
                .setAllowedOrigins("*").withSockJS();
    }

    private HandshakeHandler notifyDefaultHandshakeHandler() {
        return new DefaultHandshakeHandler(){
            @Override
            protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                //设置认证通过的用户到当前会话中
                return (Principal)attributes.get("currentUser");
            }

            @Override
            protected void doStop() {
                super.doStop();
            }
        };
    }

    private HandshakeInterceptor notifyHandshakeInterceptor() {
        return new HandshakeInterceptor() {

            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
                ServletServerHttpRequest req = (ServletServerHttpRequest) serverHttpRequest;
                //通过url的query参数获取认证参数
                String token = req.getServletRequest().getParameter("token");
                if (token == null) {
                    token = UUID.randomUUID().toString();
                } else {
                    logger.info("用户身份token:{}",token);
                }
                //根据token认证用户，不通过返回拒绝握手
//                Principal user = authenticate(token);
                Principal user = new UserPrincipal(token);
                if(user == null){
                    logger.warn("用户身份token:{}，身份验证错误或者用户未登录！",token);
                    return false;
                }
                //保存认证用户
                map.put("currentUser", user);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

            }
        };
    }

    @Override
    public void configureWebSocketTransport(final WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
            @Override
            public WebSocketHandler decorate(final WebSocketHandler handler) {
                return new WebSocketHandlerDecorator(handler) {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        logSession("已建立连接",logger, session);
                        SessionUtil.afterConnectionEstablished(session);
                        super.afterConnectionEstablished(session);
                    }

                    @Override
                    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                        logSession("处理消息",logger, session);
                        if (message.getPayload() instanceof String) {
                            FrameVo frameVo = new FrameVo((String) message.getPayload());
                            logger.info("frame >>> {}",JSON.toJSONString(frameVo));
                            if (SimpMessageType.SUBSCRIBE.toString().equals(frameVo.getType())) {
                                String destination = frameVo.getHeaders().getFirst("destination");
                                if (SessionUtil.cacheSubscription(destination, session)) {
                                    logger.info("已经订阅过了 : {}",destination);
                                    return;
                                }
                            }
                        }
                        super.handleMessage(session, message);
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                        logSession("异常，关闭连接",logger, session);
                        SessionUtil.afterConnectionClosed(session);
                        super.handleTransportError(session, exception);
                    }

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                        logSession("连接已关闭",logger, session);
                        SessionUtil.afterConnectionClosed(session);
                        super.afterConnectionClosed(session, closeStatus);
                    }
                };
            }
        });
    }

    private static void logSession(String prefix, Logger logger, WebSocketSession session) {
        String id = session.getId();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : session.getAttributes().entrySet()) {
            sb.append(entry.getKey()).append(':').append(entry.getValue()).append(';');
        }
        logger.info("{}，会话{}：[{}]", prefix, id, sb.deleteCharAt(sb.length() - 1));
    }

}
