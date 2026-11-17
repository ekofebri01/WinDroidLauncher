# Changelog

Semua perubahan penting pada proyek **Win11 Launcher** didokumentasikan di file ini.

Format changelog mengacu pada [Keep a Changelog](https://keepachangelog.com/id-ID/1.1.0/) dan penomoran versi mengikuti [Semantic Versioning](https://semver.org/).

## [1.0.1] - 2026-11-16

### Added
- **Full Taskbar & System Tray Personalization (Settings → Personalize)**
  - Penambahan kartu pengaturan taskbar komprehensif (`Win11TaskbarSettingsSection`) yang terintegrasi dengan Jetpack DataStore.
  - Pengaturan **Tinggi Taskbar** via slider (44 dp – 72 dp) dan preset cepat (*Compact*, *Standard*, *Tall*).
  - Pilihan **Gaya Taskbar**: *Docked* (menempel) atau *Floating* (melayang dengan sudut membulat).
  - Pengaturan **Opasitas Taskbar** (45% – 100%) dan **Ukuran Ikon** (*Small*, *Medium*, *Large*).
  - Toggle visibilitas komponen utama taskbar: Tombol *Search Mode* (*Auto*, *Icon*, *Search Box*, *Hidden*), *Task View*, *File Explorer*, *Pinned Apps*, dan tombol *Show Desktop*.
  - Toggle visibilitas individual komponen *System Tray*: Panah *Hidden Icons* (`^`), Indikator Wi-Fi/Jaringan, Ikon Volume, Ikon Baterai, Persentase Baterai (%), Jam & Tanggal, format 24 Jam, Detik, Titik Notifikasi, serta tombol Reset ke Default.
- **Pembaruan UI Taskbar**
  - Menghapus tombol Widget di sisi kiri taskbar demi kebersihan layout desktop.
- **Build Release APK v1.0.1**
  - Berhasil membangun dan memvalidasi rilis APK v1.0.1 (`versionCode = 2`) dengan tanda tangan kompatibel untuk *instal timpa*.

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
  - System Tray di sisi kanan taskbar menampilkan jam & tanggal, status Wi-Fi/jaringan, status & persentase baterai dinamis dengan Material Icons, Quick Settings, Calendar & Notifications, dan Task View.
- **Pengganti Home Launcher Aman**
  - Deteksi status Home default dan launcher bawaan yang terpasang, dialog konfirmasi aman (`SafeHomeChooserDialog`), pengaturan guard di *Personalize*, penanganan `onNewIntent` tombol Home, dan opsi 1-ketuk kembali ke launcher bawaan.
- **Deteksi Update GitHub & Update In-App APK**
  - Deteksi rilis/tag terbaru dari GitHub REST API dengan perbandingan versi semantik dan notifikasi di Calendar Flyout & Personalize.
  - Unduhan `.apk` di dalam aplikasi, pengecekan izin *Install Unknown Apps*, verifikasi header APK, instalasi via `PackageInstaller.Session`, animasi tiap tahap (*Downloading*, *Verifying*, *Installing*, *Cleaning Up*, *Installed Success*), dan penghapusan otomatis file `.apk` sementara setelah instalasi selesai.
- **CI/CD GitHub Actions**
  - Menambahkan `.github/workflows/android-release.yml` untuk verifikasi unit test, build Release APK bertanda tangan, unggah artifact, dan publikasi otomatis ke GitHub Releases pada push tag `v*`.
- **Dokumentasi**
  - Menambahkan `README.md` dan `CHANGELOG.md`.
