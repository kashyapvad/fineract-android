# Jetpack Compose UI Implementation Rules

## Composable Function Patterns

### RULE: Stateless Composable Design
CONTEXT: Stateless composables are more reusable, testable, and predictable for field operations UI
REQUIREMENT: Composables must be stateless and receive all data through parameters
FAIL IF:
- Composables create or manage their own state internally
- Business logic implemented inside composable functions
- Direct data source access from composables
- State mutations performed within composable body
- Side effects executed during composition phase
VERIFICATION: Check composables receive state as parameters and contain no business logic
REFERENCES: Composable function signatures, state management patterns, UI component design

### RULE: Composable Function Naming
CONTEXT: Clear naming conventions ensure maintainability in large field operations codebase
REQUIREMENT: Composables must follow PascalCase naming with descriptive, UI-focused names
FAIL IF:
- Composable names not descriptive of UI purpose
- Business logic terms used in UI component names
- Naming inconsistent across similar components
- Generic names like "Screen" or "Component" without context
- Abbreviations used without clear meaning
VERIFICATION: Check composable naming follows conventions and describes UI purpose
REFERENCES: Composable function definitions, naming conventions, UI component organization

### RULE: Composition Local Usage
CONTEXT: CompositionLocal provides implicit dependencies for theme and configuration
REQUIREMENT: CompositionLocal must be used sparingly for truly global UI dependencies
FAIL IF:
- CompositionLocal overused for non-global dependencies
- Business data passed through CompositionLocal
- CompositionLocal values not properly provided
- Implicit dependencies not clearly documented
- CompositionLocal used instead of explicit parameters
VERIFICATION: Check CompositionLocal usage is limited to theme and global UI configuration
REFERENCES: CompositionLocal definitions, theme configuration, implicit dependency usage

### RULE: Side Effect Management
CONTEXT: Side effects must be properly managed to prevent issues during recomposition
REQUIREMENT: Side effects must use appropriate effect APIs with proper cleanup
FAIL IF:
- Side effects executed directly in composable body
- LaunchedEffect not used for coroutine-based side effects
- DisposableEffect not used for cleanup requirements
- Effect keys not properly specified for recomposition control
- Side effects not cancelled when composable leaves composition
VERIFICATION: Check side effects use proper effect APIs with appropriate keys and cleanup
REFERENCES: Effect API usage, side effect management, cleanup patterns

### RULE: Preview Implementation
CONTEXT: Previews enable rapid UI development and testing for field operations interfaces
REQUIREMENT: All composables must have comprehensive preview implementations
FAIL IF:
- Composables missing preview functions
- Previews not covering different states and configurations
- Preview data not representative of real usage
- Previews not testing edge cases or error states
- Preview functions not properly annotated
VERIFICATION: Check preview coverage and verify previews represent real usage scenarios
REFERENCES: Preview function implementations, preview data setup, UI testing

## State Management & Recomposition

### RULE: State Hoisting Implementation
CONTEXT: Proper state hoisting ensures predictable data flow in financial UI components
REQUIREMENT: State must be hoisted to the lowest common ancestor that needs it
FAIL IF:
- State not hoisted to appropriate level in composition hierarchy
- Multiple composables managing same logical state
- State hoisting creates unnecessary recomposition
- Parent composables not controlling child state properly
- State ownership unclear or inconsistent
VERIFICATION: Check state hoisting follows single source of truth principle
REFERENCES: State hoisting patterns, composition hierarchy, state ownership

### RULE: Remember and RememberSaveable Usage
CONTEXT: Proper state preservation prevents data loss during configuration changes
REQUIREMENT: Use remember for computation caching and rememberSaveable for user data
FAIL IF:
- Expensive computations not cached with remember
- User input state not preserved with rememberSaveable
- Remember used for state that should survive configuration changes
- RememberSaveable used for non-serializable data
- State preservation inconsistent across similar components
VERIFICATION: Check remember usage for caching and rememberSaveable for user data preservation
REFERENCES: State preservation patterns, configuration change handling, user data management

### RULE: Derived State Implementation
CONTEXT: Derived state prevents unnecessary recomposition and improves performance
REQUIREMENT: Computed values must use derivedStateOf when depending on other state
FAIL IF:
- Computed values recalculated on every recomposition
- DerivedStateOf not used for expensive state calculations
- Derived state dependencies not properly specified
- State derivation logic too complex for composition
- Derived state not properly optimized for performance
VERIFICATION: Check derived state usage and verify performance optimization
REFERENCES: Derived state patterns, performance optimization, state calculation

### RULE: State Flow Integration
CONTEXT: StateFlow integration provides reactive UI updates for field operations data
REQUIREMENT: StateFlow must be collected using collectAsStateWithLifecycle
FAIL IF:
- StateFlow collected without lifecycle awareness
- State collection not cancelled when UI not visible
- Multiple collectors for same StateFlow in single composable
- State collection not properly scoped to composition lifecycle
- StateFlow collection causing memory leaks
VERIFICATION: Check StateFlow collection uses lifecycle-aware APIs
REFERENCES: StateFlow collection patterns, lifecycle integration, reactive UI updates

