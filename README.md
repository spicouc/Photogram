# Photogram

Android photo gallery and Telegram-backed photo backup app.

Photogram is a community-maintained fork based on [CloudGallery (Chitralaya)](https://github.com/AKS-Labs/CloudGallery), originally developed by AKS-Labs. Credit for the original project belongs to its authors and contributors.

## Features

- Browse local photos and albums.
- Back up photos to a Telegram chat or channel using your bot.
- Manage uploads and restore backed-up photos.
- Configure periodic photo backups.

## Development

- Android application written in Kotlin and Jetpack Compose.
- Build: `./gradlew assembleDebug` (requires Android SDK and JDK).
- Current development branch: [fix/upload-queue](https://github.com/spicouc/Photogram/tree/fix/upload-queue).
- Issues: https://github.com/spicouc/Photogram/issues

## Important notes

Telegram is not a substitute for an independently verified backup. Keep a separate copy of important photographs. Upload reliability improvements are under development and should be tested before relying on them.

## Attribution

Based on [AKS-Labs/CloudGallery](https://github.com/AKS-Labs/CloudGallery). See the original repository, commit history and included license files for attribution and license conditions.
