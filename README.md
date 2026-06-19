# Rust Book Native

Rust Book Native is a modern, high-performance Android application designed to provide an exceptional offline reading experience for **The Rust Programming Language** book. It supports multiple languages and offers a suite of features to help you master Rust on the go.

## 🚀 Features

- **Multi-language Support**: Download and read the Rust Book in your preferred language (Danish, German, English, Spanish, Bengali, and more).
- **Offline Access**: Once downloaded, the entire book is stored locally, allowing you to read without an internet connection.
- **Modern Material 3 UI**: A beautiful, clean interface built with the latest Android design standards.
- **Dynamic Search**: Instantly search through all book chapters and pages to find exactly what you're looking for.
- **Favorites & Bookmarks**: Save your most-referenced pages to your Favorites for quick access.
- **Reading History**: Automatically tracks your recently visited pages so you can easily jump back.
- **Session Persistence**: The app remembers exactly where you left off and restores your last opened page upon restart.
- **Custom Home Page**: Set any chapter as your personal "Home" page for immediate access when you open the app.
- **Progressive Download**: Informative download and extraction process with real-time percentage and status updates.

## 🌍 Supported Languages

The app currently supports the following languages:

- **English** (Complete)
- **Deutsch** (Complete)
- **Español** (Complete)
- **Français** (Complete)
- **日本語** (Japanese - Complete)
- **简体中文** (Simplified Chinese - Complete)
- **正體中文** (Traditional Chinese - Complete)
- **한국어** (Korean - Complete)
- **Русский** (Russian - Complete)
- **Українська** (Ukrainian - Complete)
- **Polski** (Complete)
- **Português** (Complete)
- **Danske** (Complete)
- **বাংলা** (Bengali - Complete)
- **Esperanto** (Complete)
- **Farsi** (In Progress)
- **Svenska** (In Progress)

## 🛠️ Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Design System**: [Material 3](https://m3.material.io/)
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- **Networking**: Standard Java/Kotlin URL connections for lightweight downloads.
- **Persistence**: `SharedPreferences` for user settings and reading state.
- **Concurrency**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) for smooth background operations.

## 📱 Screenshots

| Language Selection | Download Progress | Book Reader |
| :---: | :---: | :---: |
| Modern selection UI with Material 3 cards. | Real-time download and extraction status. | Full-featured reader with search and navigation. |

## 🏗️ Getting Started

### Prerequisites

- Android Studio Ladybug (or newer)
- Android SDK 24+ (Minimum) / SDK 36 (Target)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/rust-book-native.git
   ```
2. Open the project in **Android Studio**.
3. Build and run the app on an emulator or a physical device.

## 📖 How it Works

1. **Select Language**: On the first launch, browse the available languages and select the one you'd like to read.
2. **Download**: The app fetches the latest book ZIP from the official repository, downloads it to a secure cache, and extracts it to your local files directory.
3. **Read**: Use the built-in search to find topics, bookmark pages, and navigate through the comprehensive Rust documentation.

## 🤝 Contributing

Contributions are welcome! If you find a bug or have a feature suggestion, please open an issue or submit a pull request.

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
