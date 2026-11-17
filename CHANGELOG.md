# Changelog

Semua perubahan penting pada proyek **Win11 Launcher** didokumentasikan di file ini.

Format changelog mengacu pada [Keep a Changelog](https://keepachangelog.com/id-ID/1.1.0/) dan penomoran versi mengikuti [Semantic Versioning](https://semver.org/).

## [1.0.3] - 2026-11-17

### Added
- **Widget Aplikasi Desktop yang Dapat Diubah Ukurannya (`DesktopWidgetManager`, `Win11DesktopWidgets`)**
  - Dukungan penuh `AppWidgetHost` untuk menampilkan widget asli dari aplikasi Android yang terpasang di layar desktop (*long-press desktop → Tambah Widget Aplikasi* atau melalui *Settings → Personalize → Tambah Widget Aplikasi ke Desktop...*).
  - Mode atur & ubah ukuran interaktif (tekan tahan widget atau ketuk tombol ikon atur di pojok kanan atas widget): seret bebas untuk memindahkan posisi di desktop, ubah ukuran dari tepi kanan, tepi bawah, sudut kiri-bawah, dan sudut kanan-bawah (*live option updates* ke `AppWidgetHostView`), konfigurasi ulang widget (*reconfigure*), serta hapus widget — seluruh posisi dan ukuran tersimpan otomatis di DataStore.
- **Redesain System Tray & Flyout Gaya Windows 11 (`Win11Icons`, `Win11Taskbar`, `Win11Flyouts`)**
  - Ikon Tray Fluent vektor kustom (*baterai horizontal + ikon pengisian*, *speaker volume bertingkat gelombang suara / mute*, *status Wi-Fi / seluler / globe terputus*, *lonceng notifikasi & dot aksen*).
  - Peningkatan **Quick Settings Flyout** (*split tiles* gaya Win11, slider volume, tombol *Edit/Atur Tray*), **Calendar & Notifications Flyout** (*Notification Center + kalender yang dapat dilipat*), serta **Hidden Tray Overflow Flyout**.
- **Pengaturan NavBar & Jendela Floating Taskbar/Panel (`TASKBAR_PANEL_SETTINGS`)**
  - Opsi menyembunyikan Navigation Bar sistem (`hideSystemNavBar`) bersama Status Bar, serta konversi panel pengaturan taskbar menjadi jendela *floating* yang dapat digeser dan diubah ukurannya.
- **Build Release APK v1.0.3**
  - Rilis APK v1.0.3 (`versionCode = 4`) bertanda tangan kunci rilis yang konsisten untuk mendukung pembaruan timpa langsung.

### Chores
- Menambahkan aturan `*.bin` ke `.gitignore` agar file biner rilis lokal tidak ikut ter-push ke GitHub.

## [1.0.2] - 2026-11-17

### Added
- **Pemilih Panel Taskbar Mandiri (`Win11TaskbarPanelSelectorDialog`)**
  - Penambahan dialog cepat **Pilih Panel & Item Taskbar** agar pengguna dapat mengaktifkan/menyembunyikan tiap panel secara terpisah (*Gaya Docked/Floating*, *Posisi Center/Left*, *Panel Search*, *Task View*, *File Explorer*, *Pinned Apps*, *Show Desktop*, *Hidden Tray*, *Wi-Fi/Jaringan*, *Volume*, *Baterai & %*, *Jam*, *Tanggal*, *Titik Notifikasi*, *Format 24-Jam*, *Detik*, serta tombol **Reset**).
  - Dapat dibuka langsung dengan **tekan tahan (*long-press*) pada taskbar** maupun melalui tombol **Pilih & Atur Panel Taskbar Sendiri...** di **Settings → Personalize**.
- **Build Release APK v1.0.2**
  - Rilis APK v1.0.2 (`versionCode = 3`) dengan tanda tangan yang sama untuk mendukung *instal timpa* tanpa uninstall.

### Fixed
- **Download Update & Timeout Resilience**
  - Memperpanjang batas waktu koneksi (30 detik) dan pembacaan stream (120 detik) serta menambahkan mekanisme *auto-retry* (hingga 3 kali percobaan) saat mengunduh APK update dari GitHub untuk mencegah error *timeout* / *download failed*.
- **Gestur Long-Press Taskbar & Stabilitas UI**
  - Menggunakan `pointerInput` (`detectTapGestures`) untuk gestur *long-press* pada taskbar sehingga tidak membungkus komponen anak dalam semantics klik dan menjaga visibilitas *System Tray* tetap stabil.
  - Memperbaiki kontras teks/ikon tombol selector di *Personalize* dan kelancaran scroll dialog pemilih panel.

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
