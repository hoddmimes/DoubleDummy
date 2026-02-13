# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BridgeAnalyzer is a Java application that finds optimal bridge card game hands given a specific set of cards. It analyzes all possible hands and determines how many tricks NS/EW can take with various trump suits or no-trump.

## Build Commands

This project uses Gradle 9.2.1 via the Gradle Wrapper.

- **Build**: `./gradlew build`
- **Run tests**: `./gradlew test`
- **Run a single test**: `./gradlew test --tests "com.hoddmimes.ClassName.testMethodName"`
- **Clean**: `./gradlew clean`
- **Compile only**: `./gradlew compileJava`

## Architecture

- Standard Java project layout (`src/main/java`, `src/test/java`)
- Group: `com.hoddmimes`
- Testing: JUnit 5 (Jupiter) with `useJUnitPlatform()`
- No framework dependencies — standalone application
