package com.example.observability.order.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PageController {
    private final String rumScript;

    public PageController(@Value("${case-study.rum-script:}") String rumScript) {
        this.rumScript = rumScript == null ? "" : rumScript;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "<!doctype html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
            + rumScript
            + "<title>Checkout Case Study</title><link rel=\"stylesheet\" href=\"/app.css\"></head>"
            + "<body><main><header><h1>Checkout Case Study</h1><p>Java microservices observability demo for Dynatrace.</p></header>"
            + "<section class=\"grid\">"
            + product("coffee-beans", "Coffee Beans", "$12.99")
            + product("travel-mug", "Travel Mug", "$18.99")
            + product("espresso-machine", "Espresso Machine", "$199.99")
            + "</section><section class=\"panel checkout\"><label>Customer<input id=\"customerId\" value=\"web-user\"></label>"
            + "<label>Quantity<input id=\"quantity\" type=\"number\" min=\"1\" value=\"1\"></label>"
            + "<button onclick=\"checkout()\">Checkout</button></section>"
            + "<p>Selected SKU: <strong id=\"selectedSku\">coffee-beans</strong></p><div id=\"status\" class=\"status\"></div>"
            + "</main><script src=\"/app.js\"></script></body></html>";
    }

    private String product(String sku, String name, String price) {
        return "<article class=\"product\"><h2>" + name + "</h2><p>" + price + "</p>"
            + "<button onclick=\"selectProduct('" + sku + "')\">Select</button></article>";
    }
}
