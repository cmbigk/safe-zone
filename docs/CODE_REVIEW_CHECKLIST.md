# Code Review Checklist

## Purpose
This checklist ensures consistent, thorough code reviews that maintain code quality, security, and maintainability standards.

---

## 📝 Before Submitting a Pull Request

### Developer Self-Review
- [ ] Code compiles without errors or warnings
- [ ] All tests pass locally
- [ ] SonarQube analysis run locally (if possible)
- [ ] No new SonarQube violations introduced
- [ ] Branch is up to date with target branch (main/develop)
- [ ] Commit messages are clear and descriptive
- [ ] Self-review completed (read through your own changes)
- [ ] No debug code or console.log statements left
- [ ] No commented-out code (unless explicitly documented why)

### Documentation
- [ ] README updated if needed
- [ ] API documentation updated
- [ ] Inline comments explain "why" not "what"
- [ ] Complex algorithms documented
- [ ] Public APIs have JSDoc/JavaDoc comments

---

## 👀 Reviewer Checklist

### 1. Functionality ✨

#### Does It Work?
- [ ] Code accomplishes its intended purpose
- [ ] Business requirements are met
- [ ] Edge cases are handled appropriately
- [ ] Error conditions are handled gracefully
- [ ] No obvious bugs or logic errors

#### User Experience
- [ ] User-facing changes work as expected
- [ ] Error messages are user-friendly
- [ ] Loading states implemented where needed
- [ ] Responsive design maintained (frontend)

---

### 2. Code Quality 🎯

#### Readability
- [ ] Code is easy to read and understand
- [ ] Variable/function names are clear and descriptive
- [ ] Functions do one thing (Single Responsibility)
- [ ] Functions are small and focused (< 50 lines ideally)
- [ ] Code follows project conventions and style guide
- [ ] Consistent indentation and formatting

