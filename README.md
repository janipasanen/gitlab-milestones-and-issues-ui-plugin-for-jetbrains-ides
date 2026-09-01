# GitLab Milestones and Issues UI Plugin for JetBrains IDEs

A JetBrains plugin that integrates GitLab milestones and issues directly into the IDE sidebar, providing quick access to project milestones, issues, and search functionality within Rider, IntelliJ IDEA, PyCharm, and other JetBrains IDEs.

## Features

- Sidebar integration for browsing GitLab milestones and issues
- Search across projects, milestones, and issues
- Auto-refresh on window open
- Configurable GitLab server URL and private token
- Filter to show open milestones only

## Building the Plugin

```bash
# Clone the repository
git clone https://github.com/janipasanen/gitlab-milestones-and-issues-ui-plugin-for-jetbrains-ides.git
cd gitlab-milestones-and-issues-ui-plugin-for-jetbrains-ides

# Build the plugin (requires Java 21 and Gradle 9.0)
./gradlew build
```

The built plugin will be available at:
- **Plugin JAR:** `build/libs/gitlab-milestones-and-issues-ui-plugin-1.0.0.jar`
- **Distributable ZIP:** `build/distributions/gitlab-milestones-and-issues-1.0.0.zip`

## Installing the Plugin

### Method 1: Install from Disk (Recommended for Local Builds)

1. **Build the plugin first** (see Building the Plugin section above)

2. **Open your JetBrains IDE** (Rider, IntelliJ IDEA, PyCharm, etc.)

3. **Navigate to Plugins settings:**
   - **Windows/Linux:** File → Settings → Plugins
   - **macOS:** Rider/IntelliJ/PyCharm → Preferences → Plugins

4. **Install from disk:**
   - Click the ⚙️ (gear) icon in the top-right of the Plugins panel
   - Select **Install Plugin from Disk…**
   - Navigate to `build/distributions/` in the project directory
   - Select `gitlab-milestones-and-issues-1.0.0.zip`
   - Click **OK**

5. **Restart the IDE** when prompted

6. **Verify installation:**
   - After restart, check for a GitLab tool window in the left or right sidebar
   - Go to **File → Settings → GitLab** to configure your GitLab server URL and private token

### Method 2: Install from Browser (For Released Plugins)

1. Open **File → Settings → Plugins** (Windows/Linux) or **Rider → Preferences → Plugins** (macOS)
2. Click the **🔍 Browse repositories…** icon
3. Search for "GitLab Milestones and Issues"
4. Click **Install**
5. **Restart** the IDE when prompted

### Method 3: Manual ZIP Installation

1. Download the plugin ZIP file (from releases or local build)
2. Open **File → Settings → Plugins**
3. Click the 📁 (folder) icon next to the search bar
4. Select the downloaded ZIP file
5. **Restart** the IDE

## Configuration

After installing the plugin:

1. Open **File → Settings → GitLab** (Windows/Linux) or **Rider → Preferences → GitLab** (macOS)
2. Enter your **GitLab Server URL** (e.g., `https://gitlab.com` or `https://gitlab.yourcompany.com`)
3. Enter your **Private Token** (generate one at GitLab → User Settings → Access Tokens)
4. Optionally enable **Auto-refresh on window open**
5. Optionally enable **Show open milestones only by default**
6. Click **OK** to save

## Requirements

- JetBrains IDE (Rider, IntelliJ IDEA, PyCharm, etc.) version 2024.1 or later
- Java 21 (bundled with the IDE)
- GitLab account with a valid private access token

## Generating a GitLab Access Token

1. Log in to GitLab
2. Go to **User Settings** → **Access Tokens**
3. Click **Add new token**
4. Give it a name (e.g., "JetBrains IDE")
5. Select scopes: `read_api`, `read_repository`
6. Click **Create personal access token**
7. Copy and save the token (you won't see it again)

## Troubleshooting

- **"Authentication failed"** – Verify your token is valid and not expired
- **"Resource not found"** – Check your GitLab server URL (include protocol, no trailing slash)
- **Plugin not appearing** – Restart the IDE after installation, check **File → Settings → Plugins** to ensure it's enabled
- **Slow loading** – Check your network connection to the GitLab server

## Development

### Project Structure

```
src/
├── main/kotlin/co/anomaly/gitlab/
│   ├── actions/       # IDE actions (refresh, create)
│   ├── api/           # GitLab API client (OkHttp)
│   ├── models/        # Data classes (Issue, Milestone, Project)
│   ├── search/        # Search service
│   ├── services/      # GitLab services (Issue, Milestone, Project)
│   ├── settings/      # Plugin settings
│   └── ui/            # UI components (panels, dialogs)
└── test/kotlin/       # Unit tests
```

### Running Tests

```bash
./gradlew test
```

### Building for Distribution

```bash
./gradlew buildPlugin
```

The distributable ZIP will be in `build/distributions/`.

## License

This project is licensed under the [MIT License](LICENSE).

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.
