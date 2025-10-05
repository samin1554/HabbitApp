# Habit Logging Service

This is a microservice responsible for tracking and analyzing user habits in the HabbitApp ecosystem. It listens to events from other services and maintains detailed analytics on user habits.

## Table of Contents
- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Setup and Installation](#setup-and-installation)
- [Configuration](#configuration)
- [Testing](#testing)

## Overview

The Habit Logging Service is part of a microservices-based habit tracking application. It monitors user habit completions and maintains analytics including streaks, completion history, and success rates.

Key features:
- Track habit completion events
- Calculate and maintain streaks
- Store completion logs
- Provide analytics APIs
- Communicate with other services via REST and messaging queues

## Technology Stack

- Java 17+
- Spring Boot 3.x
- MongoDB (for data persistence)
- RabbitMQ (for event messaging)
- Maven (for dependency management)
- Lombok (for reducing boilerplate code)
- Spring WebFlux (for reactive web clients)

## Architecture

This service works as part of a larger microservice ecosystem:

```
User Service (8081) ←→ Habit Logging Service (8084) → RabbitMQ
       ↑                                                ↓
Habit Activity Service (8082) ← Habit Editing Service (8083)
```

The Habit Logging Service:
1. Receives habit completion events via REST API
2. Validates users with the User Service
3. Retrieves habit details from the Habit Activity Service
4. Stores analytics data in MongoDB
5. Provides analytics APIs for frontend consumption

## API Endpoints

### Health Check
```
GET /api/habit-logs/health
```
Returns service status.

### Log Habit Completion
```
POST /api/habit-logs/complete
```
Records a habit completion event.

Request Body:
```json
{
  "habitId": "string",
  "userId": "string", 
  "completionTime": "ISO 8601 datetime",
  "notes": "string (optional)"
}
```

### Get User Habit Analytics
```
GET /api/habit-logs/user/{userId}
```
Retrieves analytics for all habits of a specific user.

### Get Specific Habit Analytics
```
GET /api/habit-logs/user/{userId}/habit/{habitId}
```
Retrieves analytics for a specific habit of a user.

## Setup and Installation

### Prerequisites
- Java 17+
- Maven 3.8+
- MongoDB
- RabbitMQ

### Installation Steps

1. Clone the repository:
```bash
git clone <repository-url>
cd habbitLoggingService
```

2. Install dependencies:
```bash
mvn clean install
```

3. Configure application properties (see [Configuration](#configuration))

4. Run the application:
```bash
mvn spring-boot:run
```
or
```bash
java -jar target/habbitLoggingService-0.0.1-SNAPSHOT.jar
```

## Configuration

The application is configured through `application.properties`:

```properties
# Application name and port
spring.application.name=habbitLoggingService
server.port=8084

# MongoDB configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=habbitlogdb

# RabbitMQ configuration
rabbitmq.queue.name=habit_log_queue
rabbitmq.exchange.name=habit_exchange
rabbitmq.routing.key=habit.log

# User service URL
user.service.url=http://localhost:8081/api/users

# Activity service URL
activity.service.url=http://localhost:8082/api/habits
```

Ensure that MongoDB and RabbitMQ are running on the specified hosts and ports.

## Testing

### Using Postman

Import the provided Postman collection or manually create requests to the endpoints listed above.

#### Health Check
```
GET http://localhost:8084/api/habit-logs/health
```

#### Log Habit Completion
```
POST http://localhost:8084/api/habit-logs/complete
Content-Type: application/json

{
  "habitId": "sample-habit-id",
  "userId": "sample-user-id",
  "completionTime": "2025-10-04T14:30:00",
  "notes": "Completed habit successfully"
}
```

#### Get User Analytics
```
GET http://localhost:8084/api/habit-logs/user/sample-user-id
```

#### Get Specific Habit Analytics
```
GET http://localhost:8084/api/habit-logs/user/sample-user-id/habit/sample-habit-id
```

### Integration Testing Process

1. Start all required services (User, Habit Activity, Habit Logging)
2. Create a user via User Service
3. Create a habit via Habit Activity Service
4. Log habit completion via Habit Logging Service
5. Retrieve analytics via Habit Logging Service

## Data Models

### HabitLogs
Main entity storing habit analytics:
- `id`: Habit ID (from Habit Activity Service)
- `userId`: User ID (from User Service)
- `title`: Habit title
- `description`: Habit description
- `frequency`: How often the habit should be performed
- `days`: Days of the week when the habit is scheduled
- `streak`: Current streak count
- `longestStreak`: Longest streak achieved
- `completionLog`: List of completion timestamps
- `lastCompletionDate`: Timestamp of last completion
- `targetCount`: Target completions per period
- `status`: Habit status (ACTIVE/INACTIVE)
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a pull request

## License

This project is proprietary and confidential. Unauthorized copying or distribution is prohibited.
