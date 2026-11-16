# NOEXDATE — Android App README & Master Prompt

> **Nama aplikasi:** NOEXDATE  
> **Konsep:** Notes + Expenses + No Expired Date  
> **Platform:** Android native  
> **IDE:** Android Studio (berbasis platform IntelliJ IDEA)  
> **Bahasa utama:** Kotlin

Dokumen ini adalah spesifikasi produk dan master prompt yang bisa diberikan ke AI coding assistant untuk membangun NOEXDATE di Android Studio. Fokusnya aplikasi Android native, bukan Flutter atau React Native.

---

## 1. Master Prompt untuk AI Coding Assistant

Salin bagian di bawah ini ke AI coding assistant yang membantu mengerjakan project Android Studio.

### PROJECT PROMPT

Bertindaklah sebagai senior Android engineer. Bangun aplikasi Android native bernama **NOEXDATE** menggunakan **Kotlin dan Jetpack Compose**. Jangan gunakan Flutter, React Native, atau framework lintas platform. Aplikasi harus benar-benar berfungsi, bukan hanya UI mockup.

#### A. Teknologi dan arsitektur

Gunakan stack Android modern berikut, dengan versi dependency stabil dan saling kompatibel pada saat implementasi:

- Kotlin
- Android Studio dan Gradle Kotlin DSL
- Jetpack Compose + Material 3 untuk UI
- Navigation Compose untuk navigasi
- MVVM dengan pemisahan UI, ViewModel, domain, dan data layer
- Kotlin Coroutines dan Flow untuk pekerjaan asynchronous serta stream data
- Repository pattern
- Hilt untuk dependency injection bila sesuai
- Room untuk penyimpanan lokal dan dukungan offline
- Firebase Authentication untuk login opsional/akun pengguna
- Cloud Firestore untuk sinkronisasi data pengguna
- Firebase Storage untuk foto dan gambar lampiran
- Firebase Cloud Messaging (FCM) untuk push notification dari server
- Google ML Kit Text Recognition untuk membaca teks label tanggal kedaluwarsa dari gambar
- Android SpeechRecognizer untuk voice-to-text melalui layanan pengenalan suara yang tersedia pada perangkat; jika memerlukan Google Cloud Speech-to-Text, jelaskan konfigurasi, privasi, internet, dan biaya sebelum memakainya
- WorkManager untuk pekerjaan terjadwal yang tidak memerlukan waktu eksekusi presisi
- Android notification APIs untuk notifikasi lokal terjadwal

Periksa struktur project yang sudah ada sebelum mengubahnya. Jangan menebak konfigurasi Firebase atau menganggap kredensial sudah terpasang. Jangan menyimpan API key atau service account credentials dalam source control.

Gunakan pendekatan **local-first**: data penting tetap bisa dilihat dari database lokal Room, lalu disinkronkan ke Firebase bila sinkronisasi dan akun diaktifkan. Tangani konflik sinkronisasi secara jelas.

#### B. Fitur utama dan navigasi

Aplikasi memiliki lima tujuan utama melalui bottom navigation:

1. Home
2. Calendar
3. Notes
4. Expenses
5. Pantry

Pengaturan dan profil dapat diakses dari app bar atau halaman Settings. Sediakan aksi cepat untuk membuat note, event, expense, pantry item, dan scan tanggal kedaluwarsa.

#### C. Notes

Pengguna dapat:

- Membuat, membaca, mengubah, menghapus, mencari, dan mengarsipkan catatan.
- Menyimpan judul dan isi catatan teks/multiline.
- Melampirkan satu atau beberapa foto dari kamera atau galeri.
- Mengubah ucapan menjadi teks melalui tombol mikrofon.
- Mengedit hasil transkripsi sebelum disimpan.
- Menambahkan tag, kategori, dan prioritas.
- Menentukan deadline opsional.
- Mengatur pengingat untuk deadline.
- Menandai catatan selesai.
- Melihat catatan yang akan jatuh tempo dan yang sudah terlambat.
- Menghubungkan catatan ke event kalender.

