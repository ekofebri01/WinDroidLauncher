# Win11 Launcher

Launcher Android modern dengan tampilan dan pengalaman kerja layaknya desktop **Windows 11**, dibangun menggunakan **Kotlin**, **Jetpack Compose**, dan **Material Design 3**.

## Fitur Utama

- **Desktop & Taskbar Windows 11**
  - Grid shortcut desktop (*This PC*, *File Explorer*, *Edge*, *Personalize*, *Recycle Bin*, dan shortcut aplikasi terpasang) dengan ukuran ikon & penyusunan fleksibel.
  - Taskbar modern dengan dukungan perataan **Center** atau **Left**, ikon aplikasi tersemat (*pinned*), jendela aktif, serta tombol *Show Desktop*.
  - Dukungan tema **Dark / Light** Windows 11, wallpaper bawaan (*Bloom Dark*, *Bloom Light*, *Flow*), serta pemilihan foto kustom melalui Android Photo Picker.

- **Jendela Melayang (*Floating, Movable & Resizable*)**
  - Setiap window (seperti *File Explorer*, *This PC*, *Personalize*, *App Manager*) dibuka sebagai jendela melayang (*floating*) secara default.
  - Dapat digeser melalui *Title Bar*, diubah ukurannya dari tepi maupun sudut (termasuk grip sudut kanan bawah), otomatis berpindah fokus saat disentuh, dan memiliki tombol pojok kanan atas **Minimize**, **Cascade / Maximize**, dan **Close**.

- **File Explorer Gaya Windows 11**
  - Mendukung *multi-tab browsing*, panel navigasi *Quick Access* & *This PC*, *breadcrumb bar*, pencarian, pengurutan, *Details Pane*, pembuatan folder/file teks, *Recycle Bin*, serta akses folder perangkat secara aman via *Storage Access Framework (SAF)* tanpa izin penyimpanan luas.

- **Retractable Start Menu**
  - Panel Start Menu yang dapat dibuka/ditutup dari tombol Start (atau ditutup dengan gestur geser ke bawah, tombol chevron, ketukan di luar panel, atau tombol Back).
  - Dilengkapi **Search Bar** terpadu (mencari aplikasi, utilitas sistem, dan file), area **Pinned** & **All apps**, daftar **Recommended** files, serta menu **Power** (*Lock Screen*, *Refresh apps*, *Default Home App*, dan opsi kembali ke launcher asli).

- **System Tray & Flyout Interaktif**
  - Bagian kanan taskbar menampilkan **jam & tanggal** real-time (format 24/12 jam & detik opsional), indikator **Wi-Fi / jaringan**, status **baterai** dinamis (ikon sesuai persentase & status pengisian menggunakan Material Icons), serta kontrol audio.
  - Flyout **Quick Settings**, **Calendar & Notifications** (termasuk notifikasi update GitHub), **Widgets**, dan **Task View**.

- **Pengganti Home Launcher Aman**
  - Mendeteksi apakah Win11 Launcher sudah menjadi Home default beserta daftar launcher lain yang terpasang.
  - Menyediakan dialog konfirmasi aman (*Safe Mode*) sebelum mengganti Home default sistem (`RoleManager.ROLE_HOME` / pengaturan Home Android), lengkap dengan tombol cepat untuk kembali ke launcher bawaan kapan saja.

- **Deteksi & Update In-App via GitHub Releases**
  - Memeriksa versi rilis/tag terbaru dari repositori GitHub (`owner/repo`) secara otomatis maupun manual.
  - Mengunduh aset `.apk` langsung di dalam aplikasi, memverifikasi keutuhan arsip APK, dan memasangnya melalui **`PackageInstaller.Session`**.
  - Menampilkan animasi bertahap selama proses **Mengunduh → Memverifikasi → Menginstal → Membersihkan → Berhasil Terinstal**, dan otomatis menghapus file APK installer sementara setelah instalasi selesai (termasuk pembersihan pasca-restart saat *self-update*).

## Spesifikasi Teknis

| Komponen | Detail |
| :--- | :--- |
| Bahasa | Kotlin |
| UI Toolkit | Jetpack Compose + Material Design 3 |
| Arsitektur | MVVM (`ViewModel`, `StateFlow`, `collectAsStateWithLifecycle`) |
| Penyimpanan Preferensi | Jetpack DataStore (`Preferences`) |
| Minimum / Target SDK | Android 7.0 (`minSdk 24`) / Android 16 (`compileSdk 36`, `targetSdk 36`) |
| Versi Saat Ini | `1.0.0` (`versionCode = 1`) |
| Application ID | `com.aistudio.win11launcher.kzxmpq` |

## Struktur Modul Utama

- `app/src/main/java/com/example/model/` — Model data launcher, File Explorer, status Home, dan state pembaruan GitHub.
- `app/src/main/java/com/example/data/` — Repositori sistem Android (`SystemRepository`), preferensi DataStore (`LauncherPreferencesRepository`), penyimpanan File Explorer (`FileExplorerRepository`), klien GitHub Releases (`GitHubUpdateRepository`), dan penginstal APK (`ApkInstallerManager`).
- `app/src/main/java/com/example/viewmodel/` — `LauncherViewModel` dan `FileExplorerViewModel`.
- `app/src/main/java/com/example/ui/components/` — Komponen UI Windows 11 (*Taskbar*, *Start Menu*, *Window Frame*, *File Explorer*, *Flyouts*, *Update Dialog*, dan *Built-in Windows*).
- `app/src/main/java/com/example/ui/theme/` — Tema, palet warna, tipografi, dan ikon vektor Windows 11.
- `.github/workflows/android-release.yml` — Workflow GitHub Actions untuk test otomatis, build Release APK, dan publikasi ke GitHub Releases.

## Pengujian & Build

### Menjalankan Unit & Robolectric Test
```bash
gradle :app:testDebugUnitTest
```

### Build APK Lokal
```bash
# Debug APK
gradle :app:assembleDebug

# Release APK (memerlukan keystore & environment variable signing)
KEYSTORE_PATH=/path/to/my-upload-key.jks STORE_PASSWORD=... KEY_PASSWORD=... gradle :app:assembleRelease
```

## Otomatisasi Build & Release di GitHub Actions

Workflow `.github/workflows/android-release.yml` berjalan otomatis saat Anda *push tag* berawalan `v*` (misalnya `v1.0.0`, `v1.0.1`) atau saat dijalankan manual melalui menu **Actions → Build & Release Android APK**.

### Konfigurasi Secrets GitHub
Tambahkan secrets berikut di **GitHub Repository → Settings → Secrets and variables → Actions**:
1. `KEYSTORE_BASE64` — Hasil encode base64 dari file `my-upload-key.jks` (dengan key alias `upload`).
2. `STORE_PASSWORD` — Password keystore Anda.
3. `KEY_PASSWORD` — Password key untuk alias `upload`.

### Cara Merilis Versi Baru
```bash
git tag v1.0.1
git push origin v1.0.1
```
GitHub Actions akan menjalankan unit test, mem-build Release APK yang sudah ditandatangani, dan melampirkannya otomatis ke **GitHub Releases** agar terdeteksi oleh fitur **Update In-App** di Win11 Launcher.
