const state = {
  sku: "coffee-beans",
  quantity: 1
};

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

window.addEventListener("load", () => ux("page_rendered", {path: location.pathname}));
