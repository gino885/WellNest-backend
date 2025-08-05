# WellNest - Backend 
The backend service for WellNest, an AI-powered mental wellness companion. It's built with Java and Spring Boot to provide endpoints for AI-driven chat, sentiment analysis, and the unique generation of personalized emotional comics.

# About The Project
This project is the server-side logic for the WellNest application, developed using the Spring Boot framework. It handles all business logic, data persistence with MySQL, and communication with third-party AI services. Its most innovative feature is the ability to translate user interactions into a personalized comic strip, offering a new form of emotional reflection and guidance.

✨ Core Features & API Endpoints
The API exposes the following core functionalities:

(1) AI-Powered CBT Companion: Provides a conversational endpoint that leverages a fine-tuned GPT-4o mini model for CBT-based guidance.
![image](https://github.com/gino885/WellNest/blob/main/chatbot.jpg)

(2) Sentiment Analysis: An endpoint that accepts text from chatbot and returns an emotional analysis using a BERT model to Provides customized suggestions and tasks.
![image](https://github.com/gino885/WellNest/blob/main/task.jpg)

(3) Personalized Emotional Comic Generation: Based on the user’s prior chatbot conversations, the resulting sentiment-analysis insights, and the missions that were recommended, an AI model dynamically creates a customized comic strip that visually reflects the user’s underlying emotions.
![image](https://github.com/gino885/WellNest/blob/main/comic.jpg)

(4) Mood Assessment & Profiling: Endpoints for submitting and retrieving user psychological profiles based on the Simplified Health Scale.
![image](https://github.com/gino885/WellNest/blob/main/form.jpg)

🛠️ Technology Stack

Language: Java 

Framework: Spring Boot 

Database: MySQL 

Data Persistence: Spring Data JPA / Hibernate

Build Tool: Apache Maven

# Getting Started
Prerequisites
Make sure you have the following installed on your system:

Java Development Kit (JDK) 17 or later

Apache Maven

A running instance of MySQL Server

An API Client like Postman or curl

Installation & Configuration
Clone the repository:

```Bash

git clone https://github.com/your-username/wellnest-backend.git
cd wellnest-backend
Configure the database in application.properties:
Open src/main/resources/application.properties and update the datasource properties for your local MySQL instance.
```
```Properties

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/wellnest_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.jpa.hibernate.ddl-auto=update

# External Service API Keys
openai.api.key=your_openai_api_key
```

Build the project:

```Bash

mvn clean install
Running the Server
Run the application using the Maven Spring Boot plugin:
```
```Bash

mvn spring-boot:run
The server will start on http://localhost:8080.
```
