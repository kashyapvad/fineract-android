# Mifos Android Mobile Field App Critical Architectural Guardrails

## Offline-First Architecture Rules

### RULE: Local Data Priority [P0]
CONTEXT: Field officers work in remote areas with unreliable network connectivity requiring complete offline functionality
REQUIREMENT: All data operations must prioritize local storage over remote API calls with cache-first strategy
FAIL IF:
- Any feature requires network connectivity to function
- Data fetched from API without checking local cache first
- Offline state not handled gracefully with user feedback
- Sync conflicts not resolved with proper merge strategies
- Local data not encrypted for sensitive financial information
VERIFICATION: Test all features with airplane mode enabled and verify local database contains necessary data
REFERENCES: Repository implementations, Room database entities, sync strategies

### RULE: Background Sync with WorkManager
CONTEXT: Data synchronization must occur when connectivity available without impacting battery or user experience
REQUIREMENT: All sync operations must use WorkManager with proper constraints and retry policies
FAIL IF:
- Sync operations block UI thread or cause ANRs
- Battery optimization constraints not considered
- Sync conflicts not resolved with proper merge strategies
- Network constraints not respected (WiFi vs cellular)
- Sync failures not handled with exponential backoff
VERIFICATION: Check WorkManager implementation with proper constraints and conflict resolution
REFERENCES: WorkManager implementations, sync repository patterns, background services

### RULE: Network State Management
CONTEXT: Field operations require clear indication of connectivity status and graceful degradation
REQUIREMENT: Application must detect network state changes and adapt functionality accordingly
FAIL IF:
- Network state changes not detected or communicated
- Features fail silently when offline
- No user feedback about connectivity status
- Sync operations attempted without network validation
- Network errors not properly categorized and handled
VERIFICATION: Test network state detection and verify UI feedback for connectivity changes
REFERENCES: NetworkManager implementations, connectivity observers, error handling

## Multi-Module Clean Architecture Rules

### RULE: Layer Dependency Direction
CONTEXT: Clean architecture ensures maintainability and testability across feature teams
REQUIREMENT: Dependencies must flow Presentation → Domain → Data with no upward dependencies
FAIL IF:
- Presentation layer directly accesses data sources or repositories
- Domain layer imports Android framework classes (Context, View, etc.)
- Data layer contains business logic or UI-related code
- Modules have circular dependencies in dependency graph
- Core modules depend on feature modules
VERIFICATION: Run dependency graph analysis and verify domain layer has no Android dependencies
REFERENCES: Module structure in /feature/, /core/, dependency injection configurations

### RULE: Use Case Implementation
CONTEXT: Business logic must be encapsulated in domain layer for reusability and testing
REQUIREMENT: All business operations must be implemented as use cases in domain layer
FAIL IF:
- Business logic implemented directly in ViewModels
- Repository methods contain business rules
- Use cases depend on Android framework classes
- Complex business operations scattered across multiple layers
- Use cases not properly tested in isolation
VERIFICATION: Check use case implementations and verify business logic encapsulation
REFERENCES: Use case implementations, domain layer structure, business logic tests

### RULE: Repository Abstraction
CONTEXT: Data access must be abstracted to support multiple data sources and testing
REQUIREMENT: Repositories must be interfaces in domain layer with implementations in data layer
FAIL IF:
- ViewModels directly access data sources
- Repository implementations in domain layer
- Concrete repository classes injected instead of interfaces
- Data source switching logic in presentation layer
- Repository methods not properly abstracted
VERIFICATION: Check repository interface definitions and implementation separation
REFERENCES: Repository interfaces, data source implementations, dependency injection

## MVVM with Jetpack Compose Rules

### RULE: Unidirectional Data Flow [P1]
CONTEXT: Predictable state management prevents UI inconsistencies critical in financial applications
REQUIREMENT: ViewModels expose UI state through StateFlow with events flowing down and state flowing up
FAIL IF:
- Composables directly modify ViewModel state variables
- Multiple sources of truth for UI state exist
- State not hoisted to appropriate level
- ViewModels hold references to UI components
- Business logic exists in Composables instead of ViewModels
VERIFICATION: Check StateFlow usage and verify Composables receive state as parameters
REFERENCES:
  - PATTERN_EXAMPLE: `SearchViewModel.kt:30-33` (@HiltViewModel with StateFlow pattern)
  - PATTERN_EXAMPLE: `NoteViewModel.kt:20-30` (ViewModel with StateFlow and asStateFlow)
  - PATTERN_EXAMPLE: `ClientListViewModel.kt:28-37` (constructor injection with repositories)
  - PATTERN_EXAMPLE: `PasscodeViewModel.kt:17-29` (StateFlow and SharedFlow pattern)
  - STATE_PATTERN: `MutableStateFlow<UiState>` with `.asStateFlow()` exposure
  - INJECTION: `@HiltViewModel class XViewModel @Inject constructor(repository: Repository)`
  - VERIFICATION_CMD: `find . -name "*ViewModel.kt" -exec grep -l "@HiltViewModel" {} \;`
  - ANTI_PATTERN: Composables directly calling ViewModel methods that modify internal state

