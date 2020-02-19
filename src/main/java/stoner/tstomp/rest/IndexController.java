package stoner.tstomp.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import stoner.tstomp.bean.MessageVo;
import stoner.tstomp.bean.User;
import stoner.tstomp.service.IndexService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
@Slf4j
public class IndexController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private IndexService indexService;

    @RequestMapping(value = "/login")
    @ResponseBody
    public String login(User user, HttpServletRequest request) {
        UsernamePasswordToken usernamePasswordToken =
                new UsernamePasswordToken(user.getUserName(), user.getPassword(), true, request.getRemoteHost());
        return indexService.login(usernamePasswordToken);
    }

    @MessageMapping("/good")
    public void publish(@Payload MessageVo messageVo, StompHeaderAccessor accessor) {
        String destination = "/topic/public";
        String name = messageVo.getName();
        if (StringUtils.isEmpty(name)) {
            name = "感谢订阅！\n" + name + ", sessionId : ";
        } else {
            name = "感谢订阅！\nsessionId : ";
        }
        MessageHeaders messageHeaders = accessor.getMessageHeaders();
        name += messageHeaders.get("simpSessionId", String.class);
        messageVo.setName(name);
        template.convertAndSend(destination, messageVo);
    }

    @MessageMapping("/good/{goodNo}")
    public void publish(@Payload MessageVo messageVo, @DestinationVariable String goodNo, StompHeaderAccessor accessor) {
        String destination = "/topic/" + goodNo;
        String name = messageVo.getName();
        if (StringUtils.isEmpty(name)) {
            name = "感谢订阅！\n" + name + ", sessionId : ";
        } else {
            name = "感谢订阅！\nsessionId : ";
        }
        MessageHeaders messageHeaders = accessor.getMessageHeaders();
        name += messageHeaders.get("simpSessionId", String.class);
        messageVo.setName(name);
        template.convertAndSend(destination, messageVo);
    }

    @MessageMapping("/good/{isBoardcast}/{goodNo}")
    public void publish(@Payload MessageVo messageVo, @DestinationVariable String isBoardcast, @DestinationVariable String goodNo,StompHeaderAccessor accessor) {
        String destination;
        if ("all".equals(isBoardcast)) {
            destination = "/topic/public";
        } else if ("one".equals(isBoardcast)) {
            destination = "/topic/" + goodNo;
        } else {
            return;
        }
        String name = messageVo.getName();
        if (StringUtils.isEmpty(name)) {
            name += ", uuid : ";
        } else {
            name = "uuid : ";
        }
        Principal user = accessor.getUser();
        String passcode = accessor.getPasscode();
        if (user != null) {
            name += user.getName();
        }
        messageVo.setName(name);
        template.convertAndSend(destination, messageVo);
    }
}
