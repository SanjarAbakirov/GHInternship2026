# GH Internship 2026

A Spring Boot RESTful backend application developed as part of the GH Internship 2026 program.

---

## 📋 Project Overview

This project is a backend REST API built with Java and Spring Boot. It demonstrates core concepts of modern backend development including RESTful endpoint design, data persistence with JPA, and in-memory database integration using H2.

---

## 🛠️ Tech Stack

| Technology         | Version  | Purpose                          |
|--------------------|----------|----------------------------------|
| Java               | 21       | Core programming language        |
| Spring Boot        | 4.0.0    | Application framework            |
| Spring Web MVC     | —        | REST API layer                   |
| Spring Data JPA    | —        | Database access and persistence  |
| Spring Security    | —        | Authentication and authorization |
| H2 Database        | —        | In-memory database (dev/test)    |
| PostgreSQL         | —        | Production database               |
| Lombok             | —        | Boilerplate code reduction       |
| Bean Validation    | —        | Input validation                 |
| Maven              | 3.9+     | Build and dependency management  |

---

## 📁 Project Structure

```
GHInternship2026/
├── src/
│   ├── main/
│   │   ├── java/com/ghInternship/GHInternship2026/
│   │   │   ├── controller/         # REST controllers (@RestController)
│   │   │   ├── service/            # Business logic (@Service)
│   │   │   ├── repository/         # Database access (@Repository)
│   │   │   ├── entity/             # JPA entities (@Entity)
│   │   │   ├── dto/                # Data Transfer Objects
│   │   │   └── GhInternship2026Application.java  # Entry point
│   │   └── resources/
│   │       ├── application.properties  # App configuration
│   │       ├── static/                 # Static files
│   │       └── templates/              # HTML templates
│   └── test/                           # Unit and integration tests
├── .gitignore
├── pom.xml                             # Maven dependencies
├── mvnw                                # Maven wrapper (Unix)
├── mvnw.cmd                            # Maven wrapper (Windows)
└── README.md
```

---

## ⚙️ Prerequisites

Before running this project, make sure you have the following installed:

- **Java 21+** — [Download JDK](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.9+** — [Download Maven](https://maven.apache.org/download.cgi) *(or use the included `mvnw` wrapper)*
- **Git** — [Download Git](https://git-scm.com/)

Verify your installations:
```bash
java -version
mvn -v
git --version
```

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/SanjarAbakirov/GHInternship2026.git
cd GHInternship2026
git checkout second
```

### 2. Run the application

```bash
./mvnw spring-boot:run
```

> On Windows use: `mvnw.cmd spring-boot:run`

The application will start on **http://localhost:8080**

### 3. Build the project

```bash
./mvnw clean package
```

---

## 🔗 API Endpoints

| Method | Endpoint  | Description              | Auth Required |
|--------|-----------|--------------------------|---------------|
| GET    | `/hello`  | Returns "Hello World!"   | Yes           |

### Example request

```bash
curl -u admin:admin http://localhost:8080/hello
```

**Response:**
```
Hello World!
```

---

## 🗄️ Database

The application uses **H2 in-memory database** for development and testing.

- **H2 Console:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** *(empty)*

---

## 🔐 Security

Spring Security is enabled. Default development credentials:

- **Username:** `admin`
- **Password:** `admin`

---

## 📌 Git Workflow

This project follows a structured Git workflow:

- `main` — stable production-ready code
- `second` — active development branch
- Feature branches are created from `second` for new features

---

## 📄 License

This project is developed for educational purposes as part of the GH Internship 2026 program.