Catatan tanpa deadline atau foto tetap harus bisa dibuat. Jangan menghapus isi catatan yang sudah ada ketika pengguna menambahkan hasil voice-to-text.

#### D. Kalender terpadu

Buat kalender native dengan tampilan bulan, minggu, hari, dan agenda. Kalender terinspirasi dari kemudahan Google Calendar, namun seluruh data dan fitur tetap terintegrasi dalam NOEXDATE.

Pengguna dapat membuat, mengubah, dan menghapus acara dengan judul, deskripsi, waktu mulai/selesai, lokasi, opsi acara sepanjang hari, catatan, foto, pengingat, dan pengulangan jika didukung.

Kalender juga menampilkan:

- Event kalender biasa.
- Deadline dari Notes.
- Tanggal kedaluwarsa dari Pantry.

Bedakan jenis item dengan label/warna/icon yang konsisten. Pengguna bisa memfilter event, notes, dan pantry. Saat deadline atau tanggal kedaluwarsa berubah, tampilan kalender dan jadwal reminder harus ikut diperbarui. Gunakan ID referensi ke data asli; hindari membuat salinan data yang menyebabkan event duplikat.

Gunakan zona waktu dengan benar dan tangani event sepanjang hari secara konsisten.

#### E. Expense Tracker — input manual saja

Pengeluaran dicatat sendiri oleh pengguna. Jangan menghubungkan rekening bank atau mengambil transaksi otomatis.

Setiap transaksi memiliki:

- Nominal
- Mata uang
- Tanggal pengeluaran
- Kategori
- Deskripsi/nama merchant opsional
- Foto struk opsional
- Catatan opsional

Kategori awal:

- Food & Drinks / Makanan & Minuman
- Transportation / Transportasi
- Shopping / Belanja
- Bills & Utilities / Tagihan & Utilitas
- Health / Kesehatan
- Entertainment / Hiburan
- Household / Rumah Tangga
- Education / Pendidikan
- Travel / Perjalanan
- Other / Lainnya

Pengguna dapat mengelola kategori khusus. Sediakan riwayat transaksi, pencarian, filter, total pengeluaran harian/mingguan/bulanan, ringkasan per kategori, dan grafik.

Gunakan representasi angka yang presisi untuk nilai uang; jangan mengandalkan floating-point biasa untuk perhitungan total. Jangan menambahkan fitur pemasukan, sinkronisasi bank, atau budgeting kecuali diminta.

Tidak boleh ada notifikasi setiap kali transaksi dicatat. Ringkasan expense mingguan/bulanan bersifat opsional dan nonaktif sampai pengguna mengaktifkannya.

#### F. Pantry dan scan tanggal kedaluwarsa

Pantry menyimpan inventaris makanan dan tanggal kedaluwarsanya.

Pengguna dapat:

- Menambahkan makanan secara manual.
- Mengambil foto label atau memilih foto dari galeri.
- Membaca teks label memakai Google ML Kit Text Recognition.
- Meninjau dan mengedit nama makanan yang terdeteksi.
- Meninjau dan mengedit tanggal kedaluwarsa yang terdeteksi.
- Mengatur jumlah, unit, dan lokasi penyimpanan.
- Memilih pengingat kedaluwarsa.
- Menandai makanan telah dikonsumsi, dibuang, atau tidak lagi dilacak.
- Memfilter makanan yang segera kedaluwarsa atau sudah kedaluwarsa.
- Membuka detail item beserta foto dan tanggalnya.

Alur scan:

1. Pengguna mengambil/memilih foto.
2. ML Kit mengenali teks pada gambar.
3. Parser mencari kandidat nama produk dan tanggal.
4. Aplikasi menampilkan teks yang dikenali serta kandidat tanggal.
5. Pengguna mengonfirmasi atau memperbaiki nama dan tanggal.
6. Item baru disimpan setelah konfirmasi.
7. Item masuk ke kalender terpadu.
8. Reminder dijadwalkan sesuai pengaturan notifikasi pengguna.

