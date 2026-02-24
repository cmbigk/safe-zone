# Safe Zone - Microservices Platform with SonarQube & Jenkins CI/CD

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![SonarQube](https://img.shields.io/badge/SonarQube-Code%20Quality-4E9BCD)]()
[![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-red)]()
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue)]()

A comprehensive **DevOps demonstration project** featuring a microservices e-commerce platform with complete **SonarQube code quality analysis** and **Jenkins CI/CD pipeline** integration. Built with **Java 21**, Spring Boot, MongoDB, Kafka, and Angular.

## 🎯 Project Overview

This project demonstrates professional DevOps practices with:
- ✅ **SonarQube Integration** - Automated code quality analysis and technical debt tracking
- ✅ **Jenkins CI/CD Pipeline** - Continuous integration with quality gates
- ✅ **Code Coverage Analysis** - JaCoCo with 50% minimum threshold
- ✅ **Docker Containerization** - Complete containerized deployment
- ✅ **Microservices Architecture** - Java 21 + Spring Boot 3.2.0
- ✅ **Modern Frontend** - Angular 17 with Karma/Jasmine testing  

## 🚀 Quick Start

### 1. Start SonarQube (Code Quality Analysis)
```bash
cd deployment
./start-sonarqube.sh
```
- Access SonarQube at: http://localhost:9000
- Default credentials: `admin / admin`
- Wait 1-2 minutes for SonarQube to fully initialize

### 2. Start Jenkins (CI/CD Pipeline)
```bash
cd deployment
./start-jenkins.sh
```
- Access Jenkins at: http://localhost:8090
- Configure SonarQube connection in Jenkins settings
- Create pipelines using `Jenkinsfile.sonarqube` for each service

### 3. Start Application Services
```bash
docker-compose up -d
```
- API Gateway: http://localhost:8080
- Frontend: http://localhost:4200

## 📊 SonarQube Integration

### Features
- **Automated Code Analysis** - Every Jenkins build triggers SonarQube scan
- **Quality Gates** - Build fails if quality criteria not met
- **Code Coverage Tracking** - JaCoCo integration with 50% minimum threshold
- **Security Vulnerability Detection** - OWASP security analysis
- **Technical Debt Calculation** - Maintainability ratings
- **Multi-Project Dashboard** - Separate analysis for each microservice

### Pre-configured Projects
Each service has dedicated SonarQube configuration:
- `ecommerce-api-gateway` - API Gateway analysis
- `ecommerce-user-service` - User service analysis
- `ecommerce-product-service` - Product service analysis
- `ecommerce-media-service` - Media service analysis
- `ecommerce-frontend` - Angular frontend analysis

### Running Code Analysis

**Via Jenkins Pipeline (Recommended):**
- Use `Jenkinsfile.sonarqube` for integrated analysis
- Automatic quality gate validation
- Build artifacts and coverage reports uploaded

**Manual Analysis:**
```bash
cd api-gateway  # or any service directory
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=ecommerce-api-gateway \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token-here
```

## 🔧 Jenkins CI/CD Pipeline

### Pipeline Capabilities
- ✅ **Automated Testing** - JUnit (Java) + Jasmine/Karma (Angular)
- ✅ **SonarQube Analysis** - Code quality gates enforced
- ✅ **Code Coverage** - JaCoCo with threshold validation
- ✅ **Blue-Green Deployment** - Zero-downtime deployments with rollback
- ✅ **Multi-Environment** - Dev, staging, production configurations
- ✅ **GitHub Integration** - Webhook-triggered builds
- ✅ **Email & Slack Notifications** - Build status alerts

### Pipeline Files
- `Jenkinsfile` - Standard CI/CD pipeline
- `Jenkinsfile.sonarqube` - Enhanced pipeline with SonarQube integration
- `Jenkinsfile.fullstack` - Full-stack deployment pipeline

## 🏗️ Architecture

### Microservices (Java 21 + Spring Boot 3.2.0)
| Service | Port | Description |
|---------|------|-------------|
| **API Gateway** | 8080 | Request routing and centralized entry point |
| **User Service** | 8081 | Authentication, user management, JWT |
| **Product Service** | 8082 | Product CRUD, seller authorization |
| **Media Service** | 8083 | Image upload and storage |
| **Frontend** | 4200 | Angular 17 web application |

### Infrastructure Components
| Component | Port | Purpose |
|-----------|------|---------|
| **SonarQube** | 9000 | Code quality analysis platform |
| **PostgreSQL** | 5432 | SonarQube database |
| **Jenkins** | 8090 | CI/CD automation server |
| **MongoDB (User)** | 27017 | User service database |
| **MongoDB (Product)** | 27018 | Product service database |
| **MongoDB (Media)** | 27019 | Media service database |
| **Kafka** | 9092 | Event streaming platform |
| **Zookeeper** | 2181 | Kafka coordination |

### Technology Stack
- **Backend**: Java 21, Spring Boot 3.2.0, Spring Security, MongoDB, Kafka
- **Frontend**: Angular 17, TypeScript, RxJS
- **DevOps**: Jenkins, SonarQube, Docker, Docker Compose
- **Testing**: JUnit 5, Jasmine, Karma, JaCoCo
- **Security**: JWT, BCrypt, Role-based access control
- **Quality**: SonarQube, JaCoCo code coverage, OWASP dependency check

## ✨ Key Features

### DevOps & Quality Assurance
- **SonarQube Code Analysis** - Automated quality gates with every build
- **Code Coverage Tracking** - JaCoCo integration with 50% minimum threshold
- **Security Scanning** - OWASP dependency vulnerability detection
- **Automated Testing** - Unit, integration, and E2E tests
- **CI/CD Pipeline** - Fully automated build, test, and deployment
- **Blue-Green Deployment** - Zero-downtime deployments with rollback capability
- **Docker Containerization** - Complete infrastructure as code

