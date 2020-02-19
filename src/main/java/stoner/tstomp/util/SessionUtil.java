package stoner.tstomp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class SessionUtil {

    private static final Logger logger = LoggerFactory.getLogger(SessionUtil.class);

    private static final ConcurrentHashMap<String, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    private static final LinkedMultiValueMap<String,String> onlineSessionIds = new LinkedMultiValueMap<>();

    private static final CopyOnWriteArraySet<String> subscriptionSet = new CopyOnWriteArraySet<>();

    public static void afterConnectionEstablished(WebSocketSession session) {
        put(session);
    }

    public static void afterConnectionClosed(WebSocketSession session) {
        close(session);
    }

    @Scheduled(initialDelay = 60 * 1000, fixedDelay = 60 * 1000)
    public static void checkSessions() {
        WebSocketSession session;
        for (Map.Entry<String, List<String>> entry : onlineSessionIds.entrySet()) {
            List<String> idList = entry.getValue();
            for (String sessionId : idList) {
                if ((session = onlineSessions.get(sessionId)) == null) {
                    idList.remove(sessionId);
                } else if (!session.isOpen()) {
                    idList.remove(sessionId);
                    onlineSessions.remove(sessionId);
                }
            }
        }
    }

    public static boolean cacheSubscription(String destination,WebSocketSession session) {
        Object subscriptionList;
        Map<String,Object> attributes;
        if (session == null || (attributes = session.getAttributes()) == null) {
            return false;
        } else if ((subscriptionList = attributes.get("subscriptionSet")) == null) {
            attributes.put("subscriptionSet", new CopyOnWriteArrayList<>());
        } else if (subscriptionList instanceof CopyOnWriteArrayList) {
            CopyOnWriteArrayList subscriptionList1 = (CopyOnWriteArrayList)subscriptionList;
            if (subscriptionList1.contains(destination)) {
                return true;
            } else {
                subscriptionList1.add(destination);
            }
        }
        return false;
    }

    public static void put(WebSocketSession session) {
        String sessionId = session.getId();
        Principal principal = session.getPrincipal();
        if (principal != null) {
            onlineSessionIds.set(principal.getName(), sessionId);
        }
        if (session.isOpen()) { onlineSessions.put(sessionId, session);}
    }

    public static WebSocketSession get(WebSocketSession session) {
        return onlineSessions.get(session.getId());
    }

    public static WebSocketSession getBySessionId(String sessionId) {
        return onlineSessions.get(sessionId);
    }

    public static WebSocketSession get(String name) {
        List<String> idList = onlineSessionIds.get(name);
        if (idList == null) return null;
        WebSocketSession session;
        for (String sessionId : idList) {
            session = onlineSessions.get(sessionId);
            if (session != null && session.isOpen()) { return session;}
        }
        return null;
    }

    public static void stop() {
        for (WebSocketSession session : onlineSessions.values()) {
            close(session);
        }
    }

    private static void close(WebSocketSession session) {
        try {
            session.close(CloseStatus.GOING_AWAY);
        } catch (IOException e) {
            logger.warn("Failed to close session [{}] :{}", session.getId(), e.getMessage());
        } finally {
            onlineSessions.remove(session.getId());
        }
    }
}
