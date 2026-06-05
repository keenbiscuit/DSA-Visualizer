package com.dsavisualizer.controllers;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.dsavisualizer.algorithms.BubbleSort;
import com.dsavisualizer.algorithms.MergeSort;
import com.dsavisualizer.models.AlgoRequest;
import com.dsavisualizer.session.SessionStateManager;

@Controller
public class AlgoController {

    private final SimpMessagingTemplate messaging;
    private final BubbleSort bubbleSort;
    private final MergeSort mergeSort;
    private final SessionStateManager sessionStateManager;

    // constructor
    public AlgoController(SimpMessagingTemplate messaging, SessionStateManager sessionStateManager, BubbleSort bubbleSort, MergeSort mergeSort) {
        this.messaging = messaging;
        this.bubbleSort = bubbleSort;
        this.mergeSort = mergeSort;
        this.sessionStateManager = sessionStateManager;
    }

    // method to start the algorithm
    @MessageMapping("/algo/bubblesort")
    public void startBubbleAlgo(@Payload AlgoRequest request, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println(Arrays.toString(request.getState()));
        if (headerAccessor.getUser() == null) {
            System.out.println("ERROR: No principal found for session");
            return;
        }
        String sessionId = headerAccessor.getUser().getName();
        System.out.println("Starting bubble sort for principal: " + sessionId);
        CompletableFuture.runAsync(() -> {
            bubbleSort.sort(request.getState(), sessionId, request.getSpeed());
        }).exceptionally(ex -> {
            System.err.println("BubbleSort error: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });
    }

    @MessageMapping("/algo/mergesort")
    public void startMergeSort(@Payload AlgoRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getUser().getName();

        CompletableFuture.runAsync(() -> {

            mergeSort.sort(request.getState(),
                    sessionId,
                    request.getSpeed());
        });
    }

    @MessageMapping("/algo/pause")
    public void pauseAlgo(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getUser().getName();
        sessionStateManager.pauseSession(sessionId);
    }

    @MessageMapping("/algo/resume")
    public void resumeAlgo(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getUser().getName();
        sessionStateManager.resumeSession(sessionId);
    }
}
