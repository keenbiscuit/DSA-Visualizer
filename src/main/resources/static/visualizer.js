//  Canvas Setup
const canvas = document.getElementById("canvas");
const ctx = canvas.getContext("2d");

// Resize canvas to match its display size
function resizeCanvas() {
  const wrapper = canvas.parentElement;
  canvas.width = wrapper.clientWidth;
  canvas.height = wrapper.clientHeight;
}
resizeCanvas();
window.addEventListener("resize", () => {
  resizeCanvas();
  drawCurrent();
});

//  State
let stompClient = null;
let connected = false;
let paused = false;
let running = false;
let compareCount = 0;
let actionCount = 0;
let currentState = [5, 3, 8, 1, 9, 2, 7, 4];
let currentHighlighted = [];
let currentType = "";
let sortedIndices = new Set(); // tracks which bars are fully sorted

// Connect to WebSocket
function connect() {
  const socket = new SockJS("http://localhost:8080/ws");
  stompClient = Stomp.over(socket);
  stompClient.debug = null; // silence stomp logs

  stompClient.connect(
    {},
    function (frame) {
      connected = true;
      document.getElementById("statusDot").classList.add("connected");
      document.getElementById("statusText").textContent = "connected";

      stompClient.subscribe("/topic/steps", onStepReceived);
    },
    function (error) {
      console.error("Connection error:", error);
      document.getElementById("statusText").textContent = "disconnected";
    },
  );
}

// Handle Incoming Steps
function onStepReceived(message) {
  const step = JSON.parse(message.body);
  console.log("Received step:", step);

  // update counters
  if (step.type === "COMPARE") compareCount++;
  if (step.type === "SWAP") actionCount++;
  if (step.type === "MERGE") actionCount++; // count merge comparisons

  document.getElementById("compareCount").textContent = compareCount;
  document.getElementById("actionCount").textContent = actionCount;

  // update step display badge
  const typeEl = document.getElementById("stepType");
  typeEl.textContent = step.type;
  typeEl.className = "step-type " + step.type;
  document.getElementById("stepMessage").textContent = step.description;

  // track sorted indices — last i elements are sorted after each pass
  if (step.type === "COMPLETE") {
    // mark everything sorted
    for (let i = 0; i < step.state.length; i++) sortedIndices.add(i);
    drawBars(step.state, [], "COMPLETE");
    drawComplete(step.state);
    running = false;
    resetControls();
    return;
  }

  // draw this step
  currentState = step.state;
  currentHighlighted = step.highlighted;
  currentType = step.type;
  drawBars(step.state, step.highlighted, step.type);
}

// Draw Bars
function drawBars(state, highlighted, type) {
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  const padding = 20;
  const barCount = state.length;
  const maxVal = Math.max(...state);
  const totalW = canvas.width - padding * 2;
  const totalH = canvas.height - padding * 2 - 30; // 30px for value labels
  const barW = totalW / barCount;
  const gap = Math.max(2, barW * 0.08);

  state.forEach(function (value, index) {
    const barHeight = (value / maxVal) * totalH;
    const x = padding + index * barW;
    const y = padding + totalH - barHeight;
    const w = barW - gap;

    // pick color
    let color;
    if (sortedIndices.has(index)) {
      color = "#a78bfa"; // purple = sorted
    } else if (highlighted.includes(index)) {
      color = type === "SWAP" ? "#f43f5e" : "#f59e0b";
    } else {
      color = "#4adeae"; // teal = default
    }

    // draw bar with rounded top
    const radius = Math.min(4, w / 4);
    ctx.beginPath();
    ctx.moveTo(x + radius, y);
    ctx.lineTo(x + w - radius, y);
    ctx.quadraticCurveTo(x + w, y, x + w, y + radius);
    ctx.lineTo(x + w, y + barHeight);
    ctx.lineTo(x, y + barHeight);
    ctx.lineTo(x, y + radius);
    ctx.quadraticCurveTo(x, y, x + radius, y);
    ctx.closePath();

    // glow effect for highlighted bars
    if (highlighted.includes(index) && !sortedIndices.has(index)) {
      ctx.shadowColor = color;
      ctx.shadowBlur = 12;
    } else {
      ctx.shadowBlur = 0;
    }

    ctx.fillStyle = color;
    ctx.fill();
    ctx.shadowBlur = 0;

    // draw value label on top of bar
    const fontSize = Math.min(13, Math.max(9, barW * 0.4));
    ctx.font = `600 ${fontSize}px JetBrains Mono, monospace`;
    ctx.fillStyle = highlighted.includes(index)
      ? "#fff"
      : "rgba(255,255,255,0.6)";
    ctx.textAlign = "center";
    ctx.textBaseline = "bottom";
    ctx.fillText(value, x + w / 2, y - 3);
  });
}

// Draw Complete Sweep
function drawComplete(state) {
  sortedIndices.clear();
  let index = 0;

  const sweep = setInterval(function () {
    if (index >= state.length) {
      clearInterval(sweep);
      return;
    }
    sortedIndices.add(index);
    drawBars(state, [], "COMPLETE");
    index++;
  }, 60);
}

// Draw Current (for resize)
function drawCurrent() {
  if (currentState.length > 0) {
    drawBars(currentState, currentHighlighted, currentType);
  }
}

