# Contributing

Thanks for your interest in contributing! All contributions are welcome — bug fixes, features, improvements, and documentation.

## Reporting Issues

- **Bug reports:** Include steps to reproduce, what you expected, what actually happened, and your IDE version
- **Feature requests:** Describe the use case and why it would be useful
- Search existing issues first to avoid duplicates

## Development Setup

1. Clone the repository
2. Open the project in IntelliJ IDEA or Rider (it's a Gradle project)
3. Run `./gradlew build` to verify everything works
4. All tests must pass: `./gradlew test`

## Development Workflow

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/gitlab-milestones-and-issues-ui-plugin-for-jetbrains-ides.git
   cd gitlab-milestones-and-issues-ui-plugin-for-jetbrains-ides
   ```
3. **Create a feature branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```
   Use prefixes: `feature/`, `bugfix/`, `docs/`, `improve/`
4. **Make your changes**
5. **Test your changes:**
   ```bash
   ./gradlew test          # Run all tests
   ./gradlew build         # Full build including plugin packaging
   ```
6. **Commit your changes:**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```
7. **Push to your fork:**
   ```bash
   git push origin feature/your-feature-name
   ```
8. **Submit a Pull Request** on GitHub

## Commit Message Conventions

We use [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` — New feature
- `fix:` — Bug fix
- `docs:` — Documentation changes
- `refactor:` — Code refactoring (no feature changes)
- `test:` — Adding or updating tests
- `chore:` — Build process, dependencies, tooling
- `style:` — Formatting, missing semicolons, etc (no code change)

Example:
```
feat: add support for milestone due dates filter
```

## Code Style

- Follow existing code style in the project (Kotlin conventions)
- Use 4 spaces for indentation
- Keep functions focused and under ~50 lines when possible
- Add comments for non-obvious logic

## Pull Request Guidelines

- **Link related issues** in the PR description (e.g., `Closes #42`)
- **Describe the change** — what it does and why
- **Include tests** if you're adding or changing functionality
- **Keep PRs small** — one logical change per PR
- **Rebase** your branch on the latest `master` before submitting
- Squash commits into logical units before merging

## What to Expect

1. The maintainer will review your PR within a reasonable time
2. You may be asked to make changes — that's normal!
3. Once approved, your PR will be merged
4. You'll be credited as a contributor

## Getting Help

- Open an issue for questions or discussion
- Check the [README](README.me) for setup and usage
- Feel free to ask in your PR if you're unsure about anything

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
