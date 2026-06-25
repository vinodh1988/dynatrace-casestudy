# Dynatrace Java Microservices Case Study

This project is a runnable observability case study for a small checkout system. It is intentionally noisy enough to demonstrate synthetic monitoring, RUM, JVM metrics, JMX, distributed traces, trace-correlated logs, normal logs, UX events, alerts, and notification workflows in Dynatrace.

## Architecture

- `order-api`: Spring Boot web service, browser UI, REST APIs, JMS producer, custom JMX MBean, trace-aware structured logs.
- `payment-service`: Spring Boot worker service, JMS consumer, custom JMX MBean, simulated slow/error payment paths.
- `activemq`: JMS broker.
- `loadgen`: lightweight container that continuously calls checkout endpoints to generate traffic.

## Prerequisites

- Java 11+
- Maven
- Docker and Docker Compose
- Dynatrace SaaS or Managed environment
- A Dynatrace API token with scopes needed for OneAgent download, log ingest, and configuration work in your tenant

## Run Locally Without Dynatrace

```bash
docker compose up --build
```

Open:

- Web checkout UI: `http://localhost:8080`
- Order API actuator: `http://localhost:8080/actuator`
- Payment actuator: `http://localhost:8081/actuator`
- ActiveMQ console: `http://localhost:8161/admin` with `admin/admin`
- JMX ports: `localhost:9010` for `order-api`, `localhost:9011` for `payment-service`

The web checkout UI includes an **Artificial Load** panel. Use it to start browser-generated traffic with a configurable request rate, duration, catalog calls, slow calls, and error calls. This is useful for demonstrating RUM, user actions, API load, trace logs, and error/latency alerts without starting a separate script.

## Dynatrace Setup

Copy the example file and fill in your tenant details:

```bash
cp dynatrace/dynatrace.env.example dynatrace/dynatrace.env
```

Minimum values:

```env
DT_TENANT=abc12345
DT_TENANTTOKEN=<paas-token>
DT_CONNECTION_POINT=https://abc12345.live.dynatrace.com:443
DT_CUSTOM_PROP=app=checkout-case-study env=demo owner=platform-team
DT_RUM_SCRIPT=
```

Recommended paths:

1. Install OneAgent on the Docker host for full-stack monitoring of containers, JVMs, services, processes, logs, and network.
2. Use Dynatrace web application monitoring to create a JavaScript/RUM app and paste the generated script tag into `DT_RUM_SCRIPT`.
3. Restart the stack with `docker compose up --build`.

The services also expose JMX on ports `9010` and `9011`, actuator metrics on `/actuator/prometheus`, and structured JSON logs to stdout so Dynatrace can correlate logs with traces.

## Demonstration Scenarios

### 1. Synthetic Monitoring

Create these monitors in Dynatrace Synthetic:

- HTTP monitor: `GET http://<public-host>:8080/actuator/health`
- HTTP monitor: `GET http://<public-host>:8080/api/catalog`
- HTTP monitor: `POST http://<public-host>:8080/api/checkout` with JSON body:

```json
{"customerId":"synthetic-user","sku":"coffee-beans","quantity":1}
```

For a browser monitor, record a clickpath against `http://<public-host>:8080`, submit checkout, and validate the confirmation text.

### 2. Real User Monitoring and UX Monitoring

Set `DT_RUM_SCRIPT` to the Dynatrace RUM JavaScript tag. The UI injects it into the page head. The page also sends custom UX events to `/api/ux-events` for:

- page rendered
- product selected
- checkout started
- checkout finished
- checkout failed
- artificial load started, ticked, and stopped

In Dynatrace, inspect user actions, load actions, JavaScript errors, session replay if enabled, and conversion/funnel-style behavior around checkout.

### 3. JVM Monitoring

With OneAgent active, inspect each Java process for:

- heap and non-heap memory
- garbage collection
- threads
- CPU
- class loading
- request/service response time
- ActiveMQ client activity

The app deliberately allocates small payloads and has slow/error endpoints to make JVM and service behavior visible.

### 4. JMX Monitoring

Both services register custom MBeans:

- `checkout.case.study:type=OrderStats,name=OrderApi`
- `checkout.case.study:type=PaymentStats,name=PaymentService`

Useful attributes include total orders, failed orders, slow orders, processed payments, declined payments, and average processing time. Connect with JConsole locally or add a Dynatrace JMX extension configuration to collect these MBeans.

### 5. Traces, Trace Logs, and Normal Logs

Spring Cloud Sleuth adds trace and span IDs to MDC. `logback-spring.xml` emits structured logs with trace fields:

- `trace`
- `span`
- `level`
- `logger`
- `message`

Use checkout calls to follow a transaction from browser/RUM to `order-api`, through checkout preparation, pricing, payment dispatch, JMS, payment authorization, fraud screening, and gateway capture in `payment-service`. Compare correlated trace logs with normal application logs such as startup, health, and ActiveMQ connection logs.

### 6. Alerts and Notifications

Create alerting rules for:

- Synthetic monitor availability below target
- `order-api` 5xx rate above baseline
- `payment-service` response time degradation
- JVM heap usage over threshold
- Log event matching `checkout failed`
- Custom JMX metric for declined payments above threshold

Then connect a notification integration such as email, Slack, Microsoft Teams, Jira, ServiceNow, or webhook. Trigger test problems with:

```bash
curl "http://localhost:8080/api/simulate/error"
curl "http://localhost:8080/api/simulate/slow?delayMs=3000"
```

## Useful API Calls

```bash
curl http://localhost:8080/api/catalog
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{"customerId":"user-1","sku":"coffee-beans","quantity":2}'
curl http://localhost:8080/api/orders
curl http://localhost:8081/api/payments/stats
```

## Case Study Talking Points

- Synthetic monitoring detects the failure before users report it.
- RUM shows whether real users are affected and which browser actions are slow.
- Distributed traces identify whether latency is in `order-api`, JMS, or `payment-service`.
- Logs explain the business context of the trace.
- JVM metrics explain runtime pressure such as GC, heap, and threads.
- JMX adds domain-specific operational metrics.
- Alerts route problems to the right team with evidence instead of raw noise.

## Notes

Docker is not required to understand the code, but it is the intended demo runtime. If you run the apps directly from Maven, start ActiveMQ first and set `ACTIVEMQ_BROKER_URL=tcp://localhost:61616`.

The service Dockerfiles use multi-stage Maven builds, so Compose does not require locally prebuilt `target/*.jar` files. For a faster rebuild after the first Docker build, you can still run `mvn clean package` locally before using Docker.

Official Dynatrace references:

- Docker and container OneAgent installation: https://docs.dynatrace.com/docs/ingest-from/dynatrace-oneagent/installation/container-platforms/docker
- Web/RUM monitoring setup: https://docs.dynatrace.com/docs/observe/digital-experience/web-applications/initial-setup/monitoring-setup
- OpenTelemetry and Dynatrace: https://docs.dynatrace.com/docs/ingest-from/opentelemetry
- JMX extensions: https://docs.dynatrace.com/docs/observe/infrastructure-monitoring/extensions/jmx-extensions