Kenali istilah umum seperti EXP, EXPIRY, EXPIRES, BEST BEFORE, BB, USE BY, KEDALUWARSA, dan BAIK DIGUNAKAN SEBELUM. Tangani format tanggal seperti DD/MM/YYYY, MM/DD/YYYY, YYYY-MM-DD, serta nama bulan.

Tanggal bisa ambigu. Jangan menganggap tanggal produksi, tanggal kemasan, batch code, atau barcode sebagai tanggal kedaluwarsa tanpa validasi. Jika ragu, minta konfirmasi. OCR tidak menjamin bahwa kandidat tanggal benar. Jika scan gagal, sediakan input manual.

#### G. Push notifications dan pengingat yang bisa dipilih

Ini merupakan fitur penting NOEXDATE. Jangan mengirim semua notifikasi secara otomatis. Pengguna harus dapat menentukan notifikasi apa yang diizinkan masuk ke HP.

Sediakan halaman **Settings → Notifications** dengan:

- Master switch untuk mematikan/mengaktifkan semua notifikasi NOEXDATE.
- Toggle Notes Deadlines.
- Toggle Calendar Events.
- Toggle Pantry Expiration.
- Toggle Expense Summaries.
- Toggle General App Updates (opsional).
- Pengaturan waktu pengingat untuk setiap kategori.
- Pengaturan quiet hours.

Setiap kategori harus dapat diaktifkan atau dinonaktifkan secara independen. Contoh: pengguna mengaktifkan Pantry dan Calendar, tetapi menonaktifkan Notes dan Expense Summary.

Default yang disarankan: notifikasi opsional seperti Expense Summary dan General App Updates nonaktif sampai pengguna mengaktifkannya. Jelaskan pengaturan dengan bahasa sederhana.

**Jadwal pengingat yang dapat dipilih:**

- Pada waktunya.
- 15 menit sebelumnya.
- 30 menit sebelumnya.
- 1 jam sebelumnya.
- 1 hari sebelumnya.
- 3 hari sebelumnya.
- 7 hari sebelumnya.
- Waktu kustom.

Tampilkan pilihan yang relevan untuk setiap kategori. Pantry umumnya menyediakan opsi 1, 3, atau 7 hari sebelum kedaluwarsa, serta waktu kustom. Pengguna dapat mengaktifkan beberapa reminder untuk satu item hanya jika mereka memilihnya secara eksplisit.

**Pengaturan per item:**

Pada setiap note, event, atau item pantry, izinkan pengaturan reminder khusus. Pengaturan per item tidak boleh melewati master switch atau toggle kategori yang sedang mati. Preferensi kategori menjadi default untuk item baru; pengguna dapat memilih jadwal khusus per item jika kategori tersebut aktif.

**Quiet hours:**

- Toggle quiet hours.
- Jam mulai dan selesai yang dapat diatur.
- Mendukung rentang yang melewati tengah malam.
- Menggunakan zona waktu pengguna.
- Pilihan untuk menunda atau melewatkan reminder non-urgent ketika jam tenang berakhir, sesuai relevansi.
- Jangan mengirim banyak reminder tertunda sekaligus tanpa pertimbangan.

**Logika scheduling:**

Buat satu `ReminderScheduler`/`NotificationRepository` terpusat; jangan menulis logika notifikasi yang terpisah-pisah pada setiap screen.

Saat data note, event, pantry, atau preferensi berubah:

1. Baca pengaturan notifikasi pengguna.
2. Periksa master switch dan toggle kategori.
3. Periksa preferensi reminder item.
4. Hitung waktu reminder yang masih relevan.
5. Batalkan jadwal lama yang sudah tidak berlaku.
6. Jadwalkan ulang reminder yang valid.
7. Simpan identifier agar reminder dapat dibatalkan atau diperbarui.
8. Cegah duplikasi saat proses sinkronisasi diulang.

