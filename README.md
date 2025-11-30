# Navigator - Java/Angular Version

## Educational AI Diagnostician System

Converted from Python/Next.js to Java Spring Boot and Angular.

### Features
- **Foundational Skill Diagnostician Agent**: AI-powered student answer analysis
- **RAG System**: Retrieval Augmented Generation with Qdrant vector database
- **Quiz System**: Interactive science curriculum quizzes (Ontario Science, Grades 3-6)
- **Progress Tracking**: Longitudinal student progress monitoring

### Technology Stack

**Backend:**
- Java 17
- Spring Boot 3.2
- LangChain4j (AI orchestration)
- Qdrant (vector database)
- Apache PDFBox (PDF processing)
- Maven (build tool)

**Frontend:**
- Angular 18
- TypeScript
- RxJS
- Angular CLI

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm
- OpenAI API key
- (Optional) Cohere API key, Tavily API key

### Environment Variables

Create a `.env` file in the project root:

```bash
OPENAI_API_KEY=your-openai-key
TAVILY_API_KEY=your-tavily-key
COHERE_API_KEY=your-cohere-key
QDRANT_URL=./qdrant_local
COLLECTION_NAME=science_curriculum_g3_g6
```

### Quick Start

#### Backend (Java Spring Boot)

```bash
cd navigator-java

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on `http://localhost:8000`

#### Frontend (Angular)

```bash
cd navigator-angular

# Install dependencies
npm install

# Start development server
ng serve
```

The frontend will start on `http://localhost:4200`

### API Endpoints

- `GET /api/health` - Health check
- `POST /api/chat` - Chat with AI (streaming)
- `POST /api/rag-chat` - RAG-based chat
- `POST /api/search` - Search Qdrant vector database
- `POST /api/evaluate` - Evaluate student answers with diagnostic agent
- `GET /api/conversations/{userId}` - Get conversation history
- `GET /api/rag-status` - Get RAG system status

### Project Structure

```
navigator-java/
├── src/
│   ├── main/
│   │   ├── java/com/navigator/
│   │   │   ├── NavigatorApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── model/
│   │   │   ├── agent/
│   │   │   └── util/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md

navigator-angular/
├── src/
│   ├── app/
│   │   ├── core/
│   │   ├── features/
│   │   └── shared/
│   ├── assets/
│   └── environments/
├── angular.json
├── package.json
└── tsconfig.json
```

### Testing

**Backend:**
```bash
mvn test
```

**Frontend:**
```bash
npm test
npm run e2e
```

### Migration from Python Version

This Java/Angular version maintains API compatibility with the original Python/Next.js version. Key differences:

1. **Backend Framework**: FastAPI → Spring Boot
2. **AI Orchestration**: LangChain (Python) → LangChain4j (Java)
3. **Frontend Framework**: Next.js/React → Angular
4. **Build Tools**: uv/npm → Maven/npm

### Development

- Backend runs on port 8000
- Frontend runs on port 4200
- CORS is configured to allow all origins for development

### License

Same as original Navigator project

### Credits

Converted from the original Navigator project by vishalmysore
