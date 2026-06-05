# DSA Visualizer

A real-time data structures and algorithms visualizer built with Spring Boot and WebSockets. Multiple users can run simultaneous sorts without interfering with each other via per-session state management. Algorithms run on the backend and stream each step live to the frontend, allowing you to watch sorting algorithms execute step-by-step with pause, resume, and speed controls.

## Architecture
1. When a user clicks Start-> the frontend packages the array, algorithm name, and speed into a JSON request and sends it over the WebSocket
2. Then AlgoController receives it and spins up a new thread via CompletableFuture
3. The algorithm runs on that thread which emits each step (compare, swap, or merge) back to the front end in real time via SimpMessagingTemplate
4. Each step is routed to the specific user's session using convertAndSendToUser so the user's sessions don't interfere with each other
5. Then the frontend receives each step and redraws the canvas accordingly

## Features
- Speed slider
- Real-time step-by-step visualization via WebSocket streaming
- Pause and resume algorithm execution mid-sort
- Performance metrics displayed per run (comparisons and swap/merge counts)
- Supports Bubble Sort and Merge Sort

## Tech Stack

- **Backend:** Java, Spring Boot, WebSocket (STOMP protocol), CompletableFuture (async threading)
- **Frontend:** HTML, CSS, Vanilla JavaScript
- **Build:** Maven
- **Containerization:** Docker
- **Deployment:** AWS EC2

## Getting Started

### Prerequisites
- Java 17+
- Maven
- Docker

### Live Demo
http://107.23.209.222:8080/

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
- GitHub Actions CI/CD pipeline
- AWS ECS Migration