//Complexity Panel
document.getElementById("algoSelect").addEventListener("change", function () {
  //Stop any running sort
  stompClient.send("/app/algo/pause", {}, "");

  //reset running state
  running = false;
  paused = false;

  //reset visuals
  const array = document
    .getElementById("arrayInput")
    .value.split(",")
    .map(Number)
    .filter((n) => !isNaN(n));

  sortedIndices.clear();
  compareCount = 0;
  actionCount = 0;

  document.getElementById("compareCount").textContent = "0";
  document.getElementById("actionCount").textContent = "0";
  document.getElementById("stepType").textContent = "READY";
  document.getElementById("stepType").className = "step-type";
  document.getElementById("stepMessage").textContent =
    "Configure your array and press Start";

  drawBars(array, []);
  resetControls();
  updateComplexity(this.value);
});

//Descriptions

function updateComplexity(algo) {
  // Names
  const names = {
    BUBBLE: "Bubble Sort",
    MERGE: "Merge Sort",
    QUICK: "Quick Sort",
  };

  document.getElementById("algoName").textContent = names[algo];

  // Descriptions
  const descriptions = {
    BUBBLE:
      "Repeatedly steps through the list, compares adjacent elements and swaps them if they are in the wrong order.",
    MERGE:
      "Divides the array in half recursively until single elements remain, then merges them back in sorted order.",
    QUICK:
      "Selects a pivot element and partitions the array into elements less than and greater than the pivot, recursively sorting each partition.",
  };

  document.getElementById("algoDesc").textContent = descriptions[algo];

  // Complexities
  const complexities = {
    BUBBLE: {
      worst: "O(n²)",
      avg: "O(n²)",
      best: "O(n)",
      space: "O(1)",
    },
    MERGE: {
      worst: "O(nlog(n))",
      avg: "O(nlog(n))",
      best: "O(nlog(n))",
      space: "O(n)",
    },
    QUICK: {
      worst: "O(n²)",
      avg: "O(nlog(n))",
      best: "O(nlog(n))",
      space: "O(log(n))",
    },
  };
  const comp = complexities[algo];
  document.getElementById("best").textContent = comp.best;
  document.getElementById("avg").textContent = comp.avg;
  document.getElementById("worst").textContent = comp.worst;
  document.getElementById("space").textContent = comp.space;

  // Stats Labels
  const statlabels = {
    BUBBLE: "Swaps",
    MERGE: "Merges",
    QUICK: "Swaps",
  };
  document.getElementById("stat-label").textContent = statlabels[algo];
}

// Speed Slider
document.getElementById("speedInput").addEventListener("input", function () {
  document.getElementById("speedDisplay").textContent = this.value + "ms";
});

//  Start Button
document.getElementById("startBtn").addEventListener("click", function () {
  if (!connected) {
    alert("Not connected to server — wait a moment and try again");
    return;
  }
  if (running) {
    console.log("Already running");
    return;
  }

  // reset state
  compareCount = 0;
  actionCount = 0;
  sortedIndices.clear();
  document.getElementById("compareCount").textContent = "0";
  document.getElementById("actionCount").textContent = "0";

  const arrayInput = document.getElementById("arrayInput").value;
  const array = arrayInput
    .split(",")
    .map(Number)
    .filter((n) => !isNaN(n));
  const speed = parseInt(document.getElementById("speedInput").value);

  if (array.length < 2) {
    alert("Please enter at least 2 numbers");
    return;
  }

  currentState = array;
  drawBars(array, []);

  const algoName = document.getElementById("algoSelect").value;
  const request = {
    algoName: algoName, //'BUBBLE, MERGE, QUICK, etc',
    state: array,
    speed: speed,
  };

  if (algoName === "BUBBLE") {
    stompClient.send("/app/algo/bubblesort", {}, JSON.stringify(request));
  } else if (algoName === "MERGE") {
    stompClient.send("/app/algo/mergesort", {}, JSON.stringify(request));
  } else if (algoName === "QUICK") {
    stompClient.send("/app/algo/quicksort", {}, JSON.stringify(request));
  }

  running = true;
  paused = false;
  document.getElementById("startBtn").disabled = true;
  document.getElementById("pauseBtn").disabled = false;
  document.getElementById("pauseBtn").textContent = "⏸ Pause";
  document.getElementById("pauseBtn").classList.remove("active");
});

// Pause Button
document.getElementById("pauseBtn").addEventListener("click", function () {
  paused = !paused;

  if (paused) {
    stompClient.send("/app/algo/pause", {}, "");
    this.innerHTML = '<span class="btn-icon">▶</span> Resume';
    this.classList.add("active");
  } else {
    stompClient.send("/app/algo/resume", {}, "");
    this.innerHTML = '<span class="btn-icon">⏸</span> Pause';
    this.classList.remove("active");
  }
});

// Reset Button
document.getElementById("resetBtn").addEventListener("click", function () {
  const arrayInput = document.getElementById("arrayInput").value;
  const array = arrayInput
    .split(",")
    .map(Number)
    .filter((n) => !isNaN(n));

  currentState = array;
  currentHighlighted = [];
  currentType = "";
  sortedIndices.clear();
  compareCount = 0;
  actionCount = 0;

  document.getElementById("compareCount").textContent = "0";
  document.getElementById("actionCount").textContent = "0";
  document.getElementById("stepType").textContent = "READY";
  document.getElementById("stepType").className = "step-type";
  document.getElementById("stepMessage").textContent =
    "Configure your array and press Start";

  drawBars(array, []);
  resetControls();
});

function resetControls() {
  running = false;
  paused = false;
  document.getElementById("startBtn").disabled = false;
  document.getElementById("pauseBtn").disabled = true;
  document.getElementById("pauseBtn").innerHTML =
    '<span class="btn-icon">⏸</span> Pause';
  document.getElementById("pauseBtn").classList.remove("active");
}

// Init
connect();
drawBars(currentState, []);
updateComplexity("BUBBLE");
