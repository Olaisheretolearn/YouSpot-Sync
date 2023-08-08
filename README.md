# Tune Mover: YouTube to Spotify

Tune Mover is a program that automatically converts your liked videos from YouTube into a Spotify playlist. 

## Getting Started

Clone the project to your local machine for development and testing purposes.

```markdown
```bash
git clone https://github.com/olaisheretolearn/YouSpot-Sync.git
````

## Prerequisites

You need to have the following:

- Java installed on your machine.
- Spotify account.
- Spotify token and user ID.
- YouTube API key.

## Installation

- Open the project in your favorite IDE.
- Update the `Secret` class with your Spotify token, Spotify user ID, and YouTube API key.

## Usage

Simply run the `CreatePlaylist` main method. It uses the provided Spotify token and user ID to access your Spotify account and creates a playlist with the songs from your liked videos on YouTube.

## How It Works

1. Fetches your liked videos from YouTube.
2. Extracts the song and artist names from video titles.
3. Searches for each song on Spotify.
4. Creates a new Spotify playlist.
5. Adds found songs to the new Spotify playlist.

## Contributing

Contributions are welcome. For major changes, please open an issue first to discuss what you would like to change. Please make sure to update tests as appropriate.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.



