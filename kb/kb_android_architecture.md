# Android Architecture Implementation Rules

## Clean Architecture Implementation

### RULE: Presentation Layer Isolation
CONTEXT: Presentation layer must only handle UI state and user interactions without business logic
REQUIREMENT: ViewModels and Composables must not contain business logic or direct data access
FAIL IF:
- ViewModels directly call repository methods without use cases
- Composables contain business logic or data transformation
- UI components access data sources directly
- Business rules implemented in presentation layer
- Domain models used directly in UI without mapping
VERIFICATION: Check ViewModels only call use cases and Composables only handle UI state
REFERENCES: ViewModel implementations, Composable functions, use case patterns

### RULE: Domain Layer Purity
CONTEXT: Domain layer must be framework-agnostic to ensure testability and reusability
REQUIREMENT: Domain layer must not import Android framework classes or external dependencies
FAIL IF:
- Use cases import Android Context or framework classes
- Domain models depend on Android-specific types
- Business logic depends on external libraries
- Domain interfaces reference Android components
- Use cases contain platform-specific implementations
VERIFICATION: Check domain layer imports and verify no Android dependencies
REFERENCES: Use case implementations, domain model definitions, business logic

### RULE: Data Layer Abstraction
CONTEXT: Data layer must provide clean abstractions for different data sources
REQUIREMENT: Repository implementations must handle data source coordination and mapping
FAIL IF:
- Repository interfaces in data layer instead of domain
- Data sources exposed directly to domain layer
- Network models used in domain without mapping
- Database entities used in business logic
- Data source switching logic in presentation layer
VERIFICATION: Check repository abstractions and data source encapsulation
REFERENCES: Repository implementations, data source abstractions, model mapping

### RULE: Dependency Inversion Compliance
CONTEXT: High-level modules must not depend on low-level modules for maintainability
REQUIREMENT: All dependencies must flow toward abstractions with proper interface usage
FAIL IF:
- Concrete implementations injected instead of interfaces
- High-level modules depend on low-level implementations
- Dependency direction flows upward in architecture
- Business logic depends on infrastructure details
- Use cases depend on specific data source implementations
VERIFICATION: Check dependency directions and interface usage throughout architecture
REFERENCES: Dependency injection configurations, interface definitions, module dependencies

### RULE: Cross-Layer Communication
CONTEXT: Layers must communicate through well-defined interfaces and contracts
REQUIREMENT: Layer communication must use proper abstractions without bypassing boundaries
FAIL IF:
- Layers communicate without proper interfaces
- Business logic scattered across multiple layers
- Data transformation logic in wrong layer
- Error handling not consistent across layers
- Layer responsibilities overlap or unclear
VERIFICATION: Check layer communication patterns and interface definitions
REFERENCES: Layer interfaces, communication patterns, error handling

## MVVM Pattern with ViewModels

### RULE: ViewModel State Management
CONTEXT: ViewModels must manage UI state consistently using reactive patterns
REQUIREMENT: ViewModels must expose state through StateFlow and handle events properly
FAIL IF:
- ViewModels expose mutable state directly to UI
- State updates not thread-safe or atomic
- Multiple sources of truth for same UI state
- ViewModel state not properly initialized
- State changes not properly propagated to UI
VERIFICATION: Check StateFlow usage and state management patterns in ViewModels
REFERENCES: ViewModel implementations, state management patterns, UI state classes

### RULE: ViewModel Lifecycle Awareness
CONTEXT: ViewModels must properly handle Android lifecycle events and cleanup
REQUIREMENT: ViewModels must use viewModelScope and handle lifecycle events correctly
FAIL IF:
- Long-running operations not cancelled on ViewModel clear
- Memory leaks from ViewModel to UI components
- Lifecycle events not properly handled
- Background operations continue after ViewModel destruction
- Resources not properly released in onCleared
VERIFICATION: Check viewModelScope usage and lifecycle event handling
REFERENCES: ViewModel lifecycle management, scope handling, resource cleanup

### RULE: ViewModel Dependency Injection
CONTEXT: ViewModels must be properly injected with Hilt for testability
REQUIREMENT: ViewModels must use @HiltViewModel annotation with constructor injection
FAIL IF:
- ViewModels created manually without Hilt
- Dependencies injected using field injection
- ViewModel factory patterns used instead of Hilt
- Circular dependencies in ViewModel injection
- Test doubles cannot be injected for testing
VERIFICATION: Check @HiltViewModel usage and constructor injection patterns
REFERENCES: ViewModel injection configurations, Hilt setup, testing patterns

### RULE: ViewModel Error Handling
CONTEXT: ViewModels must handle errors consistently and provide user feedback
REQUIREMENT: ViewModels must catch exceptions and convert to appropriate UI state
FAIL IF:
- Unhandled exceptions crash the application
- Error states not properly represented in UI state
- Error messages not user-friendly
- Recovery mechanisms not provided
- Error handling inconsistent across ViewModels
VERIFICATION: Check error handling patterns and UI state error representations
REFERENCES: Error handling implementations, UI state definitions, exception management

## Multi-Module Architecture

### RULE: Module Dependency Graph
CONTEXT: Module dependencies must form a directed acyclic graph without cycles
REQUIREMENT: Feature modules must not depend on other feature modules directly
FAIL IF:
- Circular dependencies between modules
- Feature modules depend on other feature modules
- Core modules depend on feature modules
- App module contains business logic
- Module boundaries not clearly defined
VERIFICATION: Run dependency graph analysis and check module import statements
REFERENCES: Module build.gradle files, dependency configurations, module structure

