# UK Duplicate Officer Data

A Spring Boot REST API application for UK Duplicate Officer Data, built with Maven and Java 21.

## Prerequisites

- Java 21 or higher
- Maven 3.6+ (or use the Maven wrapper included)

## Getting Started

### Build the project

```bash
mvn clean install
```

### Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Hello World
```
GET http://localhost:8080/api/hello
```

Response:
```json
{
  "message": "Hello, World!",
  "status": "success"
}
```

### Hello with Name
```
GET http://localhost:8080/api/hello/{name}
```

Example:
```
GET http://localhost:8080/api/hello/John
```

Response:
```json
{
  "message": "Hello, John!",
  "status": "success"
}
```

## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ukduplicateofficerdata/
│   │   │       ├── UkDuplicateOfficerDataApplication.java
│   │   │       └── controller/
│   │   │           └── HelloController.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/ukduplicateofficerdata/
└── pom.xml
```

## Configuration

The application can be configured in `src/main/resources/application.properties`:

- `server.port`: Change the port (default: 8080)
- `spring.application.name`: Application name
- Logging levels can be adjusted as needed

## Testing

Run tests with:
```bash
mvn test
```

## Building for Production

Create an executable JAR:
```bash
mvn clean package
```

Run the JAR:
```bash
java -jar target/uk-duplicate-officer-data-0.0.1-SNAPSHOT.jar
```