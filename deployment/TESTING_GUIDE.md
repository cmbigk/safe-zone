# Automated Testing Guide

This guide explains the automated testing infrastructure integrated into the CI/CD pipeline.

## Overview

The Jenkins pipeline now includes comprehensive automated testing for both backend and frontend:

- **Backend:** JUnit 5 tests for all microservices
- **Frontend:** Jasmine/Karma tests for Angular application
- **Coverage:** Code coverage reports for both stacks
- **Fail-Fast:** Pipeline fails immediately if any test fails
- **Reporting:** Test results published in Jenkins UI with trends

## Test Execution Flow

```
1. Code Commit → GitHub
2. Webhook Trigger → Jenkins (instant)
3. Build Stage → Compile services
4. Test Stage → Run all tests in parallel
   ├─ Backend Tests (JUnit)
   │  ├─ user-service
   │  ├─ product-service
   │  ├─ media-service
   │  └─ api-gateway
   └─ Frontend Tests (Jasmine/Karma)
5. If tests pass → Deploy
6. If tests fail → Fail build + Notify
```

## Backend Testing (JUnit 5)

### Test Location

```
<service-name>/src/test/java/com/ecommerce/<service>/
```

### Test Execution

Tests run automatically during:
1. **Pipeline Test Stage:** `mvn test`
2. **Docker Build:** `mvn clean package` (includes tests)

### Example Test Structure

```java
package com.ecommerce.userservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Service Tests")
public class UserServiceTest {
    
    @Test
    @DisplayName("Should create user successfully")
    public void testCreateUser() {
        // Arrange
        User user = new User("test@example.com", "password");
        
        // Act
        boolean created = userService.create(user);
        
        // Assert
        assertTrue(created, "User should be created");
    }
}
```

### Running Tests Locally

```bash
# Run all tests
cd user-service
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report

# Skip tests (not recommended)
mvn package -DskipTests
```

### Test Reports

**Location:** `target/surefire-reports/`
- `TEST-*.xml` - JUnit XML reports (parsed by Jenkins)
- `*.txt` - Text summaries

**Jenkins UI:**
- Dashboard → Job → Test Result
- Trends over time
- Failed test details

### Coverage Reports

**Tool:** JaCoCo (Java Code Coverage)

**Generate Coverage:**
```bash
mvn test jacoco:report
```

**View Report:**
```
target/site/jacoco/index.html
```

**Jenkins:** Published automatically after tests run

## Frontend Testing (Jasmine/Karma)

### Test Location

```
frontend/src/app/**/*.spec.ts
```

### Test Execution

**Local Development:**
```bash
cd frontend

# Interactive mode (watch for changes)
npm test

# CI mode (single run, headless)
npm run test:ci
```

**Pipeline:** Runs `npm run test:ci` automatically

### Example Test Structure

```typescript
describe('ProductService', () => {
  let service: ProductService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProductService]
    });
    service = TestBed.inject(ProductService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch products', (done) => {
    service.getProducts().subscribe(products => {
      expect(products.length).toBeGreaterThan(0);
      done();
    });
  });
});
```

### Running Tests Locally

```bash
cd frontend

# Watch mode (auto-reload)
npm test

# Single run (like CI)
npm run test:ci

# With coverage
npm run test:ci -- --code-coverage

# Specific test file
npm test -- --include='**/product.service.spec.ts'
```

### Test Configuration

**karma.conf.js:**
```javascript
module.exports = function(config) {
  config.set({
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    browsers: ['Chrome'],
    customLaunchers: {
      ChromeHeadlessCI: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-gpu']
      }
    },
    junitReporter: {
      outputDir: 'test-results',
      outputFile: 'test-results.xml'
    }
  });
};
```

**package.json:**
```json
{
  "scripts": {
    "test": "ng test",
    "test:ci": "ng test --watch=false --code-coverage --browsers=ChromeHeadlessCI"
  }
}
```

### Test Reports

**Location:** `test-results/test-results.xml`

**Jenkins UI:**
- Dashboard → Job → Test Result
- Frontend test trends
- Failed spec details

### Coverage Reports

**Tool:** Istanbul/nyc (JavaScript coverage)

**Location:** `coverage/lcov-report/index.html`

**Jenkins:** Published as HTML report after tests

**View in Jenkins:**
- Dashboard → Job → Frontend Code Coverage

## Pipeline Test Configuration

### Test Stage Structure

```groovy
stage('Backend Tests') {
    parallel {
        stage('User Service Tests') {
            steps {
                dir('user-service') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'user-service/target/surefire-reports/*.xml'
                }
            }
        }
        // ... other services
    }
}

stage('Frontend Tests') {
    steps {
        dir('frontend') {
            sh 'npm ci'
            sh 'npm run test:ci'
        }
    }
    post {
        always {
            junit 'frontend/test-results/**/*.xml'
            publishHTML([
                reportDir: 'frontend/coverage/lcov-report',
                reportFiles: 'index.html',
                reportName: 'Frontend Code Coverage'
            ])
        }
    }
}
```

### Skip Tests (Not Recommended)

```groovy
// Use pipeline parameter
parameters {
    booleanParam(name: 'SKIP_TESTS', defaultValue: false)
}

// In stage
when {
    expression { params.SKIP_TESTS == false }
}
```

## Fail-Fast Behavior

### When Tests Fail

1. **Test stage fails immediately**
2. **Pipeline stops** (no deployment)
3. **Build marked as FAILED**
4. **Notifications sent** with failure details
5. **Test reports published** for debugging

