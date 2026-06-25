const state = {
  sku: "coffee-beans",
  quantity: 1,
  load: {
    running: false,
    timer: null,
    startedAt: 0,
    stopsAt: 0,
    ticks: 0,
    sent: 0,
    succeeded: 0,
    failed: 0
  }
};

const loadSkus = ["coffee-beans", "travel-mug", "espresso-machine"];

async function ux(name, data = {}) {
  try {
    await fetch("/api/ux-events", {
      method: "POST",
      headers: {"Content-Type": "application/json"},
      body: JSON.stringify({name, data, at: new Date().toISOString()})
    });
  } catch (ignored) {
  }
}

function selectProduct(sku) {
  state.sku = sku;
  document.querySelector("#selectedSku").textContent = sku;
  ux("product_selected", {sku});
}

async function checkout() {
  const customerId = document.querySelector("#customerId").value || "web-user";
  const quantity = Number(document.querySelector("#quantity").value || 1);
  const status = document.querySelector("#status");
  status.textContent = "Submitting checkout...";
  await ux("checkout_started", {sku: state.sku, quantity});

  try {
    const response = await fetch("/api/checkout", {
      method: "POST",
      headers: {"Content-Type": "application/json"},
      body: JSON.stringify({customerId, sku: state.sku, quantity})
    });
    const body = await response.json();
    if (!response.ok) {
      throw new Error(body.message || "Checkout failed");
    }
    status.textContent = `Order ${body.orderId} accepted. Payment is processing.`;
    await ux("checkout_finished", {orderId: body.orderId});
  } catch (error) {
    status.textContent = error.message;
    await ux("checkout_failed", {message: error.message});
  }
}

function readNumber(selector, fallback, min, max) {
  const value = Number(document.querySelector(selector).value || fallback);
  if (!Number.isFinite(value)) {
    return fallback;
  }
  return Math.min(Math.max(value, min), max);
}

function setLoadControls(running) {
  document.querySelector("#startLoad").disabled = running;
  document.querySelector("#stopLoad").disabled = !running;
  document.querySelector("#loadRate").disabled = running;
  document.querySelector("#loadDuration").disabled = running;
}

function updateLoadStatus(message) {
  const load = state.load;
  const status = document.querySelector("#loadStatus");
  const remainingSeconds = load.running
    ? Math.max(0, Math.ceil((load.stopsAt - Date.now()) / 1000))
    : 0;
  const summary = `sent ${load.sent}, ok ${load.succeeded}, failed ${load.failed}`;
  status.textContent = message || (load.running ? `Running for ${remainingSeconds}s, ${summary}` : `Stopped, ${summary}`);
}

async function callLoadEndpoint(url, options) {
  state.load.sent += 1;
  try {
    const response = await fetch(url, options);
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    state.load.succeeded += 1;
  } catch (error) {
    state.load.failed += 1;
  }
}

async function loadTick() {
  const load = state.load;
  if (!load.running) {
    return;
  }

  if (Date.now() >= load.stopsAt) {
    stopLoad("Completed");
    return;
  }

  load.ticks += 1;
  const count = load.ticks;
  const prefix = document.querySelector("#loadCustomerPrefix").value || "ui-load";
  const sku = loadSkus[count % loadSkus.length];
  const requests = [];

  if (document.querySelector("#includeCatalog").checked) {
    requests.push(callLoadEndpoint("/api/catalog"));
  }

  requests.push(callLoadEndpoint("/api/checkout", {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify({customerId: `${prefix}-${count}`, sku, quantity: 1})
  }));

  if (document.querySelector("#includeSlow").checked && count % 5 === 0) {
    requests.push(callLoadEndpoint("/api/simulate/slow?delayMs=1500"));
  }

  if (document.querySelector("#includeErrors").checked && count % 9 === 0) {
    requests.push(callLoadEndpoint("/api/simulate/error"));
  }

  await Promise.all(requests);
  await ux("load_tick", {sent: load.sent, succeeded: load.succeeded, failed: load.failed});
  updateLoadStatus();
}

function startLoad() {
  if (state.load.running) {
    return;
  }

  const rate = readNumber("#loadRate", 12, 1, 120);
  const durationSeconds = readNumber("#loadDuration", 60, 5, 3600);
  const intervalMs = Math.max(500, Math.round(60000 / rate));

  state.load = {
    running: true,
    timer: null,
    startedAt: Date.now(),
    stopsAt: Date.now() + durationSeconds * 1000,
    ticks: 0,
    sent: 0,
    succeeded: 0,
    failed: 0
  };
  setLoadControls(true);
  updateLoadStatus("Starting load...");
  ux("load_started", {rate, durationSeconds});

  loadTick();
  state.load.timer = window.setInterval(loadTick, intervalMs);
}

function stopLoad(reason = "Stopped") {
  const load = state.load;
  if (load.timer) {
    window.clearInterval(load.timer);
  }
  load.running = false;
  load.timer = null;
  setLoadControls(false);
  updateLoadStatus(reason);
  ux("load_stopped", {reason, sent: load.sent, succeeded: load.succeeded, failed: load.failed});
}

window.addEventListener("load", () => ux("page_rendered", {path: location.pathname}));