### RULE: Feature Module Organization
CONTEXT: Feature modules must be self-contained with clear responsibilities
REQUIREMENT: Each feature must contain its own presentation, domain, and data components
FAIL IF:
- Feature logic scattered across multiple modules
- Shared business logic in feature modules
- Feature modules missing required components
- Cross-feature communication without proper abstractions
- Feature module responsibilities overlap
VERIFICATION: Check feature module structure and component organization
REFERENCES: Feature module implementations, component organization, module boundaries

### RULE: Core Module Design
CONTEXT: Core modules must provide shared functionality without feature-specific logic
REQUIREMENT: Core modules must contain only reusable components and utilities
FAIL IF:
- Feature-specific logic in core modules
- Core modules depend on feature modules
- Business logic specific to one feature in core
- Core module responsibilities unclear or overlapping
- Shared resources not properly abstracted
VERIFICATION: Check core module contents and dependency relationships
REFERENCES: Core module implementations, shared component definitions, utility functions

### RULE: Module Communication Patterns
CONTEXT: Modules must communicate through well-defined interfaces and navigation
REQUIREMENT: Inter-module communication must use dependency injection and navigation
FAIL IF:
- Direct class references between feature modules
- Static method calls for cross-module communication
- Shared mutable state between modules
- Module communication bypasses proper abstractions
- Navigation not properly abstracted between modules
VERIFICATION: Check inter-module communication patterns and navigation setup
REFERENCES: Navigation configurations, module interfaces, communication patterns

### RULE: Build Configuration Management
CONTEXT: Module build configurations must be consistent and optimized
REQUIREMENT: Build configurations must be shared and properly organized across modules
FAIL IF:
- Duplicate build configuration across modules
- Inconsistent dependency versions between modules
- Build optimization not applied consistently
- Module-specific configurations not properly isolated
- Build performance not optimized for multi-module setup
VERIFICATION: Check build.gradle files and dependency version consistency
REFERENCES: Build configuration files, dependency management, build optimization

## Dependency Injection with Hilt

### RULE: Component Hierarchy Design
CONTEXT: Hilt components must be properly scoped for correct instance lifecycle
REQUIREMENT: Components must be installed in appropriate Hilt components with correct scoping
FAIL IF:
- Components installed in wrong Hilt component scope
- Singleton services not properly scoped
- ViewModel dependencies not properly scoped
- Component lifecycle not matching usage requirements
- Memory leaks from incorrect scoping
VERIFICATION: Check @InstallIn annotations and component scoping
REFERENCES: Hilt module definitions, component scoping, lifecycle management

### RULE: Module Organization Strategy
CONTEXT: Hilt modules must be organized by responsibility and component type
REQUIREMENT: Modules must be clearly organized with single responsibility principle
FAIL IF:
- Multiple unrelated bindings in same module
- Module responsibilities overlap or unclear
- Binding organization not logical or consistent
- Test modules not properly separated
- Module naming not descriptive of purpose
VERIFICATION: Check module organization and binding grouping
REFERENCES: Hilt module implementations, binding organization, module structure

### RULE: Qualifier Usage Patterns
CONTEXT: Qualifiers must be used to distinguish between similar dependency types
REQUIREMENT: Qualifiers must be used when multiple implementations of same interface exist
FAIL IF:
- Multiple bindings of same type without qualifiers
- Qualifier usage inconsistent across similar cases
- Custom qualifiers not properly documented
- Qualifier naming not descriptive
- Binding conflicts not resolved with qualifiers
VERIFICATION: Check qualifier usage and binding disambiguation
REFERENCES: Qualifier definitions, binding configurations, disambiguation patterns

## Navigation Architecture

### RULE: Navigation Component Integration
CONTEXT: Navigation must be centralized and type-safe using Navigation Compose
REQUIREMENT: All navigation must use Navigation Compose with proper route definitions
FAIL IF:
- Manual fragment transactions used instead of Navigation
- Navigation logic scattered across multiple components
- Route definitions not type-safe
- Deep linking not properly configured
- Navigation state not properly managed
VERIFICATION: Check Navigation Compose usage and route definitions
REFERENCES: Navigation configurations, route definitions, navigation patterns

### RULE: Deep Linking Implementation
CONTEXT: Deep links must be properly configured for external navigation
REQUIREMENT: Deep links must be handled consistently with proper validation
FAIL IF:
- Deep link handling not implemented
- Deep link validation missing or insufficient
- Navigation state not properly restored from deep links
- Deep link conflicts not resolved
- Security considerations not addressed for deep links
VERIFICATION: Check deep link configurations and validation logic
REFERENCES: Deep link definitions, navigation handling, security validation

### RULE: Navigation State Management
CONTEXT: Navigation state must be properly managed and restored
REQUIREMENT: Navigation state must be preserved across configuration changes
FAIL IF:
- Navigation state lost on configuration changes
- Back stack not properly managed
- Navigation arguments not properly passed
- State restoration not working correctly
- Navigation history not consistent
VERIFICATION: Check navigation state preservation and back stack management
REFERENCES: Navigation state handling, configuration change management, state restoration