## Material Design 3 Implementation

### RULE: Theme System Integration
CONTEXT: Consistent theming ensures professional appearance for field officer interfaces
REQUIREMENT: All components must use Material Design 3 theme system consistently
FAIL IF:
- Hardcoded colors used instead of theme colors
- Typography not following Material Design 3 specifications
- Component styling inconsistent with theme
- Custom colors not properly integrated with theme
- Theme not supporting light and dark modes
VERIFICATION: Check theme usage consistency and Material Design 3 compliance
REFERENCES: Theme definitions, Material Design 3 specifications, color system

### RULE: Typography and Spacing
CONTEXT: Proper typography and spacing improve readability for field operations
REQUIREMENT: Typography and spacing must follow Material Design 3 guidelines
FAIL IF:
- Custom font sizes used instead of typography scale
- Spacing not following 4dp grid system
- Text styles not semantically appropriate
- Line height and letter spacing not optimized
- Typography not accessible for different screen sizes
VERIFICATION: Check typography scale usage and spacing consistency
REFERENCES: Typography definitions, spacing system, accessibility guidelines

### RULE: Component Customization
CONTEXT: Material components must be customized appropriately for field operations needs
REQUIREMENT: Material components must be customized using proper theming mechanisms
FAIL IF:
- Material components heavily modified instead of themed
- Custom components created when Material components sufficient
- Component behavior inconsistent with Material Design
- Accessibility features removed during customization
- Component customization not reusable across app
VERIFICATION: Check component customization follows Material Design principles
REFERENCES: Component theming, Material Design guidelines, accessibility standards

### RULE: Accessibility Compliance
CONTEXT: Field officers may have different accessibility needs requiring proper support
REQUIREMENT: All UI components must meet accessibility standards and guidelines
FAIL IF:
- Content descriptions missing for interactive elements
- Touch targets smaller than minimum size requirements
- Color contrast not meeting accessibility standards
- Screen reader support not properly implemented
- Keyboard navigation not supported where appropriate
VERIFICATION: Check accessibility compliance using accessibility testing tools
REFERENCES: Accessibility guidelines, content descriptions, touch target specifications

## Navigation & Routing

### RULE: Navigation Compose Integration
CONTEXT: Navigation must be consistent and type-safe for field operations workflows
REQUIREMENT: Navigation must use Navigation Compose with proper route definitions
FAIL IF:
- Navigation logic scattered across multiple composables
- Route definitions not centralized or type-safe
- Navigation state not properly managed
- Back stack manipulation not following best practices
- Navigation arguments not properly validated
VERIFICATION: Check Navigation Compose usage and route definition patterns
REFERENCES: Navigation setup, route definitions, navigation patterns

### RULE: Route Definition Patterns
CONTEXT: Clear route definitions prevent navigation errors in field operations
REQUIREMENT: Routes must be defined as constants with proper argument handling
FAIL IF:
- Route strings hardcoded throughout application
- Route arguments not properly typed or validated
- Route naming not descriptive or consistent
- Navigation arguments not properly serialized
- Route definitions not centralized for maintenance
VERIFICATION: Check route definition organization and argument handling
REFERENCES: Route definitions, navigation arguments, type safety

### RULE: Navigation Argument Handling
CONTEXT: Proper argument handling ensures data integrity during navigation
REQUIREMENT: Navigation arguments must be properly typed and validated
FAIL IF:
- Navigation arguments not properly typed
- Argument validation missing or insufficient
- Complex objects passed as navigation arguments
- Argument serialization not handled properly
- Default values not provided for optional arguments
VERIFICATION: Check navigation argument types and validation logic
REFERENCES: Navigation argument handling, type safety, validation patterns

## Performance Optimization

### RULE: Lazy Composition Patterns
CONTEXT: Lazy composition improves performance for large datasets in field operations
REQUIREMENT: Large lists and grids must use lazy composition with proper optimization
FAIL IF:
- Large datasets rendered without lazy composition
- Lazy list items not properly keyed for stability
- Item composition not optimized for performance
- Lazy list state not properly managed
- Pagination not implemented for large datasets
VERIFICATION: Check lazy composition usage and performance optimization
REFERENCES: Lazy composition patterns, performance optimization, large dataset handling

### RULE: Memory Leak Prevention
CONTEXT: Memory leaks can cause performance issues on field devices with limited resources
REQUIREMENT: Composables must not create memory leaks through improper resource management
FAIL IF:
- Resources not properly released in DisposableEffect
- Event listeners not properly removed
- Coroutines not cancelled when composable leaves composition
- Image loading not optimized for memory usage
- Large objects held in composition without proper cleanup
VERIFICATION: Check resource cleanup and memory management in composables
REFERENCES: Resource management, memory optimization, cleanup patterns