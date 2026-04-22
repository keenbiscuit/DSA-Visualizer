package com.dsavisualizer.controllers;

import java.util.concurrent.CompletableFuture;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.dsavisualizer.algorithms.BubbleSort;
import com.dsavisualizer.algorithms.MergeSort;
import com.dsavisualizer.models.AlgoRequest;

@Controller
public class AlgoController {

    private final SimpMessagingTemplate messaging;
    private final BubbleSort bubbleSort;
    private final MergeSort mergeSort;

    // constructor
    public AlgoController(SimpMessagingTemplate messaging, BubbleSort bubbleSort, MergeSort mergeSort) {
        this.messaging = messaging;
        this.bubbleSort = bubbleSort;
        this.mergeSort = mergeSort;
    }


    // method to start the algorithm
    @MessageMapping("/algo/bubblesort")
    public void startBubbleAlgo(@Payload AlgoRequest request, SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();

        // Websockt thread starts off and immediately returns
        // Bubble sort runs on own independant thread
        // Websock thread stays free to send each step as it's emitted
        CompletableFuture.runAsync(() -> {

            bubbleSort.sort(request.getState(),
                    sessionId,
                    request.getSpeed());
        });

    }

    @MessageMapping("/algo/mergesort")
    public void startMergeSort(@Payload AlgoRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        CompletableFuture.runAsync(() -> {

            mergeSort.sort(request.getState(),
                    sessionId,
                    request.getSpeed());
        });
    }

    @MessageMapping("/algo/pause")
    public void pauseAlgo() {
        bubbleSort.pause();
        mergeSort.pause();
    }

    @MessageMapping("/algo/resume")
    public void resumeAlgo() {
        bubbleSort.resume();
        mergeSort.resume();
    }
}
