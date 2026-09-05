Cloud Based Meta File Storage Service

A cloud-based file storage and management system inspired by applications like Google Drive.

Project Overview

This project provides a web-based platform where users can securely manage their files and folders. Users can register and log in, upload and download files, organize files into folders, share files with other users, manage starred files, and move deleted files to trash.

Technologies Used

Backend

- Java
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven

Frontend

- React.js
- Vite
- JavaScript
- HTML
- CSS

Main Features

- User Registration and Login
- JWT-based Authentication
- File Upload
- File Download
- File Delete and Trash
- File Restore
- Folder Management
- File Sharing
- Viewer and Editor Permissions
- Public Share Links
- File Version Management
- Starred Files
- Activity Management
- React-based User Interface

Project Structure

Cloud-Storage-Project/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
└── .gitignore

Backend

The backend is developed using Spring Boot and provides REST APIs for authentication, users, files, folders, sharing, file versions, stars, activities, and public links.

Frontend

The frontend is developed using React and Vite. It provides the user interface for interacting with the cloud storage system.

Database

PostgreSQL is used as the database, with Spring Data JPA and Hibernate for database operations.

How to Run

Backend

Open the "backend" folder in IntelliJ IDEA and run:

CloudStorageApplication.java

Make sure PostgreSQL is running and the database configuration is correctly configured in:

backend/src/main/resources/application.properties

Frontend

Open a terminal inside the "frontend" folder and run:

npm install
npm run dev

Then open the URL displayed by Vite in the browser.

Project Status

The project contains a Spring Boot backend, PostgreSQL database integration, JWT security components, and a React frontend.

Author

Himmat Singh Yadav

GitHub: Himmat-18
