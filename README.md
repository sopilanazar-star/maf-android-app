# МАФ — Android WebView App

Сайт: https://maf.lviv.ua
Іконка: офіційний щит МАФ із прозорим фоном.
AGP 8.4.0, Gradle 8.6, SDK 34. Debug-APK збирається у хмарі за кілька хвилин.

## Варіант A: GitHub Actions (хмара) — РЕКОМЕНДОВАНО
1. Створи репозиторій на GitHub і завантаж сюди весь вміст цієї папки.
2. Відкрий вкладку **Actions** → дозволи для workflow.
3. Запусти **Build Android Debug APK** (*Run workflow*).
4. Після завершення завантаж **Artifacts → maf-app-debug → app-debug.apk**.

## Варіант B: Android Studio (ПК)
1. Відкрий проєкт у Android Studio → дочекайся Gradle Sync.
2. Build → Build APKs або Run ▶.
3. APK: `app/build/outputs/apk/debug/app-debug.apk`.

### Налаштування
- Початкова URL (`HOME_URL`): `app/src/main/java/ua/lviv/maf/MainActivity.kt`
- Назва додатка: `app/src/main/res/values/strings.xml`
- Іконка: `res/drawable-anydpi-v26/ic_maf_foreground.png` + адаптивні `mipmap-anydpi-v26/ic_launcher.xml`

### Що вміє апка
- WebView на https://maf.lviv.ua
- splash-екран, pull-to-refresh, індикатор завантаження
- file-upload, завантаження файлів DownloadManager
- deep-links на домен `maf.lviv.ua`, зовнішні лінки — в браузер

> Для релізу (Play) потрібен AAB і release keystore — зробимо окремо.
