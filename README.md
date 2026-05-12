# Dunya Beauty Web App

A web application for beauty salon management and appointment booking. Built as a university laboratory project to practice Spring Boot, MVC architecture, and secure user authentication.

## Features

* **Secure Authentication:** Implements user registration, login, and password recovery. Uses Spring Security for route protection, BCrypt for password hashing, and UUID tokens for email verification.
* **Role-Based Access Control (RBAC):** Restricts access to sensitive endpoints based on user roles (`ROLE_USER`, `ROLE_ADMIN`).
* **Appointment Management:** Clients can browse available services, view master profiles, and book appointments.
* **Admin Dashboard:** Provides an administrative interface to manage services, staff, and view analytical data regarding salon performance.
* **Data Export/Import:** Supports exporting financial reports and client data to Excel (`.xlsx`) format using the Apache POI library.
* **Email Integration:** Uses `JavaMailSender` to deliver transactional emails (account activation, password reset links).

## Tech Stack

* **Backend:** Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA
* **Database:** PostgreSQL
* **Frontend:** HTML5, CSS3, JavaScript, Thymeleaf
