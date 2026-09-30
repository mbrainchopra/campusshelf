# CampusShelf

CampusShelf is a simple College Books & Study Materials Sharing Platform built with:

- Spring Boot
- Java 17
- Spring Data JPA
- MySQL
- Thymeleaf
- Bootstrap 5

## Main Modules

1. Student Registration and Login
2. Student Dashboard
3. Book Sharing
4. Book Search and Filtering
5. Study Material Upload
6. Study Material Search and Filtering
7. Material Download
8. Admin Dashboard
9. Admin Student/Book/Material Management

## Database

Create the database:

```sql
CREATE DATABASE campus_shelf;
```

Update `src/main/resources/application.properties` with your MySQL username and password.

## Run

Open the project in IntelliJ IDEA and run:

`CampusShelfApplication.java`

Application URL:

`http://localhost:8082`

## Admin Login

Username:

`admin`

Password:

`admin123`

## Uploaded Files

Study materials are stored in:

`uploads/materials/`

The application creates this folder automatically when the first file is uploaded.

## Notes

- No Spring Security or JWT is used; authentication is intentionally simple for a college project.
- JPA uses `ddl-auto=update`, so tables are created/updated automatically.
