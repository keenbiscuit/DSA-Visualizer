package com.dsavisualizer.algorithms;

import java.util.Arrays;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.dsavisualizer.models.AlgoStep;

@Component
public class BubbleSort {
    // the 'what' sent over the websocket
    private final SimpMessagingTemplate messaging;
    private volatile boolean paused = false;

    // Spring injects SimpMessagingTemplate automatically
    public BubbleSort(SimpMessagingTemplate messaging) {
        this.messaging = messaging;

    }

    public void pause() {
        this.paused = true;
    };

    public void resume() {
        this.paused = false;
    };

    public void sort(int[] arr, String sessionId, int speed) {

        for (int i = 0; i < arr.length - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < arr.length - i - 1; j++) {

                // Check running flag prior to each step
                while (paused) {
                    
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
        messaging.convertAndSend("/topic/steps", step);
    }
}