Saat note selesai/dihapus, event dihapus, atau pantry item dikonsumsi/dihapus, batalkan reminder terkait. Saat tanggal kedaluwarsa/deadline berubah, batalkan jadwal lama dan buat jadwal baru. Item yang tetap expired tidak boleh memicu notifikasi berulang tanpa batas.

**Strategi Android:**

- Gunakan notifikasi lokal terjadwal untuk pengingat yang bisa dijalankan oleh perangkat.
- Gunakan WorkManager untuk pekerjaan tertunda yang tidak membutuhkan eksekusi pada menit/detik yang presisi.
- Gunakan AlarmManager hanya jika kebutuhan waktunya memang memerlukan alarm; perhatikan batasan exact alarm Android dan izin khusus yang berlaku.
- Gunakan FCM untuk push dari server, misalnya reminder yang disinkronkan lintas perangkat atau dijadwalkan dari backend. FCM sendiri bukan scheduler yang menjamin pengiriman tepat pada waktunya; gunakan backend/scheduler yang sesuai jika diperlukan.
- Siapkan notification channels Android yang relevan untuk Notes, Calendar, Pantry, dan ringkasan lain jika pemisahan channel memang membantu pengguna.
- Tangani izin `POST_NOTIFICATIONS` pada Android 13+.
- Jelaskan bahwa notifikasi dapat dipengaruhi izin OS, pengaturan channel, battery restrictions, koneksi, dan kebijakan perangkat.
- Jangan menyatakan pengiriman dijamin tepat waktu.

Jika memakai FCM dari backend, jangan menaruh service account key di aplikasi. Semua pengiriman sensitif harus dilakukan oleh backend tepercaya dengan verifikasi kepemilikan data pengguna.

**Tes notifikasi wajib:**

- Hanya Pantry aktif dan kategori lain mati.
- Hanya Calendar aktif.
- Master switch mati.
- Izin notifikasi Android ditolak.
- Reminder item dinonaktifkan.
- Deadline/tanggal kedaluwarsa berubah.
- Item selesai/dihapus/dikonsumsi sebelum reminder.
- Reminder yang sama dijadwalkan berulang.
- Quiet hours melewati tengah malam.
- Zona waktu atau jadwal perangkat berubah.
- Item masih expired beberapa hari.
- Potensi duplikasi notifikasi lokal dan remote.

#### H. Tema dan bahasa

Sediakan tema Light dan Dark, dengan opsi mengikuti tema sistem. Simpan preferensi pengguna dan terapkan secara konsisten pada seluruh layar.

Sediakan bahasa:

- Bahasa Indonesia (`id`)
- English (`en`)

Gunakan Android resource localization seperti `strings.xml` dan `values-id/strings.xml`. Jangan menaruh teks antarmuka langsung sebagai hardcoded string dalam composable. Terjemahkan tombol, menu, error, empty state, notifikasi, kategori, dan pengaturan.

Format tanggal, waktu, nominal, dan mata uang harus mengikuti locale yang dipilih serta preferensi pengguna.

#### I. Dashboard Home

Tampilkan ringkasan ringkas:

- Agenda hari ini.
- Deadline notes mendatang.
- Makanan yang segera kedaluwarsa.
- Catatan terbaru.
- Pengeluaran hari ini/bulan ini.
- Tombol aksi cepat.

Gunakan data nyata dari repository, bukan angka contoh yang di-hardcode. Tampilkan empty state bila data belum ada.

#### J. Data model awal

Buat model dan repository untuk objek berikut. Sesuaikan field bila diperlukan dan dokumentasikan keputusan yang diambil.

**UserPreferences**
- userId
- preferredLanguage
- themePreference
- timezone
- currencyPreference
- notificationPreferences
- updatedAt

**Note**
- id
- userId
- title
- content
- photoUris/photoUrls
- tags
- category
- priority
- deadline
- isCompleted
- isArchived
- reminderConfiguration
- createdAt
- updatedAt

