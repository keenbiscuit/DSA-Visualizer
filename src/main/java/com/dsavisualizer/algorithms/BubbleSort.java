package com.dsavisualizer.algorithms;

import java.util.Arrays;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.dsavisualizer.session.SessionStateManager;

import com.dsavisualizer.models.AlgoStep;

@Component
public class BubbleSort {
    // the 'what' sent over the websocket
    private final SimpMessagingTemplate messaging;
    private final SessionStateManager sessionStateManager;

    // Spring injects dependencies automatically
    public BubbleSort(SimpMessagingTemplate messaging, SessionStateManager sessionStateManager) {
        this.messaging = messaging;
        this.sessionStateManager = sessionStateManager;
    }

    public void sort(int[] arr, String sessionId, int speed) {

        for (int i = 0; i < arr.length - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < arr.length - i - 1; j++) {

                // Check running flag prior to each step
                while (sessionStateManager.isSessionPaused(sessionId)) {

                    try {
                        Thread.sleep(100);// wait in 100ms chunks while paused
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                emit(sessionId, "COMPARE",
                        arr,
                        new int[] { j, j + 1 },
                        "Comparing " + arr[j] + " and " + arr[j + 1]);

                if (arr[j] > arr[j + 1]) {

                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;

                    emit(sessionId, "SWAP",
                            arr,
                            new int[] { j, j + 1 },
                            "Swapping " + arr[j] + " and " + arr[j + 1]);
                }
                try {
                    Thread.sleep(speed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (!swapped)
                break;
        }
        // emit COMPLETE
        emit(sessionId, "COMPLETE", arr, new int[] {}, "Sort Complete!");
    }

    private void emit(String sessionId, String type, int[] state, int[] highlighted, String description) {
        System.out.println("Emitting: " + type + " to session: " + sessionId);
        AlgoStep step = new AlgoStep(
                type,
                description,
                Arrays.copyOf(state, state.length),
                highlighted);
        messaging.convertAndSendToUser(sessionId, "/queue/steps", step);
    }
}
