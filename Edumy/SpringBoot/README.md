# Edumy Spring Boot foundation

This is the Spring Boot 3.3 / Java 17 Maven application alongside the
existing ASP.NET Core backend. The Auth module is included; other business
modules have not been migrated.

## Run

From this directory, set the SQL Server connection and start the application:

```powershell
$env:EDUMY_DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=EduMyDb;encrypt=true;trustServerCertificate=true"
$env:EDUMY_DB_USERNAME = "sa"
$env:EDUMY_DB_PASSWORD = "<your-local-password>"
.\mvnw.cmd spring-boot:run
```

Do not commit database credentials. The application uses the existing
SQL Server schema without creating or altering tables (`ddl-auto=none`).
EF Core migrations remain the schema source of truth until a separate,
reviewed migration plan is approved.

Health and metrics are exposed at `/actuator/health`, `/actuator/metrics`,
and `/actuator/prometheus`.

## Test

```powershell
.\mvnw.cmd test
```

Tests use an isolated H2 in-memory database through the `test` profile. This
checks application wiring and architecture rules, not connectivity or schema
compatibility with the real SQL Server database.

## Package layout

```text
src/main/java/com/edumy/
  config/          Spring configuration
  filters/         Servlet filters for request-wide concerns
  interceptors/    MVC authorization and handler concerns
  annotations/     Annotations consumed by aspects/interceptors
  aspects/         Cross-cutting logging and metrics
  common/          Shared API/error contracts
  auth/            JWT verification, token handling and identity context
  controllers/     Auth HTTP endpoints
  dto/             Auth request/response contracts
  entities/        Auth entities mapped to the existing EF Core tables
  repositories/    Auth persistence access
  services/        Auth business operations
  modules/         Future business modules
src/main/resources/
  application.yml
  application-test.yml
```

When a business module is migrated, keep its controllers, DTOs, entities,
repositories, and services under `modules/<module>/`. Controllers only route
and delegate; services contain business logic and use Spring's
`@Transactional` annotation where required. Do not put logging, authorization,
metrics, or transaction plumbing inline in service methods.

JWT verification is handled by the Auth filter. The Auth interceptor requires
authentication on `/api/**` except `/api/auth/**` and enforces
`@RequiresRoles` outside controllers. MVC errors are mapped by the shared
`@RestControllerAdvice`; request-wide IDs and JWT parsing use servlet filters.