### RULE: State Hoisting Patterns
CONTEXT: Proper state management prevents recomposition issues and ensures UI consistency
REQUIREMENT: State must be hoisted to lowest common ancestor with proper remember usage
FAIL IF:
- State not hoisted to appropriate level in composition tree
- Expensive operations performed during composition phase
- Side effects not properly managed with LaunchedEffect
- Recomposition causes performance issues or UI lag
- State not preserved across configuration changes
VERIFICATION: Use Layout Inspector to check recomposition frequency and state preservation
REFERENCES: Composable implementations, state management patterns, performance optimization

### RULE: ViewModel Lifecycle Management
CONTEXT: Proper ViewModel scoping prevents memory leaks and ensures data consistency
REQUIREMENT: ViewModels must be properly scoped with Hilt and handle lifecycle events correctly
FAIL IF:
- ViewModels not properly scoped with @HiltViewModel
- ViewModel instances created manually
- Lifecycle events not properly handled
- Memory leaks from ViewModel to UI references
- ViewModel state not cleared when appropriate
VERIFICATION: Check ViewModel scoping and lifecycle event handling
REFERENCES: ViewModel implementations, Hilt scoping, lifecycle management

## Hilt Dependency Injection Rules

### RULE: Component Scoping Strategy
CONTEXT: Proper dependency scoping prevents memory leaks and ensures correct instance lifecycle
REQUIREMENT: Dependencies must use appropriate Hilt scopes for their intended lifecycle
FAIL IF:
- Dependencies manually instantiated with constructors
- Incorrect scoping leads to memory leaks
- Circular dependencies exist in dependency graph
- Test doubles cannot be injected for testing
- Modules not properly organized by responsibility
VERIFICATION: Check Hilt scopes and run dependency graph analysis for circular dependencies
REFERENCES:
  - PATTERN_EXAMPLE: `RepositoryModule.kt:75-119` (SingletonComponent with @Singleton scope)
  - PATTERN_EXAMPLE: `LoginViewModel.kt:32-46` (@HiltViewModel injection with @ApplicationContext)
  - SCOPING_EXAMPLE: `@InstallIn(SingletonComponent::class)` for app-wide singletons
  - SCOPING_EXAMPLE: `@HiltViewModel` for ViewModel scoping with lifecycle awareness
  - MODULE_PATTERN: `@Module @InstallIn(ComponentClass::class) object ModuleName`
  - QUALIFIER_USAGE: Use qualifiers for multiple implementations of same interface
  - VERIFICATION_CMD: `find . -name "*Module.kt" -exec grep -l "@InstallIn" {} \;`
  - ANTI_PATTERN: Manual constructor injection instead of Hilt-managed dependencies

### RULE: Module Organization
CONTEXT: Clear module organization ensures maintainability and prevents configuration conflicts
REQUIREMENT: Hilt modules must be organized by responsibility with clear component installation
FAIL IF:
- Multiple modules providing same dependency type
- Modules installed in incorrect components
- Qualifier annotations missing for similar types
- Module responsibilities overlap or unclear
- Test modules not properly configured
VERIFICATION: Check module organization and component installation configurations
REFERENCES: Hilt module implementations, component configurations, qualifier usage

### RULE: Injection Point Validation
CONTEXT: Consistent dependency injection ensures testability and maintainability
REQUIREMENT: All dependencies must be injected through constructors with proper annotations
FAIL IF:
- Field injection used instead of constructor injection
- Dependencies accessed through static methods
- Service locator pattern used instead of injection
- Injection points not properly annotated
- Manual dependency resolution in production code
VERIFICATION: Check constructor injection usage and annotation configurations
REFERENCES: Injectable class constructors, Hilt annotations, dependency configurations

## Room Database Rules

### RULE: Entity Relationship Design
CONTEXT: Proper database design ensures data integrity and supports offline operations
REQUIREMENT: Entities must have proper relationships with foreign key constraints and indices
FAIL IF:
- Database operations performed on main thread
- Migration strategy not defined for schema changes
- Foreign key relationships not properly configured
- Database queries not optimized with proper indices
- Sensitive financial data stored unencrypted
VERIFICATION: Test database operations are asynchronous and verify migration scripts
REFERENCES: Entity definitions, DAO implementations, migration strategies

