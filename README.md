# eCommerce Example — Vaadin + Spring Boot

A small example eCommerce-style demo built with Vaadin Flow (server-side UI), Spring Boot, Spring Data JPA and an
H2 in-memory database. It's intended as a starting point and reference for building feature-based Vaadin applications
with server-side data loading, pagination and simple CRUD examples.

## Key Features

- **Server-side UI with Vaadin Flow** (no client-side SPA framework required)
- **Grid with lazy loading & pagination**: the main list views use Vaadin's server-side data provider and
  `VaadinSpringDataHelpers.toSpringPageRequest(query)` to request only the visible page of data from the database
  for scalable data browsing (infinite/scoped data sets are supported).
- **Spring Data JPA repositories** for persistence and query support
- **H2 in-memory database** for development and tests (config in `application.properties`)
- **Feature-based package structure**: each feature contains its entity, repository, service and UI view
- **Example feature**: `examplefeature` demonstrates a `Task` entity, repository, service and a `TaskListView`
  Vaadin view with pagination/lazy-loading
- **Tests**: integration tests using `@SpringBootTest` are included for the example feature

## Project Structure

```
src
├── main/java
│   └── com.example
│       ├── base          # reusable UI helpers and layout (MainLayout, ViewToolbar)
│       ├── examplefeature # example feature with entity, service, repo and UI
│       └── Application.java # Spring Boot entry point
├── main/resources
│   ├── application.properties
│   └── META-INF/resources/styles.css
└── test/java
    └── com.example.examplefeature
        └── TaskServiceTest.java
```

## Running the Application

Development mode (from project root):

- macOS / Linux: `./mvnw`
- Windows: `mvnw.cmd` or `./mvnw.cmd`

The app will be available at: `http://localhost:8080`

Vaadin dev server and static resource compilation run automatically during development.

## Build & Docker

Build a production JAR:

```bash
./mvnw -Pproduction package
```

Build a Docker image:

```bash
docker build -t my-application:latest .
```

If you use commercial Vaadin add-ons, pass your license key as a build secret:

```bash
docker build --secret id=proKey,src=$HOME/.vaadin/proKey .
```

## Testing

Run all tests:

```bash
./mvnw test
```

Run a single test class:

```bash
./mvnw test -Dtest=TaskServiceTest
```

Run a single test method:

```bash
./mvnw test -Dtest=TaskServiceTest#tasks_are_stored_in_the_database_with_the_current_timestamp
```

## Adding a New Feature

1. Create a new package under `com.example` (e.g. `com.example.orders`)
2. Add an `Entity`, a Spring Data `Repository`, a `Service` and a Vaadin `View` (use `@Route` / `@Menu` for navigation)
3. Use `VaadinSpringDataHelpers.toSpringPageRequest(query)` in Grid data providers for server-side paging
4. Add integration tests under `test/java` for your feature's service layer

## Notes & Tips

- The `examplefeature` package is intentionally small and self-contained — use it as a template for new features
- Styles are located in `src/main/resources/META-INF/resources/styles.css`
- Use constructor injection throughout the codebase (no field injection)

---

