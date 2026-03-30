# UniEarn – Student Freelancing & E-Learning Platform

## Overview
This project was developed as part of the PIDEV – 3rd Year Engineering Program at Esprit School of Engineering (Academic Year 2025–2026). 
UniEarn is a comprehensive platform designed to empower students by bridging the gap between academic learning and professional freelancing. It allows students to manage tasks, collaborate on projects, and earn while they learn.

## Features
- User Management: Secure authentication and profile management for clients and freelancers.
- Project Marketplace: Browse, apply for, and manage freelancing opportunities.
- Task Management: Structured workflow for project execution and tracking.
- Evaluation System: Integrated feedback and rating system for quality assurance.
- Payment Integration: Secure handling of transactions and escrow services.
- Real-time Communication: WebSocket-based chat for seamless collaboration.
- Document Generation: Automated PDF reports and contracts using iText and JasperReports.

## Tech Stack
### Frontend
- JavaFX: For a rich, responsive desktop user interface.
- CSS: Custom styling for a modern look and feel.

### Backend
- Java 17: Core programming language.
- MySQL: Relational database for persistent storage.
- Spring Boot (WebSockets): For real-time messaging capabilities.
- Maven: Dependency management and build automation.

## Architecture
The project follows a Model-View-Controller (MVC) design pattern to ensure separation of concerns, scalability, and maintainability. It utilizes a Service layer for business logic and a DAO/Repository layer for data access.

## Contributors
- Yassmine Tebrizi
- Intissar Ferjani
- Akrem Arbi
- Eya Ghzaiel
- Firas benAli

## Academic Context
Developed at Esprit School of Engineering – Tunisia
PIDEV – 3A18 | 2025–2026

## Getting Started
1. Clone the repository: `git clone https://github.com/Intissar-Ferjani/UniEarn-Java.git`
2. Ensure you have JDK 17 and Maven installed.
3. Set up the MySQL database using the provided `.sql` scripts.
4. Update the `.env` or configuration files with your database credentials.
5. Build the project: `mvn clean install`
6. Run the application: `mvn javafx:run`

## Acknowledgments
We would like to thank Esprit School of Engineering for providing the academic framework and resources for this project.
