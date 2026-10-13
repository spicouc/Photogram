<div align="center">

# 📸 Photogram

### Your photos. Your Telegram. Your control.

**A personal photo gallery and Telegram-powered backup app for Android.**

[![Android](https://img.shields.io/badge/Android-10%2B-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![GitHub](https://img.shields.io/badge/Source-Photogram-181717?logo=github)](https://github.com/spicouc/Photogram)

</div>

---

## 🌟 What is Photogram?

**Photogram** turns your Android phone and your own Telegram bot into a personal photo gallery and backup workflow. Browse the pictures on your device, select what you want to preserve, and send them to a Telegram chat or channel you control.

No separate photo-hosting server is required. Photogram connects your device to Telegram through the bot configuration you provide.

> **Important:** Telegram storage is not a substitute for an independent, verified backup. Keep a second copy of irreplaceable photos. Availability, file sizes and upload limits are subject to Telegram's policies and API behavior.

## 💡 Why Photogram?

Your memories shouldn't be difficult to organize or back up. Photogram aims to make it simple to:

- 📷 **Browse your photos** in a familiar Android gallery.
- ☁️ **Back up to Telegram** using your own bot and destination chat.
- 🔄 **Schedule backups** and manage upload activity.
- 📥 **Restore photos** from previously uploaded media when supported by the configured backup.
- 🔍 **Avoid unnecessary repeat uploads** using local backup records and duplicate checks.
- 🛠️ **Inspect and improve the code** in an open-source Android project.

## 🚀 How it works

1. Open Telegram and create a bot with [@BotFather](https://t.me/BotFather).
2. Create or choose a private Telegram chat or channel for your photo backups.
3. Give your bot the permissions it needs to post there.
4. Install Photogram, then configure your bot token and destination chat.
5. Choose your photos or enable the available backup options.
6. Check that uploaded media appears in Telegram and keep an independent backup.

**Keep your bot token secret.** Anyone who obtains it may be able to control your bot.

## 📱 Features

| Feature | Description |
| --- | --- |
| Android photo gallery | Browse and select media on your device |
| Telegram integration | Upload through a bot to a configured chat or channel |
| Manual uploads | Back up selected photos |
| Periodic backup | Configure scheduled photo backup |
| Upload queue | Track pending and ongoing uploads |
| Duplicate checks | Reduce repeated uploads with local records and hashes |
| Restore tools | Retrieve supported backed-up media |
| Open-source development | Review code, report issues and contribute improvements |

Some upload reliability improvements are under active development. Features and behavior may vary between releases.

## 🔧 Built for Android

Photogram uses **Kotlin** and **Jetpack Compose**. Developers can build the app with Gradle and the Android SDK:

```bash
git clone https://github.com/spicouc/Photogram.git
cd Photogram
./gradlew assembleDebug
```

The development branch for upload reliability work is [`fix/upload-queue`](https://github.com/spicouc/Photogram/tree/fix/upload-queue). A successful build does not by itself establish that backup and restore are reliable; test them before relying on the app.

## 🧪 Roadmap

- More resilient uploads and persistent retry handling
- Recovery after interrupted transfers
- Better diagnostics for Telegram API errors
- Improved progress reporting
- Tests for queue state and backup recovery

These are development goals, **not promises of completed features**.

## 🤝 Contributing

Found a bug or have an idea? [Open an issue](https://github.com/spicouc/Photogram/issues). Contributions and reproducible bug reports are welcome.

## 🙏 Credits and attribution

Photogram is based on **[Chitralaya CloudGallery by AKS-Labs](https://github.com/AKS-Labs/CloudGallery)**. We acknowledge the original author and contributors for the foundation of this application. Original copyright notices, license files and relevant attribution must remain intact.

---

<div align="center">

**Photogram — Keep your memories within reach.**

[Source code](https://github.com/spicouc/Photogram) · [Report an issue](https://github.com/spicouc/Photogram/issues)

</div>
