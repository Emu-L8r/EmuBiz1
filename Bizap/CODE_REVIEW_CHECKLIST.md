# Code Review Checklist

Use this checklist when reviewing pull requests or code changes.

## Architecture & Design
- [ ] Follows clean architecture principles
- [ ] Proper separation of concerns (UI/Domain/Data)
- [ ] No circular dependencies
- [ ] Dependency injection properly configured
- [ ] No god objects (methods/classes doing too much)

## Database Changes (if applicable)
- [ ] New entities registered in @Database annotation
- [ ] New DAOs have abstract methods in AppDatabase
- [ ] DAO methods provided in DatabaseModule
- [ ] Entity class names match table references in queries
- [ ] All column types are supported by Room (no custom types without converters)
- [ ] Indices created for frequently queried columns

## Code Quality
- [ ] No unused imports
- [ ] No debug statements or print statements
- [ ] Consistent naming conventions (camelCase for vars, PascalCase for classes)
- [ ] Proper error handling (try-catch where needed)
- [ ] No magic numbers (use named constants instead)
- [ ] All public APIs documented with KDoc

## Testing
- [ ] Unit tests written for business logic
- [ ] Edge cases covered
- [ ] Integration tests for complex flows
- [ ] All tests passing locally
- [ ] No skipped or disabled tests

## Performance
- [ ] No N+1 query problems
- [ ] Lazy loading used where appropriate
- [ ] No unnecessary recompositions (Compose)
- [ ] Memory leaks checked

## Documentation
- [ ] README updated (if needed)
- [ ] API documented (KDoc comments)
- [ ] Complex logic has comments explaining why
- [ ] Architecture decisions documented (if major)

## Security
- [ ] No hardcoded secrets or API keys
- [ ] No SQL injection vulnerabilities
- [ ] Proper input validation
- [ ] User data protected

## Sign-Off
- [ ] Approve only if all above items checked ✅
- [ ] Request changes if issues found
- [ ] Comment constructively on improvements
