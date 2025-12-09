# 🚀 LanCast

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=java" alt="JavaFX 21">
  <img src="https://img.shields.io/badge/Platform-Windows%20%7C%20Linux-green?style=for-the-badge" alt="Platform">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="License">
</p>

**LanCast** is a modern, cross-platform file sharing application that allows you to share files over your local network with ease. Simply drag and drop files, start the server, and share the generated URL or QR code with anyone on the same network.

---

## ✨ Features

- 📁 **Drag & Drop** - Simply drag files into the app to share them
- 🔗 **Easy Sharing** - Share via URL or QR code
- 🔒 **PIN Protection** - Secure your transfers with a 4-digit PIN
- 📊 **Transfer History** - Track all file transfers with timestamps
- 🎨 **Customizable Themes** - Dark/Light mode with multiple accent colors
- 🌐 **Cross-Platform** - Works on Windows and Linux

---

## 📋 Prerequisites

- **Java 21** or higher (with JavaFX support)
- **Maven 3.6+** (for building from source)

---

## 🖥️ Installation

### Windows

1. **Install Java 21:**
   - Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
   - Run the installer and follow the prompts
   - Verify installation:
     ```cmd
     java -version
     ```

2. **Install Maven:**
   - Download from [Apache Maven](https://maven.apache.org/download.cgi)
   - Extract to `C:\Program Files\Maven`
   - Add `C:\Program Files\Maven\bin` to your PATH environment variable
   - Verify installation:
     ```cmd
     mvn -version
     ```

3. **Clone and Run:**
   ```cmd
   git clone https://github.com/OussamaBelhane/LanCast.git
   cd LanCast
   mvn clean javafx:run
   ```

---

### Linux (Debian/Ubuntu)

1. **Install Java 21:**
   ```bash
   sudo apt update
   sudo apt install openjdk-21-jdk openjfx
   ```

2. **Install Maven:**
   ```bash
   sudo apt install maven
   ```

3. **Clone and Run:**
   ```bash
   git clone https://github.com/OussamaBelhane/LanCast.git
   cd LanCast
   mvn clean javafx:run
   ```

---

### Linux (Arch Linux)

1. **Install Java 21:**
   ```bash
   sudo pacman -S jdk21-openjdk java-openjfx
   ```

2. **Install Maven:**
   ```bash
   sudo pacman -S maven
   ```

3. **Clone and Run:**
   ```bash
   git clone https://github.com/OussamaBelhane/LanCast.git
   cd LanCast
   mvn clean javafx:run
   ```

---

### Linux (Fedora)

1. **Install Java 21:**
   ```bash
   sudo dnf install java-21-openjdk java-21-openjdk-devel
   ```

2. **Install Maven:**
   ```bash
   sudo dnf install maven
   ```

3. **Clone and Run:**
   ```bash
   git clone https://github.com/OussamaBelhane/LanCast.git
   cd LanCast
   mvn clean javafx:run
   ```

---

## 🚀 Usage

1. **Launch the application** using `mvn clean javafx:run`

2. **Add files** - Drag and drop files into the drop zone, or click "Browse" to select files

3. **Start the server** - Click the "▶ Start" button to start sharing

4. **Share the link** - Copy the URL or scan the QR code from any device on the same network

5. **Enter PIN** - Recipients enter the PIN code shown in the app to download files

---

## 📱 Accessing Shared Files

From any device on the same network:

1. Open a web browser
2. Navigate to the URL displayed in LanCast (e.g., `http://192.168.0.19:8000`)
3. Enter the 4-digit PIN
4. Click download to receive the files

---

## ⚙️ Settings

- **Theme Mode** - Toggle between Dark and Light mode
- **Accent Colors** - Choose from Purple, Blue, Pink, Green, or Orange
- **PIN Code** - Refresh to generate a new random PIN

---

## 🗂️ Project Structure

```
LanCast/
├── src/
│   └── main/
│       ├── java/com/lancast/lancast/
│       │   ├── Launcher.java           # Application entry point
│       │   ├── LanCastApplication.java # JavaFX Application
│       │   ├── LanCastController.java  # Main UI controller
│       │   ├── core/
│       │   │   ├── LanCast.java        # Core server logic
│       │   │   ├── QRService.java      # QR code generation
│       │   │   ├── SettingsManager.java# Settings persistence
│       │   │   └── ZipStreamManager.java# ZIP streaming
│       │   └── database/
│       │       ├── HistoryManager.java # Transfer history DB
│       │       └── TransferLog.java    # Log data model
│       └── resources/
│           └── com/lancast/lancast/
│               ├── lancast-view.fxml   # UI layout
│               └── styles.css          # Stylesheet
├── pom.xml                             # Maven configuration
└── README.md
```

---

## 🔧 Building

To build a standalone JAR:

```bash
mvn clean package
```

The JAR file will be created in the `target/` directory.

---

## 🐛 Troubleshooting

### "JavaFX runtime components are missing"
Make sure you have JavaFX installed and use `mvn javafx:run` instead of running the JAR directly.

### Server not accessible from other devices
- Ensure your firewall allows incoming connections on port **8000**
- Make sure all devices are on the same local network
- Try disabling VPN if connected

### Linux: GTK warnings
These warnings can be safely ignored:
```
Gtk-Message: Failed to load module "colorreload-gtk-module"
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## � Authors

| Name | GitHub |
|------|--------|
| **Oussama Belhane** | [@OussamaBelhane](https://github.com/OussamaBelhane) |
| **Manal Wassila** | [@manalwassila](https://github.com/manalwassila) |
| **Hiba Ayatallah** | [@Biba7124](https://github.com/Biba7124) |
| **Labzae Chaimae** | [@chaimaeLabzae](https://github.com/chaimaeLabzae) |

---
