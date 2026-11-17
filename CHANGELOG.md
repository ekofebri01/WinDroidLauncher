# Changelog

Semua perubahan penting pada proyek **Win11 Launcher** didokumentasikan di file ini.

Format changelog mengacu pada [Keep a Changelog](https://keepachangelog.com/id-ID/1.1.0/) dan penomoran versi mengikuti [Semantic Versioning](https://semver.org/).

## [1.0.0] - 2026-11-16

### Added
- **Desktop & Taskbar Windows 11**
  - Desktop grid dengan shortcut sistem (*This PC*, *File Explorer*, *Edge*, *Personalize*, *Recycle Bin*) dan shortcut aplikasi terpasang.
  - Taskbar bawah modern (alignment *Center* / *Left*, pinned apps, indikator window aktif, dan tombol *Show Desktop*).
  - Pilihan tema Dark/Light, wallpaper preset (*Bloom Dark*, *Bloom Light*, *Flow*), dukungan foto galeri via Photo Picker, serta *Lock Screen*.
- **Jendela Melayang (*Floating, Movable & Resizable Windows*)**
  - Dukungan drag posisi dari *Title Bar*, resize dari seluruh tepi dan sudut (termasuk grip sudut kanan bawah), fokus otomatis saat disentuh, serta tombol kontrol **Minimize**, **Cascade/Maximize**, dan **Close** di pojok kanan atas.
- **File Explorer Gaya Windows 11**
  - Navigasi *multi-tab*, sidebar *Quick Access* & *This PC*, *breadcrumb bar*, pencarian, pengurutan, *Details Pane*, *Recycle Bin*, pembuatan file/folder, pratinjau teks/gambar, dan mounting folder Android via *Storage Access Framework (SAF)*.
- **Retractable Start Menu**
  - Panel Start Menu interaktif dengan *Search Bar* terpadu, daftar *Pinned* & *All apps*, area *Recommended* files, gestur tutup (*swipe down*, chevron, scrim tap, *Back*), serta menu *Power*.
- **System Tray & Flyouts**
  - System Tray di sisi kanan taskbar menampilkan jam & tanggal, status Wi-Fi/jaringan, status & persentase baterai dinamis dengan Material Icons, Quick Settings, Calendar & Notifications, Widgets, dan Task View.
- **Pengganti Home Launcher Aman**
  - Deteksi status Home default dan launcher bawaan yang terpasang, dialog konfirmasi aman (`SafeHomeChooserDialog`), pengaturan guard di *Personalize*, penanganan `onNewIntent` tombol Home, dan opsi 1-ketuk kembali ke launcher bawaan.
- **Deteksi Update GitHub & Update In-App APK**
  - Deteksi rilis/tag terbaru dari GitHub REST API dengan perbandingan versi semantik dan notifikasi di Calendar Flyout & Personalize.
  - Unduhan `.apk` di dalam aplikasi, pengecekan izin *Install Unknown Apps*, verifikasi header APK, instalasi via `PackageInstaller.Session`, animasi tiap tahap (*Downloading*, *Verifying*, *Installing*, *Cleaning Up*, *Installed Success*), dan penghapusan otomatis file `.apk` sementara setelah instalasi selesai.
- **CI/CD GitHub Actions**
  - Menambahkan `.github/workflows/android-release.yml` untuk verifikasi unit test, build Release APK bertanda tangan, unggah artifact, dan publikasi otomatis ke GitHub Releases pada push tag `v*`.
- **Dokumentasi**
  - Menambahkan `README.md` dan `CHANGELOG.md`.