**CalendarEvent**
- id
- userId
- title
- description
- location
- startAt
- endAt
- isAllDay
- recurrenceRule
- photoUris/photoUrls
- linkedNoteIds
- reminderConfiguration
- createdAt
- updatedAt

**Expense**
- id
- userId
- amountMinorUnits atau representasi presisi yang sesuai
- currency
- categoryId
- description
- expenseDate
- receiptPhotoUri/photoUrl
- notes
- createdAt
- updatedAt

**ExpenseCategory**
- id
- userId
- nameKey/customName
- icon
- isDefault

**PantryItem**
- id
- userId
- name
- photoUri/photoUrl
- quantity
- unit
- storageLocation
- expirationDate
- recognizedText
- scanConfidence (opsional)
- status
- reminderConfiguration
- createdAt
- updatedAt

**NotificationPreferences**
- userId
- masterEnabled
- noteDeadlinesEnabled
- calendarEventsEnabled
- pantryExpirationEnabled
- expenseSummariesEnabled
- generalUpdatesEnabled
- categoryReminderDefaults
- quietHoursEnabled
- quietHoursStart
- quietHoursEnd
- deferredNotificationPolicy
- timezone
- updatedAt

**ScheduledReminder**
- id
- userId
- sourceType
- sourceId
- category
- scheduledAt
- deliveryType
- platformNotificationId
- status
- createdAt
- updatedAt

Gunakan Room entities dan DAO untuk data lokal, serta repository untuk mengabstraksikan sinkronisasi Firebase. Buat indeks database yang sesuai. Data setiap pengguna harus diisolasi. Jangan menyimpan duplikat seluruh Note atau PantryItem di event kalender; gunakan referensi ID.

#### K. Keamanan dan privasi

- Firebase Authentication bila login/sinkronisasi akun diaktifkan.
- Batasi akses Firestore dan Firebase Storage agar pengguna hanya dapat mengakses datanya sendiri.
- Validasi data di aplikasi dan backend/security rules.
- Minta izin kamera, foto, mikrofon, dan notifikasi pada saat relevan, dengan penjelasan yang jelas.
- Jangan meminta semua izin saat pertama kali aplikasi dibuka.
- Sediakan alur logout dan penghapusan akun/data jika akun didukung.
- Hindari menyimpan audio mentah jika tidak diperlukan.
- Jelaskan pemrosesan data pihak ketiga jika menggunakan layanan cloud.

#### L. Susunan project yang disarankan

Gunakan feature-first structure, misalnya:

```text
app/src/main/java/<package>/
  core/
    database/
    network/
    notifications/
    permissions/
    design system/
    util/
  data/
    local/
    remote/
    repository/
  domain/
    model/
    usecase/
  feature/
    home/
    notes/
    calendar/
    expenses/
    pantry/
    settings/
    notificationsettings/
  navigation/
  MainActivity.kt
```

Sesuaikan package name dengan konfigurasi project asli. Jangan membuat struktur yang terlalu kompleks tanpa kebutuhan. UI Compose sebaiknya memanggil ViewModel; jangan menaruh seluruh business logic di composable.

#### M. Tahapan implementasi

**Phase 1 — Foundation**
- Periksa project Android Studio.
- Atur Kotlin, Gradle, Compose, Material 3, navigation, theme, dan localization.
- Susun model dan repository dasar.
- Implementasikan penyimpanan lokal Room.
- Integrasikan Firebase jika konfigurasi tersedia.

**Phase 2 — Notes**
- CRUD Notes.
- Lampiran foto.
- Deadline.
- Voice-to-text.

**Phase 3 — Calendar**
- Tampilan bulan/minggu/hari/agenda.
- CRUD event.
- Integrasi deadline Notes.

**Phase 4 — Expenses**
- Input manual.
- Kategori.
- Riwayat dan laporan.

**Phase 5 — Pantry**
- Input manual.
- Integrasi ML Kit.
- Konfirmasi hasil scan.
- Status kedaluwarsa dan kalender.

