package com.dsavisualizer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//What the server sends to the browser
//Ex: Everytime bubble sort does a comparison or swap
//We package that moment and sent it to the browser

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlgoStep {
    private String type;
    private String description;
    private int[] state;
    private int[] highlighted;

}