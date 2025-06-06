 # Fineract Android Mobile Knowledge Base Index

## Overview
This Knowledge Base provides architectural guardrails and development guidelines for the Fineract Android Mobile Field Application - an offline-first financial services app built with Kotlin, Jetpack Compose, and Clean Architecture for field officers serving underbanked populations.

## Priority-Based Quick Start
- **P0 (Critical)**: [kb_critical.md](./kb_critical.md) - Offline-first, security, data integrity
- **P1 (Important)**: [kb_android_architecture.md](./kb_android_architecture.md) - Clean architecture, MVVM patterns
- **P2 (Recommended)**: [kb_customization.md](./kb_customization.md) - Theming, branding, optimization

## Cross-Project Integration
For multi-project tasks: **[../fineract/kb/kb_cross_project.md](../fineract/kb/kb_cross_project.md)** - Dependencies with Backend & Web

## Knowledge Base Structure

### 🎯 Core Architecture [P0]
- **[kb_critical.md](./kb_critical.md)** (18 rules)
  - Essential architectural guardrails for mobile financial app
  - Offline-first architecture patterns
  - Multi-module Clean Architecture enforcement
  - MVVM with Jetpack Compose best practices
  - **Use when**: Starting new features, architectural decisions, code reviews

### 🏗️ Android Architecture Patterns
- **[kb_android_architecture.md](./kb_android_architecture.md)** (20 rules)
  - Clean Architecture layer separation
  - MVVM pattern with ViewModels
  - Multi-module dependency management
  - Hilt dependency injection patterns
  - Navigation architecture guidelines
  - **Use when**: Implementing features, setting up modules, architecture reviews

### 🎨 Compose UI Implementation  
- **[kb_compose_ui.md](./kb_compose_ui.md)** (15 rules)
  - Jetpack Compose best practices
  - Material Design 3 implementation
  - State management and recomposition
  - Navigation and routing patterns
  - Accessibility compliance
  - **Use when**: Building UI components, screen development, UI reviews

### 🗄️ Data Layer & Offline Operations
- **[kb_data_layer.md](./kb_data_layer.md)** (12 rules)
  - Room database implementation
  - Repository pattern abstractions
  - Offline data synchronization
  - RxJava stream management
  - Data consistency strategies
  - **Use when**: Database changes, sync implementation, offline features

### 🔐 Security & Data Protection
- **[kb_security.md](./kb_security.md)** (10 rules)
  - Local data encryption strategies
  - Network security implementation
  - Biometric authentication patterns
  - Input validation and sanitization
  - Financial data protection
  - **Use when**: Security features, sensitive data handling, compliance

### 🔧 Customization & Theming
- **[kb_customization.md](./kb_customization.md)** (8 rules)
  - Theme customization patterns
  - Brand integration strategies
  - Feature configuration management
  - Asset organization principles
  - Localization implementation
  - **Use when**: Customizing app appearance, multi-tenant features, branding

### 🚀 Deployment & CI/CD
- **[kb_deployment.md](./kb_deployment.md)** (10 rules)
  - Android build optimization
  - CI/CD pipeline configuration
  - Release management strategies
  - Performance monitoring
  - Production deployment patterns
  - **Use when**: Build configuration, release preparation, CI/CD setup

### 📜 Open Source Compliance
- **[kb_open_source.md](./kb_open_source.md)** (6 rules)
  - MPL V2 license compliance
  - Contribution guidelines
  - Code quality standards
  - Community collaboration practices
  - **Use when**: Contributing code, license compliance, community interaction

## Quick Navigation by Development Context

### 🆕 New Feature Development
1. Start with: `kb_critical.md` → `kb_android_architecture.md`
2. For UI components: `kb_compose_ui.md`
3. For data features: `kb_data_layer.md`
4. For security features: `kb_security.md`

### 📱 UI Development
1. Primary: `kb_compose_ui.md`
2. Architecture: `kb_android_architecture.md` (MVVM patterns)
3. Foundation: `kb_critical.md` (state management)
4. Theming: `kb_customization.md`

### 🗄️ Data & Offline Features
1. Core: `kb_data_layer.md`
2. Architecture: `kb_critical.md` (offline-first patterns)
3. Security: `kb_security.md` (data encryption)
4. Sync: `kb_android_architecture.md` (repository patterns)

### 🔒 Security Implementation
1. Primary: `kb_security.md`
2. Foundation: `kb_critical.md` (data protection)
3. Architecture: `kb_android_architecture.md` (secure patterns)
4. Data: `kb_data_layer.md` (encryption at rest)

### 🏗️ Architecture & Module Design
1. Essential: `kb_critical.md`
2. Implementation: `kb_android_architecture.md`
3. Data patterns: `kb_data_layer.md`
4. UI patterns: `kb_compose_ui.md`

### 🎨 Customization & Branding
1. Core: `kb_customization.md`
2. UI: `kb_compose_ui.md` (theming)
3. Architecture: `kb_android_architecture.md` (module organization)
4. Foundation: `kb_critical.md`

### 🚀 Production Deployment
1. Primary: `kb_deployment.md`
2. Security: `kb_security.md` (production hardening)
3. Performance: `kb_critical.md` (optimization rules)
4. Data: `kb_data_layer.md` (database migrations)

## Rule Statistics
- **Total Rules**: ~100 architectural guardrails
- **Critical Rules**: 18 (must follow)
- **Architecture Rules**: 35 (structural patterns)
- **UI Rules**: 25 (Compose & Material Design)
- **Data Rules**: 22 (offline & sync patterns)

## Mobile-Specific Contexts

### 📶 Offline Operations
- **Primary**: `kb_critical.md` (offline-first rules)
- **Data**: `kb_data_layer.md` (sync strategies)
- **Architecture**: `kb_android_architecture.md` (repository patterns)

### 🔋 Battery & Performance
- **Critical**: `kb_critical.md` (battery optimization)
- **UI**: `kb_compose_ui.md` (recomposition efficiency)
- **Data**: `kb_data_layer.md` (background sync)

### 👥 Field Officer Workflows
- **UX**: `kb_compose_ui.md` (accessibility & usability)
- **Data**: `kb_data_layer.md` (offline data management)
- **Security**: `kb_security.md` (field security patterns)

### 🌐 Multi-Tenant Mobile
- **Architecture**: `kb_android_architecture.md` (module separation)
- **Customization**: `kb_customization.md` (tenant-specific features)
- **Data**: `kb_data_layer.md` (tenant data isolation)

## Usage Instructions
1. **Before coding**: Read relevant KB files for your development context
2. **During development**: Use RULE format for validation checks
3. **Code reviews**: Reference specific rules in feedback
4. **Architecture decisions**: Consult kb_critical.md first
5. **UI development**: Start with kb_compose_ui.md for component patterns

## Android Development Workflow
```
Feature Request → kb_index.md → Relevant KB files → Implementation → Rule verification → Testing
```

### Development Phase Mapping
- **Planning**: `kb_critical.md` + `kb_android_architecture.md`
- **Implementation**: Context-specific KB files
- **Testing**: All relevant KB files for validation
- **Review**: `kb_critical.md` for non-negotiables

## AI Development Assistant Instructions
This KB integrates with AI development tools to ensure consistent Android architecture compliance and best practices across the mobile field application.

---
*Last Updated: December 2024*  
*Android Version: API 24+ (Android 7.0+)*  
*Architecture: Offline-First Clean Architecture with Jetpack Compose*  
*Target Users: Field Officers in Remote Banking Operations*