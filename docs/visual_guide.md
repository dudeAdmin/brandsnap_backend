# Spring Boot Visual Learning Guide

## Table of Contents

1. [Architecture Diagrams](#architecture-diagrams)
2. [Request Flow Visualizations](#request-flow-visualizations)
3. [Database Relationships](#database-relationships)
4. [Security Flow](#security-flow)

---

## Architecture Diagrams

### Overall Application Architecture

```mermaid
graph TB
    subgraph "Frontend Layer"
        A[React App<br/>Port 5173]
    end

    subgraph "Backend Layer - Spring Boot"
        B[Controllers<br/>REST API Endpoints]
        C[Services<br/>Business Logic]
        D[Repositories<br/>Data Access]
        E[Security<br/>JWT + OAuth2]
    end

    subgraph "External Services"
        F[MySQL Database<br/>Port 3306]
        G[Google Gemini API<br/>Image Generation]
        H[Google OAuth2<br/>Authentication]
    end

    A -->|HTTP Requests| B
    B -->|Validates| E
    B -->|Calls| C
    C -->|Uses| D
    D -->|SQL Queries| F
    C -->|API Calls| G
    E -->|Verifies| H

    style A fill:#61dafb,stroke:#333,stroke-width:2px
    style B fill:#6db33f,stroke:#333,stroke-width:2px
    style C fill:#6db33f,stroke:#333,stroke-width:2px
    style D fill:#6db33f,stroke:#333,stroke-width:2px
    style E fill:#6db33f,stroke:#333,stroke-width:2px
    style F fill:#4479a1,stroke:#333,stroke-width:2px
    style G fill:#4285f4,stroke:#333,stroke-width:2px
    style H fill:#4285f4,stroke:#333,stroke-width:2px
```

### Spring Boot Layered Architecture

```mermaid
graph LR
    subgraph "Presentation Layer"
        A1[AuthController]
        A2[ProjectController]
        A3[CampaignController]
        A4[AssetController]
    end

    subgraph "Business Logic Layer"
        B1[UserService]
        B2[ProjectService]
        B3[CampaignService]
        B4[AssetService]
    end

    subgraph "Data Access Layer"
        C1[UserRepository]
        C2[ProjectRepository]
        C3[CampaignRepository]
        C4[AssetRepository]
    end

    subgraph "Domain Layer"
        D1[User Entity]
        D2[Project Entity]
        D3[Campaign Entity]
        D4[Asset Entity]
    end

    A1 --> B1
    A2 --> B2
    A3 --> B3
    A4 --> B4

    B1 --> C1
    B2 --> C2
    B3 --> C3
    B4 --> C4

    C1 --> D1
    C2 --> D2
    C3 --> D3
    C4 --> D4

    style A1 fill:#ff6b6b,stroke:#333
    style A2 fill:#ff6b6b,stroke:#333
    style A3 fill:#ff6b6b,stroke:#333
    style A4 fill:#ff6b6b,stroke:#333
    style B1 fill:#4ecdc4,stroke:#333
    style B2 fill:#4ecdc4,stroke:#333
    style B3 fill:#4ecdc4,stroke:#333
    style B4 fill:#4ecdc4,stroke:#333
    style C1 fill:#95e1d3,stroke:#333
    style C2 fill:#95e1d3,stroke:#333
    style C3 fill:#95e1d3,stroke:#333
    style C4 fill:#95e1d3,stroke:#333
    style D1 fill:#f38181,stroke:#333
    style D2 fill:#f38181,stroke:#333
    style D3 fill:#f38181,stroke:#333
    style D4 fill:#f38181,stroke:#333
```

### Dependency Injection Flow

```mermaid
graph TD
    A["Spring IoC Container"] -->|"Creates and Manages"| B["Beans"]
    B -->|"Injects into"| C["ProjectController"]
    B -->|"Injects into"| D["ProjectService"]
    B -->|"Injects into"| E["ProjectRepository"]

    C -->|"@Autowired"| F["ProjectService Bean"]
    D -->|"@Autowired"| G["ProjectRepository Bean"]
    D -->|"@Autowired"| H["UserRepository Bean"]

    style A fill:#6db33f,stroke:#333,stroke-width:3px
    style B fill:#95e1d3,stroke:#333
    style C fill:#ff6b6b,stroke:#333
    style D fill:#4ecdc4,stroke:#333
    style E fill:#95e1d3,stroke:#333
```

---

## Request Flow Visualizations

### User Registration Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant US as UserService
    participant UR as UserRepository
    participant PE as PasswordEncoder
    participant DB as MySQL Database

    C->>AC: POST /api/auth/register<br/>{username, email, password}
    AC->>US: registerUser(user)
    US->>UR: existsByUsername(username)
    UR->>DB: SELECT * FROM users WHERE username=?
    DB-->>UR: false
    UR-->>US: false
    US->>UR: existsByEmail(email)
    UR->>DB: SELECT * FROM users WHERE email=?
    DB-->>UR: false
    UR-->>US: false
    US->>PE: encode(password)
    PE-->>US: $2a$10$encrypted...
    US->>UR: save(user)
    UR->>DB: INSERT INTO users...
    DB-->>UR: User with ID=1
    UR-->>US: User object
    US-->>AC: User object
    AC-->>C: 200 OK<br/>{id, username, email}
```

### Login & JWT Generation Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant AM as AuthenticationManager
    participant US as UserDetailsService
    participant PE as PasswordEncoder
    participant JU as JwtUtils
    participant DB as Database

    C->>AC: POST /api/auth/login<br/>{username, password}
    AC->>AM: authenticate(username, password)
    AM->>US: loadUserByUsername(username)
    US->>DB: SELECT * FROM users WHERE username=?
    DB-->>US: User data
    US-->>AM: UserDetails
    AM->>PE: matches(password, encodedPassword)
    PE-->>AM: true
    AM-->>AC: Authentication object
    AC->>JU: generateJwtToken(authentication)
    JU-->>AC: JWT token
    AC-->>C: 200 OK<br/>{token, id, username, email}
```

### Authenticated Request Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant F as AuthTokenFilter
    participant JU as JwtUtils
    participant SC as SecurityContext
    participant PC as ProjectController
    participant PS as ProjectService
    participant DB as Database

    C->>F: GET /api/projects/user/1<br/>Header: Bearer <JWT>
    F->>JU: validateJwtToken(token)
    JU-->>F: true
    F->>JU: getUserNameFromJwtToken(token)
    JU-->>F: "john_doe"
    F->>SC: setAuthentication(user)
    F->>PC: Forward request
    PC->>PS: getProjectsByUser(userId)
    PS->>DB: SELECT * FROM projects WHERE user_id=?
    DB-->>PS: List of projects
    PS-->>PC: List<Project>
    PC-->>C: 200 OK<br/>[{project1}, {project2}]
```

### Asset Generation Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AssetController
    participant AS as AssetService
    participant CR as CampaignRepository
    participant API as Google Gemini API
    participant AR as AssetRepository
    participant DB as Database

    C->>AC: POST /api/assets<br/>{campaignId, prompt, inputImage}
    AC->>AS: generateAsset(campaignId, prompt, inputImage)
    AS->>CR: findById(campaignId)
    CR->>DB: SELECT * FROM campaigns WHERE id=?
    DB-->>CR: Campaign data
    CR-->>AS: Campaign object
    AS->>API: POST generateContent<br/>{prompt, image}
    API-->>AS: Generated image (base64)
    AS->>AR: save(asset)
    AR->>DB: INSERT INTO assets...
    DB-->>AR: Asset with ID
    AR-->>AS: Asset object
    AS-->>AC: Asset object
    AC-->>C: 200 OK<br/>{id, imageData, prompt}
```

---

## Database Relationships

### Entity Relationship Diagram

```mermaid
erDiagram
    USER ||--o{ PROJECT : creates
    PROJECT ||--o{ CAMPAIGN : contains
    CAMPAIGN ||--o{ ASSET : has

    USER {
        bigint id PK "Auto-increment"
        varchar username UK "Unique, not null"
        varchar password "Nullable for OAuth"
        varchar email UK "Unique, not null"
        varchar provider "LOCAL or GOOGLE"
        varchar provider_id "Google user ID"
        datetime created_at "Auto-set on create"
    }

    PROJECT {
        bigint id PK "Auto-increment"
        varchar title "Not null"
        text description "Optional"
        bigint user_id FK "References user.id"
        datetime created_at "Auto-set on create"
    }

    CAMPAIGN {
        bigint id PK "Auto-increment"
        varchar purpose "Not null"
        bigint project_id FK "References project.id"
    }

    ASSET {
        bigint id PK "Auto-increment"
        longtext image_data "Base64 encoded image"
        text prompt "AI generation prompt"
        bigint campaign_id FK "References campaign.id"
    }
```

### Cascade Delete Behavior

```mermaid
graph TD
    A[Delete User ID=1] -->|CASCADE| B[Delete All Projects<br/>by User 1]
    B -->|CASCADE| C[Delete All Campaigns<br/>in those Projects]
    C -->|CASCADE| D[Delete All Assets<br/>in those Campaigns]

    E[Delete Project ID=5] -->|CASCADE| F[Delete All Campaigns<br/>in Project 5]
    F -->|CASCADE| G[Delete All Assets<br/>in those Campaigns]

    H[Delete Campaign ID=10] -->|CASCADE| I[Delete All Assets<br/>in Campaign 10]

    style A fill:#ff6b6b,stroke:#333,stroke-width:2px
    style E fill:#ff6b6b,stroke:#333,stroke-width:2px
    style H fill:#ff6b6b,stroke:#333,stroke-width:2px
    style B fill:#ffa07a,stroke:#333
    style C fill:#ffa07a,stroke:#333
    style D fill:#ffa07a,stroke:#333
    style F fill:#ffa07a,stroke:#333
    style G fill:#ffa07a,stroke:#333
    style I fill:#ffa07a,stroke:#333
```

---

## Security Flow

### JWT Authentication Architecture

```mermaid
graph TB
    subgraph "Client Side"
        A[User Login]
        B[Store JWT in localStorage]
        C[Include JWT in Headers]
    end

    subgraph "Server Side - Security Filter Chain"
        D[AuthTokenFilter]
        E[Extract JWT from Header]
        F[Validate JWT]
        G{Valid?}
        H[Load User Details]
        I[Set Authentication]
        J[Reject Request]
    end

    subgraph "Protected Resources"
        K[Controller Methods]
    end

    A -->|POST /login| L[AuthController]
    L -->|Generate JWT| B
    B --> C
    C -->|Request with JWT| D
    D --> E
    E --> F
    F --> G
    G -->|Yes| H
    H --> I
    I --> K
    G -->|No| J
    J -->|401 Unauthorized| C

    style A fill:#61dafb,stroke:#333
    style B fill:#61dafb,stroke:#333
    style C fill:#61dafb,stroke:#333
    style D fill:#6db33f,stroke:#333,stroke-width:2px
    style G fill:#ff6b6b,stroke:#333,stroke-width:2px
    style K fill:#4ecdc4,stroke:#333
```

### OAuth2 Google Sign-In Flow

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant G as Google OAuth2
    participant B as Backend
    participant DB as Database

    U->>F: Click "Sign in with Google"
    F->>G: Redirect to Google login
    U->>G: Enter credentials
    G->>G: Authenticate user
    G-->>F: Return credential (JWT)
    F->>B: POST /api/auth/google<br/>{credential}
    B->>B: Decode JWT payload
    B->>B: Extract email
    B->>DB: Find user by email + GOOGLE provider
    alt User exists
        DB-->>B: User data
    else User doesn't exist
        B->>DB: Create new user<br/>(provider=GOOGLE)
        DB-->>B: New user data
    end
    B->>B: Generate app JWT token
    B-->>F: {token, user data}
    F->>F: Store token
    F-->>U: Redirect to dashboard
```

### Security Configuration Flow

```mermaid
graph TD
    A[Incoming Request] --> B{Public Endpoint?}
    B -->|Yes /api/auth/**| C[Allow Access]
    B -->|No| D[AuthTokenFilter]
    D --> E{JWT Valid?}
    E -->|No| F[401 Unauthorized]
    E -->|Yes| G{User Authenticated?}
    G -->|No| F
    G -->|Yes| H[Access Controller]
    H --> I[Execute Business Logic]

    style A fill:#61dafb,stroke:#333
    style B fill:#ffe66d,stroke:#333,stroke-width:2px
    style C fill:#4ecdc4,stroke:#333
    style D fill:#6db33f,stroke:#333
    style E fill:#ffe66d,stroke:#333,stroke-width:2px
    style F fill:#ff6b6b,stroke:#333
    style G fill:#ffe66d,stroke:#333,stroke-width:2px
    style H fill:#4ecdc4,stroke:#333
    style I fill:#95e1d3,stroke:#333
```

---