### Example Failure Output

```
❌ BUILD FAILED #18

Failed Stage: User Service Tests

📊 Test Results:
- Total: 25
- Failed: 3
- Skipped: 0
- Pass Rate: 88%

🔗 Test Report: http://jenkins:8090/job/fullstack/18/testReport/
```

### Notifications

**Console Output:**
- Failed stage name
- Test statistics
- Links to test reports

**Optional Integrations:**
- Email (uncomment in Jenkinsfile)
- Slack (uncomment in Jenkinsfile)
- Custom webhooks

## Test Best Practices

### Backend (JUnit)

✅ **Do:**
- Use `@DisplayName` for readable test names
- Follow AAA pattern (Arrange, Act, Assert)
- Test edge cases and error conditions
- Mock external dependencies
- Use `@BeforeEach` for test setup
- Keep tests independent and isolated

❌ **Don't:**
- Write tests that depend on execution order
- Use real databases (use H2 in-memory or mocks)
- Ignore test failures
- Skip tests in production builds
- Write tests without assertions

### Frontend (Jasmine/Karma)

✅ **Do:**
- Test component logic and user interactions
- Mock HTTP requests with HttpClientTestingModule
- Test error handling
- Use `done()` callback for async tests
- Test both success and failure paths
- Maintain >80% code coverage

❌ **Don't:**
- Test Angular framework internals
- Make real HTTP requests in tests
- Forget to call `done()` in async tests
- Test implementation details
- Ignore console warnings/errors

## Writing Your First Test

### Backend JUnit Test

1. **Create test file:**
   ```bash
   mkdir -p user-service/src/test/java/com/ecommerce/userservice
   touch user-service/src/test/java/com/ecommerce/userservice/MyTest.java
   ```

2. **Write test:**
   ```java
   @Test
   @DisplayName("Should validate user email")
   public void testEmailValidation() {
       String email = "test@example.com";
       assertTrue(email.contains("@"));
   }
   ```

3. **Run test:**
   ```bash
   mvn test -Dtest=MyTest
   ```

### Frontend Jasmine Test

1. **Create test file:**
   ```bash
   touch frontend/src/app/my.service.spec.ts
   ```

2. **Write test:**
   ```typescript
   describe('MyService', () => {
     it('should return data', () => {
       const result = service.getData();
       expect(result).toBeDefined();
     });
   });
   ```

3. **Run test:**
   ```bash
   npm test -- --include='**/my.service.spec.ts'
   ```

## Troubleshooting

### Backend Tests Failing

**Check:**
1. Review console output: `target/surefire-reports/*.txt`
2. Check for missing dependencies in `pom.xml`
3. Verify test database configuration
4. Check Spring Boot test annotations

**Common Issues:**
- Missing `@SpringBootTest` annotation
- Incorrect mock configurations
- Database connection issues
- Missing test dependencies

### Frontend Tests Failing

**Check:**
1. Review karma output in terminal
2. Check for missing imports in test file
3. Verify TestBed configuration
4. Check async test handling

**Common Issues:**
- Missing `TestBed.configureTestingModule()`
- Forgot `done()` in async tests
- Incorrect mock setup
- Missing dependencies

### Tests Pass Locally But Fail in CI

**Common Causes:**
1. **Timing issues:** Use proper async handling
2. **Dependencies:** Ensure all deps in `package.json`/`pom.xml`
3. **Environment:** Check environment-specific config
4. **Resources:** CI may have less memory/CPU

**Solutions:**
- Add retries for flaky tests
- Increase timeout values
- Mock external dependencies
- Check resource limits

## Monitoring Test Health

### Jenkins Test Dashboard

**Access:** Dashboard → Job Name → Test Result

**Metrics:**
- Pass/Fail trends over time
- Test duration trends
- Flaky test detection
- Coverage trends

### Setting Test Thresholds

**Example in Jenkinsfile:**
```groovy
post {
    always {
        junit testResults: '**/target/surefire-reports/*.xml',
              healthScaleFactor: 1.0,
              allowEmptyResults: true
        
        // Fail build if coverage too low
        jacoco(
            execPattern: '**/target/jacoco.exec',
            minimumBranchCoverage: '70',
            minimumLineCoverage: '80'
        )
    }
}
```

## Continuous Improvement

### Test Coverage Goals

**Target Coverage:**
- **Backend:** >80% line coverage
- **Frontend:** >80% statement coverage
- **Critical paths:** 100% coverage

### Test Performance

**Monitor:**
- Test execution time per service
- Slow tests (>5 seconds)
- Overall test stage duration

**Optimize:**
- Parallelize tests where possible
- Mock slow dependencies
- Use in-memory databases
- Cache npm dependencies

### Test Maintenance

**Regular Tasks:**
- Remove obsolete tests
- Update mocks when APIs change
- Refactor duplicate test code
- Review and fix flaky tests
- Update test documentation

## Summary

✅ **Automated Testing Implemented:**
- Backend JUnit tests run in parallel
- Frontend Jasmine/Karma tests with coverage
- Test reports published in Jenkins
- Fail-fast on test failures
- Instant webhook triggers

⚡ **Result:** High-quality, tested code deployed automatically!

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Jasmine Documentation](https://jasmine.github.io/)
- [Karma Configuration](https://karma-runner.github.io/)
- [Jenkins JUnit Plugin](https://plugins.jenkins.io/junit/)
- Example tests in codebase:
  - Backend: `user-service/src/test/java/.../ExampleTest.java`
  - Frontend: `frontend/src/app/example.spec.ts`
