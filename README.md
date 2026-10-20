    # CodeCraftHub

CodeCraftHub is a simple Spring Boot REST API for tracking courses that developers want to learn.

Course data is stored in a local JSON file named `courses.json`. No database, authentication, or user management is required.

## Features

- Create courses
- View all courses
- View a specific course
- Update courses
- Delete courses
- Automatically generate course IDs
- Automatically generate creation timestamps
- Store data in `courses.json`
- Validate required fields
- Validate course status values
- Return helpful error responses
- Automatically create `courses.json` if it does not exist

## Technology Stack

- Java 17
- Spring Boot 3
- Spring Web
- Jackson JSON processing
- Maven
- Local JSON file storage

## Course Fields

Each course contains the following fields:

| Field | Required | Description |
|
---
|
---:
|
---
|
|
`id`
|
No
|
Automatically generated numeric ID
|
|
`name`
|
Yes
|
Course name
|
|
`description`
|
Yes
|
Course description
|
|
`target_date`
|
Yes
|
Target completion date in
`YYYY-MM-DD`
format
|
|
`status`
|
Yes
|
Must be
`Not Started`
,
`In Progress`
, or
`Completed`
|
|
`created_at`
|
No
|
Automatically generated timestamp
|

Example course:

```json
{
  "id": 1,
  "name": "Spring Boot REST APIs",
  "description": "Learn how to build REST APIs with Spring Boot.",
  "target_date": "2026-12-31",
  "status": "Not Started",
  "created_at": "2026-10-20T12:30:45.123456Z"
}
``` 
