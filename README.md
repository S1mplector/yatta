# Anime-TUI

A terminal-based anime browser and player written in Java, following Clean Architecture principles.

## Features

- 🔍 Browse popular anime and current season releases
- 🔎 Search anime by title or keywords
- 📺 List episodes for selected anime
- ▶️ Launch external media player (MPV/VLC) for episode playback
- 🎨 Colorful terminal user interface
- 🏗️ Clean, layered architecture for maintainability and testability

## Architecture

The application follows a 4-layer Clean Architecture pattern:

```
[Adapters/Drivers: TUI, CLI, tests]
            ↓
[Infrastructure: scraper, player, config]
            ↓
[Application: use-cases, DTOs]
            ↓
[Domain: entities + ports]
```

### Layer Responsibilities

- **Domain Layer**: Pure Java entities and port interfaces
- **Application Layer**: Use-cases that orchestrate domain operations
- **Infrastructure Layer**: Real implementations (API calls, player control, configuration)
- **Adapter Layer**: User interfaces and dependency injection

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- MPV media player (recommended) or VLC

### Installation

1. Clone the repository
2. Build the project:
   ```bash
   mvn clean package
   ```
3. Run the application:
   ```bash
   java -jar target/anime-tui-0.1.0-SNAPSHOT.jar
   ```

### Windows Users

Use the provided batch script:
```cmd
run.bat
```

## Configuration

Configure the application by editing `src/main/resources/application.properties`:

```properties
# Player Configuration
player.command=mpv
player.args=--no-terminal --input-ipc-server=\\.\pipe\animetui-mpv

# Scraper Configuration
scraper.api=jikan
scraper.baseUrl=https://api.jikan.moe/v4
scraper.timeout=30000

# Cache Configuration
cache.enabled=true
cache.directory=.animetui-cache
```

### Environment Variables

You can override configuration using environment variables:

- `PLAYER_COMMAND` - Media player executable
- `SCRAPER_BASEURL` - API base URL
- `CACHE_ENABLED` - Enable/disable caching

## Usage

1. **Browse Popular Anime**: View trending and popular anime series
2. **Browse Current Season**: See what's currently airing
3. **Search**: Find anime by title or keywords
4. **Select Episodes**: Choose episodes to watch from selected anime
5. **Play**: Launch your configured media player

## Data Sources

- **Jikan API**: Fetches anime metadata from MyAnimeList
- **Stub Link Resolver**: Currently uses placeholder video links for testing

## Development

### Project Structure

```
src/
├── main/java/com/animetui/
│   ├── domain/          # Core business logic
│   │   ├── model/       # Entities (Anime, Episode, StreamLink)
│   │   └── port/        # Interfaces for external dependencies
│   ├── application/     # Use cases and DTOs
│   ├── infrastructure/  # External integrations
│   │   ├── config/      # Configuration management
│   │   ├── player/      # Media player adapters
│   │   └── scraper/     # API clients and web scrapers
│   └── adapter/         # User interfaces
│       └── tui/         # Terminal UI implementation
└── test/                # Unit and integration tests
```

### Running Tests

```bash
mvn test
```

### Building

```bash
mvn clean package
```

## Extending the Application

### Adding New Anime Sources

1. Implement the `AnimeRepository` interface
2. Update the `Main` class to wire the new implementation
3. Add configuration properties as needed

### Adding New Media Players

1. Implement the `MediaPlayerPort` interface
2. Update the player factory in `Main` class
3. Add player-specific configuration

### Adding New UIs

1. Create a new adapter package (e.g., `adapter.web`)
2. Inject the same use-cases used by the TUI
3. Update the main class or create a separate entry point

## Roadmap

- [ ] Real anime streaming source integration
- [ ] Episode progress tracking
- [ ] Download functionality
- [ ] Advanced search filters
- [ ] Multiple theme support
- [ ] Remote control API
- [ ] GUI version

## Contributing

1. Fork the repository
2. Create a feature branch
3. Follow the existing architecture patterns
4. Add tests for new functionality
5. Submit a pull request

## License

This project is for educational purposes. Please respect copyright laws and anime licensing when using this application.

## Troubleshooting

### Media Player Not Found

If you get a "Media player not available" warning:

1. Install MPV: `winget install mpv` (Windows) or `brew install mpv` (macOS)
2. Or configure a different player in `application.properties`
3. Ensure the player executable is in your system PATH

### API Rate Limiting

The Jikan API has rate limits. If you encounter errors:

1. Wait a few seconds between requests
2. Consider implementing caching (future feature)
3. Check the Jikan API status page

### Network Issues

If anime data fails to load:

1. Check your internet connection
2. Verify the API base URL in configuration
3. Check if the Jikan API is accessible from your network
