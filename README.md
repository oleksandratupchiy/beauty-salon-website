# Beauty Salon Management System

A full-stack web application designed for beauty salon management. This project streamlines the process of booking appointments, managing services, and handling client reviews.

## Features
* **Secure Authentication:** User login and registration using Spring Security and BCrypt password hashing.
* **Role-Based Access:** Distinct functionality for admins, masters, and clients.
* **Database Management:** Normalized relational database schema in PostgreSQL to manage users, appointments, services, and reviews.
* **MVC Architecture:** Clean separation of concerns for maintainable and scalable code.

## Tech Stack
* **Backend:** Java, Spring Boot, Spring Security
* **Database:** PostgreSQL
* **Build Tool:** Maven

## Getting Started
1. Clone the repository: 
   `git clone https://github.com/oleksandratupchiy/beauty-salon-website.git`
2. Configure your PostgreSQL database credentials in `src/main/resources/application.properties`.
3. Build and run the application using Maven: 
   `mvn spring-boot:run`