### Application Features
- **User Management** - Registration, authentication (JWT), profile management
- **Product Management** - CRUD operations with seller authorization
- **Media Management** - Image upload with validation (2MB limit, type checking)
- **Role-Based Access** - CLIENT and SELLER roles with authorization
- **API Gateway** - Centralized routing and request orchestration
- **Event-Driven Architecture** - Kafka messaging for service communication

## 📋 Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Maven 3.9+ (for local development)
- Node.js 18+ (for frontend development)
- Git

## 🔍 Code Quality Standards

### Enforced Quality Gates
- **Code Coverage**: Minimum 50% (JaCoCo)
- **Duplicated Code**: < 3%
- **Code Smells**: Maintainability rating A
- **Security Hotspots**: Must be reviewed
- **Bugs**: Zero blocking/critical bugs
- **Vulnerabilities**: Zero high/critical vulnerabilities

### SonarQube Configuration
Each service includes:
- `sonar-project.properties` - Project-specific SonarQube settings
- `Jenkinsfile.sonarqube` - Pipeline with analysis integration
- Coverage exclusions for configuration/exception classes
- Binary paths for accurate analysis

## 🧪 Testing & Coverage

### Test Reports
- **JUnit XML Reports** - Detailed test execution results
- **JaCoCo Coverage** - HTML and XML coverage reports
- **Frontend Coverage** - Istanbul/Karma coverage reports
- **Historical Tracking** - Jenkins archives all test results

### Running Tests Locally

**Backend Services:**
```bash
cd api-gateway  # or any service
mvn clean test  # Unit tests only
mvn clean verify  # Unit + integration tests with coverage
```

**Frontend:**
```bash
cd frontend
npm test  # Karma tests
npm run test:coverage  # With coverage report
```

## 🚀 Deployment

### Local Development
```bash
# Start all infrastructure
docker-compose up -d

# Build and run individual service
cd user-service
mvn spring-boot:run
```

### Production Deployment (via Jenkins)
1. Push code to GitHub repository
2. Jenkins webhook triggers build automatically
3. Pipeline executes:
   - Checkout code
   - Build service
   - Run tests
   - SonarQube analysis
   - Quality gate validation
   - Docker image build
   - Blue-green deployment
4. Notifications sent on success/failure

## 📁 Project Structure

```
safe-zone/
├── api-gateway/           # API Gateway service
│   ├── Jenkinsfile       # CI/CD pipeline
│   ├── Jenkinsfile.sonarqube  # Pipeline with SonarQube
│   └── sonar-project.properties
├── user-service/         # User management service
├── product-service/      # Product management service
├── media-service/        # Media/image service
├── frontend/             # Angular web application
├── deployment/           # DevOps infrastructure
│   ├── docker-compose.jenkins.yml
│   ├── docker-compose.sonarqube.yml
│   ├── start-jenkins.sh
│   ├── start-sonarqube.sh
│   └── Jenkinsfile.fullstack
└── docker-compose.yml    # Application services
```

## 🔧 Configuration

### SonarQube Setup
1. Start SonarQube: `cd deployment && ./start-sonarqube.sh`
2. Access: http://localhost:9000 (admin/admin)
3. Generate authentication token: User > My Account > Security
4. Configure in Jenkins: Manage Jenkins > Configure System > SonarQube servers

### Jenkins Setup
1. Start Jenkins: `cd deployment && ./start-jenkins.sh`
2. Access: http://localhost:8090
3. Install required plugins: SonarQube Scanner, Docker Pipeline
4. Configure SonarQube connection (add token)
5. Create multibranch pipelines for each service
6. Configure GitHub webhook (optional)

## 📊 Monitoring & Reports

### SonarQube Dashboard
- http://localhost:9000
- View project-specific quality metrics
- Track technical debt and code smells
- Review security vulnerabilities
- Monitor code coverage trends

### Jenkins Build Reports
- Test Reports: `${BUILD_URL}testReport/`
- Coverage Reports: `${BUILD_URL}jacoco/`
- Build Console: `${BUILD_URL}console`
- Deployment Status: Pipeline visualization

## 🛠️ Troubleshooting

### SonarQube Issues
```bash
# Check logs
docker logs sonarqube

# Restart SonarQube
cd deployment
./stop-sonarqube.sh
./start-sonarqube.sh

# For Linux: Fix vm.max_map_count error
sudo sysctl -w vm.max_map_count=262144
```

### Jenkins Issues
```bash
# Check logs
docker logs jenkins

# Restart Jenkins
cd deployment
./stop-jenkins.sh
./start-jenkins.sh
```

### Application Issues
```bash
# View service logs
docker-compose logs -f user-service

# Restart services
docker-compose restart

# Clean restart
docker-compose down
docker-compose up -d
```

## 📚 Documentation

- **SonarQube Documentation**: https://docs.sonarqube.org/
- **Jenkins Pipeline**: https://www.jenkins.io/doc/book/pipeline/
- **Spring Boot**: https://docs.spring.io/spring-boot/
- **Angular**: https://angular.io/docs

## 👥 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Ensure tests pass: `mvn clean verify`
4. Ensure SonarQube quality gates pass
5. Commit changes: `git commit -m 'Add amazing feature'`
6. Push to branch: `git push origin feature/amazing-feature`
7. Open Pull Request

## 📝 License

This project is for educational and demonstration purposes.

## 🙋 Support

For issues, questions, or contributions, please open an issue in the GitHub repository.

---

**Built with ❤️ to demonstrate professional DevOps practices with SonarQube and Jenkins**
