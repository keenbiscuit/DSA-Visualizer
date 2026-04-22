# DSA Visualizer

A real-time data structures and algorithms visualizer built with Spring Boot and WebSockets. Algorithms run on the backend and stream each step live to the frontend, allowing you to watch sorting algorithms execute step-by-step with pause, resume, and speed controls.

## Features

- Real-time step-by-step visualization via WebSocket streaming
- Pause and resume algorithm execution mid-sort
- Performance metrics displayed per run (comparisons, time elapsed)
- Supports Bubble Sort and Merge Sort

## Tech Stack

- **Backend:** Java, Spring Boot, WebSocket (STOMP protocol), CompletableFuture (async threading)
- **Frontend:** HTML, CSS, Vanilla JavaScript
- **Build:** Maven

## Getting Started

### Prerequisites
- Java 17+
- Maven

### Run locally

```bash
git clone https://github.com/keenbiscuit/DSA-Visualizer.git
cd DSA-Visualizer
./mvnw spring-boot:run
```

Then open `http://localhost:8080` in your browser.

## Project Structure

```
src/
├── main/
│   ├── java/com/dsavisualizer/
│   │   ├── algorithms/        # BubbleSort, MergeSort
│   │   ├── controllers/       # AlgoController (WebSocket message handling)
│   │   ├── models/            # AlgoRequest, AlgoStep
│   │   └── websocket/         # WebSocketConfig (STOMP broker setup)
│   └── resources/
│       └── static/            # index.html, styles.css, visualizer.js
```

## Planned Features

- QuickSort, Heap Sort, Insertion Sort
- Binary Search visualization
- Binary Search Tree visualizer
- Deployment via AWS ECS (in progress)