**Phase 6 — Notifications**
- Settings notifikasi per kategori.
- Reminder per item.
- Local scheduling.
- Integrasi FCM/backend bila diperlukan.
- Quiet hours dan pencegahan duplikasi.

**Phase 7 — Quality and release**
- Unit tests untuk logika bisnis.
- UI/instrumentation tests untuk alur penting.
- Security rules.
- Uji perangkat dan versi Android yang didukung.
- Dokumentasi build, konfigurasi, dan release.

#### N. Definition of Done

MVP dianggap selesai ketika:

- Pengguna bisa membuat dan mengelola notes dengan foto serta deadline.
- Voice-to-text memasukkan transkripsi yang dapat diedit.
- Event kalender bisa dibuat dengan deskripsi dan lampiran.
- Kalender menampilkan event, deadline note, dan tanggal kedaluwarsa pantry tanpa duplikasi.
- Expense dibuat manual dan ringkasannya dihitung dengan benar.
- Pantry dapat memakai hasil scan sebagai saran yang dikonfirmasi pengguna.
- Pengguna dapat memilih kategori notifikasi yang boleh masuk ke HP.
- Master switch mematikan seluruh notifikasi NOEXDATE.
- Mengubah atau menghapus item memperbarui/membatalkan reminder terkait.
- Pengaturan quiet hours dan pencegahan duplikasi berfungsi.
- Light/Dark dan English/Indonesian berfungsi di seluruh aplikasi.
- Data tersimpan secara lokal dan sinkronisasi berjalan bila dikonfigurasi.
- Security rules dan permission flow sudah diperiksa.
- Ada tes untuk alur bisnis dan notifikasi paling penting.

#### O. Cara kerja AI developer

Mulai dengan memeriksa project Android Studio yang ada. Sebelum mengubah file, jelaskan secara singkat:

1. Struktur project saat ini.
2. Arsitektur yang akan digunakan.
3. Dependency yang diperlukan.
4. Langkah setup Firebase dan Google ML Kit.
5. Rencana file yang akan ditambah/diubah.

Lalu implementasikan fitur per fase. Setelah setiap perubahan, jalankan build/check yang tersedia dan perbaiki error yang ditemukan. Jangan mengklaim fitur selesai jika baru berupa UI. Catat konfigurasi yang harus dilakukan sendiri oleh developer dan sediakan fallback ketika service/permission tidak tersedia.

Prioritaskan MVP yang stabil dan mudah diuji daripada menambah banyak fitur sekaligus.

---

## 2. Catatan setup penting

1. **Android Studio:** buat atau buka project native Android dengan Kotlin dan Jetpack Compose.
2. **Firebase:** buat project Firebase, daftarkan aplikasi Android dengan package name yang benar, lalu tempatkan `google-services.json` sesuai dokumentasi resmi. Jangan upload file rahasia ke repository publik tanpa memeriksa isinya.
3. **ML Kit:** tambahkan dependency Text Recognition yang cocok dengan konfigurasi Gradle. Uji dengan foto label yang jelas dan foto yang sulit.
4. **Push notification:** untuk FCM, konfigurasi aplikasi dan backend bila notifikasi dikirim dari server. Untuk pengingat lokal, uji notifikasi pada perangkat nyata. Android 13+ memerlukan izin runtime notifikasi.
5. **Build:** jalankan Gradle sync, kemudian build dan tes di emulator serta perangkat fisik. Fitur kamera, notifikasi, speech recognition, dan background execution perlu diuji pada perangkat nyata.

## 3. Prinsip penting produk

- Notifikasi harus membantu, bukan membanjiri pengguna.
- Toggle per kategori harus benar-benar memengaruhi jadwal notifikasi yang sudah ada.
- OCR hanya memberikan kandidat; pengguna mengonfirmasi tanggal sebelum data disimpan.
- Expense tetap manual.
- Kalender adalah tampilan terpadu dari data sumber, bukan salinan data yang tidak sinkron.
- Fitur inti tetap dapat diakses jika izin notifikasi ditolak.
