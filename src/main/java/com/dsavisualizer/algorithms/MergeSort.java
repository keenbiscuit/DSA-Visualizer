package com.dsavisualizer.algorithms;

import java.util.Arrays;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.dsavisualizer.models.AlgoStep;
import com.dsavisualizer.session.SessionStateManager;

@Component
public class MergeSort {
    private final SimpMessagingTemplate messaging;
    private final SessionStateManager sessionStateManager;

    public MergeSort(SimpMessagingTemplate messaging, SessionStateManager sessionStateManager) {
        this.messaging = messaging;
        this.sessionStateManager = sessionStateManager;
    }

    public void sort(int[] arr, String sessionId, int speed) {

        mergeSort(arr, 0, arr.length - 1, sessionId, speed);
        emit(sessionId, "COMPLETE", arr, new int[] {}, "Sort Complete!");
    }

    public void mergeSort(int[] arr, int left, int right, String sessionId, int speed) {
        if (right - left <= 0)
            return;

        int mid = (left + right) / 2;
        mergeSort(arr, left, mid, sessionId, speed);
        mergeSort(arr, mid + 1, right, sessionId, speed);
        merge(arr, left, mid, right, sessionId, speed);
    }

    public void merge(int[] arr, int left, int mid, int right, String sessionId, int speed) {
        int[] temp = Arrays.copyOfRange(arr, left, right + 1);

        int i = 0; // pointer for left half of temp
        int j = mid - left + 1; // pointer for right half of temp
        int k = left; // pointer for position in arr

        while (i < mid - left + 1 && j < right - left + 1) {

            // Check paused flag
            while (sessionStateManager.isSessionPaused(sessionId)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            emit(sessionId, "COMPARE",
                    arr,
                    new int[] { i + left, j + left },
                    "Comparing " + temp[i] + " and " + temp[j]);

            if (temp[i] <= temp[j]) {
                arr[k] = temp[i];
                i++;
            } else {
                arr[k] = temp[j];
                j++;
            }

            emit(sessionId, "MERGE",
                    arr,
                    new int[] { k },
                    "Placing " + arr[k] + " at position " + k);

            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            k++;
        }

        // Copy remaining left elements if any
        while (i < mid - left + 1) {
            arr[k] = temp[i];
            emit(sessionId, "MERGE", arr, new int[] { k }, "Placing remaining " + arr[k] + " at position " + k);
            i++;
            k++;

        }

        // Copy remaining right elements if any
        while (j < right - left + 1) {
            arr[k] = temp[j];
            emit(sessionId, "MERGE", arr, new int[] { k }, "Placing " + arr[k] + " at position " + k);
            j++;
            k++;
        }
    }

    private void emit(String sessionId, String type, int[] state, int[] highlighted, String description) {
        System.out.println("Emitting: " + type + " to session: " + sessionId);
        AlgoStep step = new AlgoStep(type, description, Arrays.copyOf(state, state.length), highlighted);
        messaging.convertAndSendToUser(sessionId, "/queue/steps", step);
    }

}