#### Maintainability
- [ ] Code is DRY (Don't Repeat Yourself) - no duplication
- [ ] Magic numbers/strings extracted to constants
- [ ] Configuration externalized (not hardcoded)
- [ ] Code is modular and reusable
- [ ] Dependencies are minimal and necessary

#### Complexity
- [ ] Cognitive complexity is reasonable
- [ ] No overly nested conditions (max 3 levels)
- [ ] No overly long methods/functions
- [ ] Complex logic is broken into smaller functions
- [ ] Algorithms are efficient

---

### 3. Security 🔒

#### Authentication & Authorization
- [ ] Authentication checks in place
- [ ] Authorization verified for all endpoints
- [ ] Session management is secure
- [ ] JWT/tokens validated properly

#### Input Validation
- [ ] All user inputs are validated
- [ ] Input sanitization implemented
- [ ] Type checking in place
- [ ] Length/range limits enforced

#### Vulnerabilities
- [ ] No SQL injection vulnerabilities
- [ ] No XSS (Cross-Site Scripting) vulnerabilities
- [ ] No CSRF (Cross-Site Request Forgery) issues
- [ ] No hardcoded credentials or secrets
- [ ] No sensitive data in logs
- [ ] No exposed API keys or tokens

#### Data Protection
- [ ] Passwords are hashed (never stored plain text)
- [ ] Sensitive data is encrypted
- [ ] HTTPS used for data transmission
- [ ] Personal data handling complies with GDPR/privacy laws

---

### 4. Performance ⚡

#### Efficiency
- [ ] No obvious performance bottlenecks
- [ ] Database queries are optimized
- [ ] Indexes used where appropriate
- [ ] N+1 query problems avoided
- [ ] Pagination implemented for large datasets

#### Resource Management
- [ ] Resources (files, connections, streams) are properly closed
- [ ] No memory leaks
- [ ] Caching implemented where beneficial
- [ ] Lazy loading used appropriately

#### Scalability
- [ ] Code will scale with increased load
- [ ] No blocking operations in async contexts
- [ ] Concurrent access handled properly

---

### 5. Testing 🧪

#### Test Coverage
- [ ] Unit tests added for new code
- [ ] Test coverage meets project standards (e.g., 80%)
- [ ] Edge cases are tested
- [ ] Error cases are tested
- [ ] Integration tests added (if applicable)

#### Test Quality
- [ ] Tests are readable and maintainable
- [ ] Test names clearly describe what's being tested
- [ ] Tests are isolated (no dependencies between tests)
- [ ] Tests use appropriate assertions
- [ ] Mock/stub dependencies appropriately
- [ ] Tests are fast and don't rely on external services

#### Test Data
- [ ] Test data is realistic
- [ ] No hardcoded test data that could fail over time
- [ ] Test cleanup properly implemented

---

### 6. Error Handling ⚠️

#### Exceptions
- [ ] Appropriate exception types used
- [ ] Exceptions provide meaningful messages
- [ ] Exceptions are caught at appropriate levels
- [ ] No swallowing exceptions silently
- [ ] Finally blocks used for cleanup

#### Logging
- [ ] Appropriate log levels used (ERROR, WARN, INFO, DEBUG)
- [ ] Sensitive data not logged
- [ ] Log messages are clear and actionable
- [ ] Sufficient context in error logs

#### Resilience
- [ ] Retry logic implemented where appropriate
- [ ] Circuit breakers considered for external calls
- [ ] Graceful degradation implemented
- [ ] Timeouts configured

---

### 7. Database & Data 💾

#### Schema Changes
- [ ] Migration scripts provided
- [ ] Backward compatibility maintained
- [ ] Rollback plan considered
- [ ] Indexes added for new queries

#### Queries
- [ ] Queries are efficient
- [ ] Proper use of transactions
- [ ] Connection pooling configured
- [ ] No SQL injection risks

#### Data Integrity
- [ ] Foreign key constraints used
- [ ] Data validation at database level
- [ ] Concurrent modifications handled
- [ ] Data consistency maintained

---

### 8. API Design 🔌

#### RESTful Principles
- [ ] Proper HTTP methods used (GET, POST, PUT, DELETE)
- [ ] Appropriate status codes returned
- [ ] Consistent URL structure
- [ ] Versioning strategy followed

#### Request/Response
- [ ] Request validation implemented
- [ ] Response format consistent
- [ ] Appropriate content types
- [ ] Error responses well-structured

#### Documentation
- [ ] API endpoints documented
- [ ] Request/response examples provided
- [ ] Authentication requirements specified
- [ ] Rate limiting documented

---

### 9. Frontend Specific (Angular) 🎨

#### Components
- [ ] Components are small and focused
- [ ] Proper use of @Input and @Output
- [ ] Change detection strategy optimized
- [ ] No memory leaks (unsubscribe from observables)

#### State Management
- [ ] State managed consistently
- [ ] Services used for shared state
- [ ] Immutability maintained

#### Styling
- [ ] SCSS follows project conventions
- [ ] No inline styles (unless necessary)
- [ ] Responsive design implemented
- [ ] Accessibility (a11y) considered

#### Performance
- [ ] OnPush change detection where appropriate
- [ ] Lazy loading implemented
- [ ] Bundle size optimized
- [ ] Images optimized

---

### 10. Backend Specific (Spring Boot) ☕

#### Architecture
- [ ] Proper layering (Controller, Service, Repository)
- [ ] Dependency injection used correctly
- [ ] Transaction boundaries appropriate
- [ ] Exception handling via @ControllerAdvice

#### Configuration
- [ ] Properties externalized
- [ ] Environment-specific configurations
- [ ] Profiles used appropriately

#### Best Practices
- [ ] Use of Spring annotations appropriate
- [ ] Bean scopes correct
- [ ] Validation annotations used (@Valid, @NotNull, etc.)

---

### 11. SonarQube Analysis 📊

#### Quality Gate
- [ ] Quality gate status is PASSED
- [ ] No new blocker issues
- [ ] No new critical issues
- [ ] Security rating A or B
- [ ] Maintainability rating A or B

#### Metrics
- [ ] Code coverage maintained or improved
- [ ] Technical debt ratio acceptable
- [ ] Duplication below threshold
- [ ] Cognitive complexity reasonable

#### Issues
- [ ] All blockers addressed
- [ ] Critical issues explained or fixed
- [ ] Security hotspots reviewed

---

### 12. DevOps & Deployment 🚀

#### Configuration
- [ ] Environment variables used for config
- [ ] Secrets managed securely
- [ ] Docker configuration correct
- [ ] Health check endpoints implemented

#### CI/CD
- [ ] Pipeline runs successfully
- [ ] Build artifacts created correctly
- [ ] Tests run in CI
- [ ] Deployment process documented

#### Monitoring
- [ ] Metrics/monitoring added for new features
- [ ] Alerts configured if needed
- [ ] Logging sufficient for troubleshooting

---

## 🎯 Priority Levels

### 🔴 Must Fix (Blocking Issues)
- Security vulnerabilities
- Blocker/Critical SonarQube issues
- Failing tests
- Breaking changes without migration path
- Hardcoded secrets

### 🟡 Should Fix (Important but not blocking)
- Major code smells
- Missing tests for critical paths
- Performance concerns
- Incomplete documentation
- Accessibility issues

### 🟢 Nice to Have (Suggestions)
- Minor refactoring opportunities
- Code style preferences
- Additional test coverage
- Documentation improvements

---

## 💬 Review Etiquette

### For Reviewers
- **Be respectful and constructive**
- Explain the "why" behind suggestions
- Distinguish between blocking issues and suggestions
- Praise good code and improvements
- Ask questions rather than make demands
- Focus on the code, not the person

### For Authors
- **Don't take feedback personally**
- Ask for clarification if feedback is unclear
- Explain your reasoning if you disagree
- Thank reviewers for their time
- Address all comments (fix or respond)
- Request re-review when ready

---

## 📊 Review Approval Criteria

### ✅ Approve when:
- All blocking issues are resolved
- Tests pass and coverage is adequate
- SonarQube quality gate passes
- Documentation is complete
- Security concerns addressed
- Code meets team standards

### ❌ Request changes when:
- Security vulnerabilities exist
- Tests are failing
- Quality gate failed
- Breaking changes undocumented
- Required functionality missing

### 💬 Comment when:
- Providing suggestions for future improvements
- Asking questions for understanding
- Highlighting good practices

---

## 🔄 Re-Review Process

After changes are made:
1. Author addresses all comments
2. Author requests re-review
3. Reviewer verifies changes
4. Repeat until approved
5. Merge when all approvals received

---

## 📚 Additional Resources

- [SonarQube Rules](http://localhost:9000/coding_rules)
- [Project Style Guide](../docs/STYLE_GUIDE.md)
- [Security Best Practices](../docs/SECURITY.md)
- [Testing Guidelines](../docs/TESTING.md)

---

## 📋 Quick Reference

**Before starting review:**
```bash
# Pull latest changes
git checkout feature-branch
git pull origin feature-branch

# Run locally
mvn clean install  # For Java
npm install && npm test  # For frontend

# Check SonarQube
# View analysis at http://localhost:9000
```

**Review template comment:**
```
## Review Summary

### ✅ Strengths
- [List what was done well]

### 🔴 Blocking Issues
- [List must-fix items]

### 🟡 Suggestions
- [List nice-to-have improvements]

### ❓ Questions
- [List any questions]
```

---

**Remember**: The goal of code review is to improve code quality, share knowledge, and build better software together! 🎯
