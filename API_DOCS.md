# Navigator Backend - API Documentation

## 🚀 Application is Running!

The Spring Boot application is now running at:
- **Base URL**: http://localhost:8000
- **Swagger UI**: http://localhost:8000/swagger-ui/index.html
- **OpenAPI Spec**: http://localhost:8000/v3/api-docs

## 📋 Available Endpoints

### Health Check
- **GET** `/api/health` - Check if the service is running
  ```bash
  curl http://localhost:8000/api/health
  ```

### Integration Tests
- **GET** `/api/test-integrations` - Test OpenAI, Cohere, and Tavily API connections
  ```bash
  curl http://localhost:8000/api/test-integrations
  ```
  
  **Response Example**:
  ```json
  {
    "openai": {
      "status": "success",
      "response": "Hello"
    },
    "cohere": {
      "status": "skipped",
      "error": "API key not configured"
    },
    "tavily": {
      "status": "skipped",
      "error": "API key not configured"
    }
  }
  ```

### Chat
- **POST** `/api/chat` - Send a chat message
- **POST** `/api/chat/stream` - Send a chat message with streaming response

### RAG (Retrieval-Augmented Generation)
- **POST** `/api/rag/upload` - Upload PDF documents
- **POST** `/api/rag/chat` - Chat with RAG context
- **GET** `/api/rag/documents` - List uploaded documents
- **DELETE** `/api/rag/documents/{documentId}` - Delete a document

### Search
- **POST** `/api/search` - Search using Tavily

### Evaluation
- **POST** `/api/evaluate` - Evaluate student answers

## 🔧 Environment Variables

Make sure these environment variables are set (check `.env` or `.env.example`):

```properties
OPENAI_API_KEY=your_openai_api_key
COHERE_API_KEY=your_cohere_api_key  # Optional
TAVILY_API_KEY=your_tavily_api_key  # Optional
QDRANT_URL=./qdrant_local
COLLECTION_NAME=science_curriculum_g3_g6
```

## 📖 Interactive API Documentation

Visit the **Swagger UI** for interactive API testing:

**http://localhost:8000/swagger-ui/index.html**

You can:
- ✅ View all available endpoints
- ✅ Test endpoints directly from the browser
- ✅ See request/response examples
- ✅ Download OpenAPI specification

## 🧪 Testing the Integration Endpoint

Test all API integrations at once:

```bash
# Using curl
curl http://localhost:8000/api/test-integrations

# Using PowerShell
Invoke-RestMethod -Uri http://localhost:8000/api/test-integrations -Method Get

# Or just visit in your browser:
# http://localhost:8000/api/test-integrations
```

## 📦 Build & Run

```bash
# Build the project
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/navigator-backend-1.0.0.jar
```

## 🎯 Quick Links

- **Application**: http://localhost:8000
- **Swagger UI**: http://localhost:8000/swagger-ui/index.html
- **Health Check**: http://localhost:8000/api/health
- **Test Integrations**: http://localhost:8000/api/test-integrations
- **OpenAPI JSON**: http://localhost:8000/v3/api-docs
- **OpenAPI YAML**: http://localhost:8000/v3/api-docs.yaml
