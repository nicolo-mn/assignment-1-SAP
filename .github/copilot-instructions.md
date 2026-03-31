# Copilot Instructions

## Architecture & Design Patterns
- **Architecture Style**: Microservices architecture emphasizing Domain-Driven Design (DDD) and Hexagonal Architecture (Ports and Adapters).
- **Directory Structure**: Each service (`account-service`, `dispatch-service`, `drone-service`) has its core logic separated into `application`, `domain`, and `infrastructure` packages.
  - `domain`: Contains core business logic, entities, and domain events. Dependent on nothing but standard Java and `common` utilities.
  - `application`: Use cases and orchestration. Interacts with the domain and defines interfaces (ports) for outside interactions.
  - `infrastructure`: Concrete implementations of ports (adapters), database connections, REST controllers, web servers, and entry points (`Main` classes).
- **Shared Code**: The `common` module contains shared models, DDD abstractions, and configurations used across services.

## Technology Stack
- **Language**: Java 21
- **Framework**: Eclipse Vert.x (version 5.0.x). Reactive programming is heavily used, including `io.vertx:vertx-core` and `io.vertx:vertx-web` for routing/HTTP servers.
- **Build System**: Gradle (Multi-project setup).
- **Testing**: JUnit 5 alongside Vert.x JUnit5 extensions (`io.vertx:vertx-junit5`).

## Coding Conventions
- Emphasize non-blocking, asynchronous coding patterns utilizing Vert.x constructs (e.g., `Future`, `Promise`).
- Do not pollute the `domain` logic with framework-specific annotations or Vert.x dependencies. 
- Ensure loose coupling between modules. Services should communicate primarily over standard network protocols or event buses.

## Development Workflows
Common tasks defined in the root `build.gradle`:
- Build the project: `./gradlew build`
- Run Account Service: `./gradlew runAccountService` (maps to `sap.account.infrastructure.AccountServiceMain`)
- Run Dispatch Service: `./gradlew runDispatchService`
- Run Drones: `./gradlew runDrone1` / `./gradlew runDrone2` (Runs `sap.drone.infrastructure.DroneMain` with custom args)
- Run tests: `./gradlew test`