### RULE: DAO Implementation Patterns
CONTEXT: Data access must be efficient and support offline-first operations
REQUIREMENT: DAOs must use suspend functions with proper query optimization and error handling
FAIL IF:
- Blocking database operations used
- Queries not optimized for performance
- Error handling missing in DAO methods
- Transaction boundaries not properly defined
- Pagination not implemented for large datasets
VERIFICATION: Check DAO method signatures and query performance with large datasets
REFERENCES:
  - PATTERN_EXAMPLE: `DatabaseHelperLoan.kt:210-249` (Observable.defer pattern for async operations)
  - PATTERN_EXAMPLE: `DatabaseHelperSavings.kt:69-111` (save() and update() patterns with transactions)
  - PATTERN_EXAMPLE: `DatabaseHelperClient.kt:339-436` (complex payload save with related entities)
  - PATTERN_EXAMPLE: `DatabaseHelperGroups.kt:85-120` (SQLite.select with where clauses and ordering)
  - DBFLOW_PATTERN: `SQLite.select().from(Entity::class.java).where(condition).querySingle()`
  - ASYNC_PATTERN: `Observable.defer { /* database operation */ Observable.just(result) }`
  - VERIFICATION_CMD: `find . -name "DatabaseHelper*.kt" -exec grep -l "Observable.defer" {} \;`
  - ANTI_PATTERN: Direct database calls on main thread without Observable wrapper

### RULE: Database Migration Strategy
CONTEXT: Schema changes must not cause data loss for field officers in remote locations
REQUIREMENT: All schema changes must have proper migration scripts with data preservation
FAIL IF:
- Schema changes without migration scripts
- Migration scripts not tested with real data
- Data loss during migration process
- Migration rollback not properly handled
- Version conflicts not resolved
VERIFICATION: Test migration scripts with various data scenarios and version combinations
REFERENCES: Migration implementations, database versioning, schema evolution

## Security & Data Protection Rules

### RULE: Local Data Encryption [P0]
CONTEXT: Financial data must be protected at rest to meet regulatory requirements
REQUIREMENT: Sensitive data must be encrypted using Android Keystore or equivalent security
FAIL IF:
- Sensitive data stored in plain text
- Encryption keys stored insecurely
- Biometric authentication not properly implemented
- Data masking not applied in logs or debug output
- Backup data not properly secured
VERIFICATION: Check data encryption implementation and key storage security
REFERENCES: Encryption implementations, Keystore usage, biometric authentication

### RULE: Network Security Implementation
CONTEXT: All network communications must be secure to protect financial data in transit
REQUIREMENT: HTTPS with certificate pinning must be implemented for all API communications
FAIL IF:
- HTTP used instead of HTTPS for any communication
- SSL certificates not pinned to prevent MITM attacks
- Authentication tokens stored in plain text
- API requests can be intercepted or tampered with
- Network security bypassed in debug builds
VERIFICATION: Test network traffic uses HTTPS and verify certificate pinning prevents attacks
REFERENCES: Network security configurations, certificate pinning, authentication handling

### RULE: Input Validation and Sanitization
CONTEXT: Invalid input can cause transaction failures and security vulnerabilities
REQUIREMENT: All user inputs must be validated and sanitized before processing or storage
FAIL IF:
- User inputs not validated before processing
- SQL injection vulnerabilities exist in database queries
- Financial calculations use imprecise floating-point types
- Validation errors not user-friendly or actionable
- Data integrity constraints not enforced
VERIFICATION: Test input validation with edge cases and verify financial calculations use BigDecimal
REFERENCES: Validation implementations, input sanitization, data type usage

## Performance Optimization Rules

### RULE: Battery Life Optimization
CONTEXT: Field officers need long battery life for extended field work without charging
REQUIREMENT: Application must minimize battery usage through efficient resource management
FAIL IF:
- UI operations block main thread causing ANRs
- Memory leaks exist in ViewModels or Composables
- Large datasets loaded without pagination
- Battery-intensive operations run unnecessarily
- Background processing not optimized for battery
VERIFICATION: Use profiler to check main thread blocking and battery usage optimization
REFERENCES: Performance optimization patterns, battery usage monitoring, resource management

### RULE: Memory Management
CONTEXT: Field devices may have limited memory requiring efficient resource usage
REQUIREMENT: Application must manage memory efficiently with proper cleanup and optimization
FAIL IF:
- Memory leaks in long-running components
- Large objects not properly released
- Image loading not optimized for memory
- Collection sizes not bounded appropriately
- Memory usage grows unbounded over time
VERIFICATION: Test for memory leaks and verify memory usage patterns with profiling tools
REFERENCES: Memory management patterns, image optimization, collection management

### RULE: UI Performance Standards
CONTEXT: Smooth UI performance is critical for field officer productivity
REQUIREMENT: UI must maintain 60fps performance with smooth animations and interactions
FAIL IF:
- Frame drops during scrolling or animations
- Composition performance issues with large lists
- Image loading blocks UI thread
- Navigation transitions not smooth
- Touch responsiveness below acceptable thresholds
VERIFICATION: Use GPU profiler to confirm 60fps performance and smooth interactions
REFERENCES: UI performance optimization, Compose performance patterns, animation implementations