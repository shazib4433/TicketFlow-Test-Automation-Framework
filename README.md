# TicketFlow

Enterprise Test Automation & Distributed Systems Validation Framework.

A lightweight ServiceNow-style incident management app (`application/`) plus a Java test automation framework (`automation/`) covering UI (Playwright), API (REST Assured), DB, WireMock dependency failures, async and concurrent workflows.

## Stack

Java 26 · Maven · Spring Boot 4.1 · Playwright for Java · REST Assured · TestNG · WireMock · PostgreSQL · Docker Compose · GitHub Actions

## Modules

| Module | Purpose |
|---|---|
| `application` | TicketFlow Spring Boot app |
| `automation` | TestNG suites: `ui`, `api`, `integration`, `concurrency`, `mocks`, `config`, `utils` |

## Prerequisites

- JDK 26 (`java -version`, `javac -version`)
- Maven not required — use the bundled wrapper `./mvnw`

## Configuration

Copy `.env.example` to `.env` and fill in credentials. Never commit `.env`.

Automation config resolves in order: `-Dkey=value` → env var (`base.url` → `BASE_URL`) → `automation/src/test/resources/test.properties`. Credentials have no defaults.

## Build

```bash
./mvnw clean compile
./mvnw clean test-compile
```

Automation tests are skipped by default because they need a running environment:

```bash
./mvnw -pl automation test -DskipAutomation=false -Dgroups=smoke
```
