package com.dsavisualizer.session;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStateManager {
    private final ConcurrentHashMap<String, Boolean> pausedSession = new ConcurrentHashMap<>();

    public void pauseSession(String sessionId) {
        pausedSession.put(sessionId, true);
    }

    public void resumeSession(String sessionId) {
        pausedSession.put(sessionId, false);
    }

    public boolean isSessionPaused(String sessionId) {
        return pausedSession.getOrDefault(sessionId, false);
    }

    public void remove(String sessionId) {
        pausedSession.remove(sessionId);
    }
}
