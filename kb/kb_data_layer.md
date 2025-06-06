# Data Layer Implementation Rules

## Room Database Implementation

### RULE: Entity Definition and Relationships
CONTEXT: Proper entity design ensures data integrity and supports complex financial relationships
REQUIREMENT: Entities must have proper primary keys, foreign key relationships, and indices
FAIL IF:
- Entities missing primary key definitions
- Foreign key relationships not properly configured
- Database indices not defined for frequently queried columns
- Entity relationships not properly mapped with annotations
- Cascade operations not properly configured for data integrity
VERIFICATION: Check entity annotations and verify foreign key constraints in database schema
REFERENCES: Entity class definitions, database schema, relationship mappings

### RULE: DAO Implementation Patterns
CONTEXT: Data Access Objects must provide efficient and type-safe database operations
REQUIREMENT: DAOs must use suspend functions with proper query optimization and error handling
FAIL IF:
- Blocking database operations used instead of suspend functions
- Raw SQL queries used without proper parameterization
- Query methods not optimized for performance
- Error handling missing in DAO implementations
- Transaction boundaries not properly defined
VERIFICATION: Check DAO method signatures use suspend functions and proper query optimization
REFERENCES: DAO implementations, query optimization, database performance

### RULE: Database Migration Strategies
CONTEXT: Schema changes must preserve field officer data during app updates
REQUIREMENT: All schema changes must have proper migration scripts with data preservation
FAIL IF:
- Schema changes implemented without migration scripts
- Migration scripts not tested with real data scenarios
- Data loss occurs during migration process
- Migration rollback strategies not implemented
- Version conflicts not properly handled
VERIFICATION: Test migration scripts with various data scenarios and version combinations
REFERENCES: Migration class implementations, database versioning, schema evolution

### RULE: Type Converter Usage
CONTEXT: Complex data types must be properly converted for database storage
REQUIREMENT: Type converters must handle complex objects and maintain data integrity
FAIL IF:
- Complex objects stored without proper type conversion
- Type converters not handling null values properly
- Serialization not optimized for performance
- Type conversion not reversible or lossy
- Custom types not properly annotated
VERIFICATION: Check type converter implementations and verify data integrity
REFERENCES: Type converter definitions, data serialization, custom type handling

### RULE: Database Testing Patterns
CONTEXT: Database operations must be thoroughly tested for field operations reliability
REQUIREMENT: Database tests must cover CRUD operations, migrations, and edge cases
FAIL IF:
- Database operations not covered by unit tests
- Migration testing not comprehensive
- Edge cases not tested (null values, large datasets)
- Performance testing missing for database operations
- Test data not representative of real usage
VERIFICATION: Check database test coverage and verify edge case handling
REFERENCES: Database test implementations, test data setup, performance testing

## Repository Pattern & Data Sources

### RULE: Repository Abstraction Layers
CONTEXT: Repository pattern must abstract data sources for testability and flexibility
REQUIREMENT: Repository interfaces must be defined in domain layer with implementations in data layer
FAIL IF:
- Repository interfaces defined in data layer
- Repository implementations exposed to domain layer
- Data source details leaked through repository interface
- Repository methods not properly abstracted
- Business logic implemented in repository layer
VERIFICATION: Check repository interface definitions and implementation separation
REFERENCES: Repository interfaces, implementation classes, domain layer abstractions

### RULE: Local vs Remote Data Source Coordination
CONTEXT: Field operations require seamless coordination between local and remote data sources
REQUIREMENT: Data sources must be coordinated with proper fallback and caching strategies
FAIL IF:
- Local and remote data sources not properly coordinated
- Cache invalidation strategies not implemented
- Data source switching logic not transparent to consumers
- Fallback mechanisms not properly implemented
- Data consistency not maintained across sources
VERIFICATION: Check data source coordination logic and caching strategies
REFERENCES: Data source implementations, caching logic, fallback mechanisms

### RULE: Data Source Switching Logic
CONTEXT: App must seamlessly switch between online and offline data sources
REQUIREMENT: Data source switching must be transparent and maintain data consistency
FAIL IF:
- Data source switching not transparent to repository consumers
- Switching logic not based on network availability
- Data consistency not maintained during switching
- Error handling not proper during source switching
- Performance impact not considered during switching
VERIFICATION: Test data source switching scenarios and verify consistency
REFERENCES: Data source switching implementations, network state management

