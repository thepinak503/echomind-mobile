# Echomind Development Guide for Agents

This document provides instructions and context for AI agents working on the Echomind mobile application.

## Project Overview
- **Project Name:** Echomind
- **Root Directory:** `/home/pinak/AndroidStudioProjects/echomind-mobile`
- **Main Module:** `:app`

## Tech Stack & Architecture
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Build System:** Gradle (Kotlin DSL)
- **Architecture:** Follow MVVM or Clean Architecture patterns as established in the project.

## Development Rules
1. **Code Style:** Maintain consistency with existing Kotlin and Compose code.
2. **UI Development:** 
    - Use Jetpack Compose.
    - Always provide `@Preview` functions for UI components.
    - Use `ui_state` to verify screen layouts.
3. **Resource Management:** 
    - Hardcoded strings are discouraged; use `strings.xml`.
    - Use theme-based colors and typography.
4. **Error Handling:** 
    - Use `analyze_file` after modifications to ensure no syntax errors or warnings were introduced.
5. **Git Workflow:**
    - Small, atomic commits are preferred.
    - Add new files to Git immediately.

## Agent Capabilities & Tools
- Use `find_declaration` and `find_usages` for navigation.
- Use `code_search` and `grep` for finding patterns.
- Use `deploy` and `adb_shell_input` for on-device testing.
- Use `render_compose_preview` for quick UI iterations.

## Instructions for Faster Development
- When starting a task, search for existing similar implementations to reuse patterns.
- Always check `logcat` if an app crash occurs during deployment.
- Keep `agents.md` updated with any new project-specific conventions.
