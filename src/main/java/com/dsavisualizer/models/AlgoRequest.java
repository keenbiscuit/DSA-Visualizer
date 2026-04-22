package com.dsavisualizer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// What the browser sends to the server
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlgoRequest {
    private String algoName;
    private int[] state;
    private int speed;
}