### RULE: Error Handling Across Data Sources
CONTEXT: Robust error handling ensures field operations continue despite data source failures
REQUIREMENT: Error handling must be consistent across all data sources with proper recovery
FAIL IF:
- Error handling inconsistent between data sources
- Network errors not properly categorized and handled
- Database errors not gracefully handled
- Error recovery mechanisms not implemented
- User feedback not provided for data source errors
VERIFICATION: Check error handling patterns and recovery mechanisms
REFERENCES: Error handling implementations, exception management, recovery strategies

## Offline Data Synchronization

### RULE: Sync Strategy Implementation
CONTEXT: Field officers need reliable data synchronization when connectivity becomes available
REQUIREMENT: Sync strategies must handle conflicts, preserve data integrity, and optimize performance
FAIL IF:
- Sync conflicts not properly detected and resolved
- Data integrity not maintained during sync operations
- Sync performance not optimized for large datasets
- Partial sync scenarios not handled properly
- Sync progress not communicated to users
VERIFICATION: Test sync operations with various conflict scenarios and data volumes
REFERENCES: Sync strategy implementations, conflict resolution, performance optimization

### RULE: Conflict Resolution Patterns
CONTEXT: Data conflicts must be resolved consistently to maintain financial data accuracy
REQUIREMENT: Conflict resolution must follow business rules and preserve data integrity
FAIL IF:
- Conflict resolution strategies not clearly defined
- Business rules not applied during conflict resolution
- Data loss occurs during conflict resolution
- Conflict resolution not auditable or traceable
- User input not considered for conflict resolution when appropriate
VERIFICATION: Test conflict resolution with various scenarios and verify business rule compliance
REFERENCES: Conflict resolution implementations, business rule definitions, audit trails

### RULE: Data Consistency Management
CONTEXT: Data consistency is critical for financial operations and regulatory compliance
REQUIREMENT: Data consistency must be maintained across all operations and sync scenarios
FAIL IF:
- Data consistency not maintained during concurrent operations
- Referential integrity violated during sync operations
- Transaction boundaries not properly defined
- Consistency checks not implemented
- Data validation not performed during sync
VERIFICATION: Check data consistency mechanisms and transaction management
REFERENCES: Consistency management, transaction handling, data validation

### RULE: Background Sync Scheduling
CONTEXT: Background sync must be efficient and not impact device performance or battery life
REQUIREMENT: Background sync must use WorkManager with proper constraints and scheduling
FAIL IF:
- Background sync not using WorkManager
- Sync constraints not properly configured
- Battery optimization not considered
- Sync frequency not optimized for data freshness vs performance
- Sync operations not cancellable when appropriate
VERIFICATION: Check WorkManager configuration and sync scheduling logic
REFERENCES: WorkManager implementations, sync scheduling, battery optimization

## RxJava Stream Management

### RULE: Observable Composition Patterns
CONTEXT: Reactive streams must be properly composed for complex data operations
REQUIREMENT: Observable chains must be properly composed with appropriate operators
FAIL IF:
- Observable chains too complex or difficult to understand
- Inappropriate operators used for data transformations
- Error handling not properly integrated into streams
- Memory leaks from improper stream composition
- Performance not optimized in observable chains
VERIFICATION: Check observable composition and verify proper operator usage
REFERENCES: Observable implementations, operator usage, stream composition

### RULE: Error Handling in Streams
CONTEXT: Error handling in reactive streams must be robust for field operations reliability
REQUIREMENT: Error handling must be integrated into streams with proper recovery mechanisms
FAIL IF:
- Errors not properly caught and handled in streams
- Error recovery not implemented in observable chains
- Error propagation not controlled properly
- Stream termination not handled gracefully
- Error context not preserved for debugging
VERIFICATION: Check error handling in observable chains and recovery mechanisms
REFERENCES: Error handling in streams, recovery patterns, exception management

### RULE: Subscription Lifecycle Management
CONTEXT: Proper subscription management prevents memory leaks and resource waste
REQUIREMENT: Subscriptions must be properly managed with lifecycle-aware disposal
FAIL IF:
- Subscriptions not disposed when no longer needed
- Memory leaks from long-running subscriptions
- Subscription lifecycle not tied to component lifecycle
- CompositeDisposable not used for multiple subscriptions
- Subscription disposal not handled in error scenarios
VERIFICATION: Check subscription disposal patterns and lifecycle management
REFERENCES: Subscription management, disposal patterns, lifecycle integration