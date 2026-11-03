package com.example.tugaskotlinutsumsida

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugaskotlinutsumsida.ui.theme.Pertemuan6Theme

/**
- Activity utama aplikasi yang menjadi titik masuk (entry point).
- Mengatur tema dasar dan menampilkan layar pendaftaran [RegistrationScreen].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1F2937),    // Dark Gray / Off-black
                    onPrimary = Color.White,        // Teks tombol putih
                    secondary = Color(0xFF6B7280),  // Medium Cool Gray
                    background = Color(0xFFF8FAFC), // Latar belakang abu-abu es (ultra light)
                    surface = Color(0xFFE5E7EB)     // Kartu formulir abu-abu lembut
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PortalUmsidaApp()
                }
            }
        }
    }
}

enum class Screen { REGISTRATION, DASHBOARD, SETTINGS }

// Warna Kustom Tema Modern Abu-Abu & Dark Slate
val ModernDarkSlate = Color(0xFF1F2937)
val MaroonUmsida = ModernDarkSlate
val BackgroundGray = Color(0xFFF8FAFC)

@Composable
fun PortalUmsidaApp() {
    // State Penentu Layar & Penyimpan Nama
    var currentScreen by rememberSaveable { mutableStateOf(Screen.REGISTRATION) }
    var loggedInName by rememberSaveable { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (currentScreen) {
            Screen.REGISTRATION -> RegistrationScreen(
                onLoginSuccess = { name ->
                    loggedInName = name // Simpan nama yang dikirim dari form
                    currentScreen = Screen.DASHBOARD // Pemicu ganti halaman
                }
            )
            Screen.DASHBOARD -> DashboardScreen(
                studentName = loggedInName, // Pemicu dimana oper nama pada dashboard
                onNavigateSettings = { currentScreen = Screen.SETTINGS },
                onLogout = { currentScreen = Screen.REGISTRATION }
            )
            Screen.SETTINGS -> SettingsScreen(
                onBack = { currentScreen = Screen.DASHBOARD }
            )
        }
    }
}

/**
- Screen-level state owner untuk contoh state hoisting dasar.
- Memperagakan penerapan State, Event, Anatomi Field, Form Validation, dan UI Modern di Jetpack Compose.
 */
@Composable
fun RegistrationScreen(onLoginSuccess: (String) -> Unit = {}) {
    // =========================================================================
    // 1. STATE MANAGEMENT
    // State adalah data yang menentukan apa yang ditampilkan oleh UI.
    // Ketika state berubah, Jetpack Compose akan melakukan Rekomposisi (Recomposition).
    // =========================================================================

    // State input formulir
    // - rememberSaveable: Menjaga state tetap bertahan saat terjadi perubahan konfigurasi (misal: rotasi layar).
    var nama by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var noHp by rememberSaveable { mutableStateOf("") }

    // - remember: Menjaga state selama rekomposisi UI, namun nilai akan di-reset jika Activity dihancurkan.
    var password by remember { mutableStateOf("") }
    var konfirmasiPassword by remember { mutableStateOf("") }
    var setuju by rememberSaveable { mutableStateOf(false) }
    var passwordTerlihat by rememberSaveable { mutableStateOf(false) }

    // State status pengiriman dan hasil validasi
    var sudahSubmit by remember { mutableStateOf(false) }
    var berhasil by remember { mutableStateOf(false) }

    // =========================================================================
    // 2. FORM VALIDATION (VALIDASI FORMULIR)
    // Evaluasi validasi dihitung secara reaktif/deklaratif setiap kali terjadi perubahan state input.
    // =========================================================================
    val errorNama = sudahSubmit && nama.trim().isEmpty()
    val errorEmail = sudahSubmit && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val errorNoHp = sudahSubmit && (noHp.trim().length < 10 || !noHp.all { it.isDigit() })
    val errorPassword = sudahSubmit && password.length < 8
    val errorKonfirmasi = sudahSubmit && konfirmasiPassword != password
    val errorPersetujuan = sudahSubmit && !setuju

    val formValid = nama.trim().isNotEmpty() &&
            noHp.trim().length >= 10 &&
            noHp.all { it.isDigit() } &&
            Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
            password.length >= 8 && konfirmasiPassword == password && setuju

    // [MODERN UI] Radius seragam untuk seluruh field dan tombol agar selaras
    val roundedFieldShape = RoundedCornerShape(14.dp)

    // Layout utama layar dengan dukungan scroll vertikal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // [MODERN UI] Background abu-abu ultra-light modern
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header aplikasi
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Aplikasi Portal Umsida", fontSize = 14.sp, color = ModernDarkSlate, fontWeight = FontWeight.SemiBold)
            Text("Login", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text("Isi data diri Anda di bawah ini secara lengkap.", color = Color(0xFF64748B), fontSize = 14.sp)
        }

        // Kartu kontainer formulir
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp), // [MODERN UI] Radius lengkung sudut kartu yang lebih smooth
            colors = CardDefaults.cardColors(containerColor = Color.White), // [MODERN UI] Warna putih bersih
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // =========================================================================
                // 3. ANATOMI FIELD (OutlinedTextField) & EVENT HANDLING
                // =========================================================================

                // Anatomi Field 1: Input Nama Lengkap
                OutlinedTextField(
                    value = nama, // [Anatomi Field: Value] State penampung teks nama
                    onValueChange = { // [EVENT] Dipanggil setiap ada perubahan teks dari input pengguna
                        nama = it
                        berhasil = false // [EVENT Effect] Reset status berhasil saat pengguna mengubah input
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = roundedFieldShape, // [MODERN UI] Sudut membulat modern
                    label = { Text("Nama Lengkap") }, // [Anatomi Field: Label]
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }, // [Anatomi Field: Leading Icon]
                    singleLine = true,
                    isError = errorNama, // [Anatomi Field & Validasi: Status Error]
                    supportingText = { if (errorNama) Text("Nama wajib diisi") } // [Anatomi Field & Validasi: Supporting Text]
                )

                // Anatomi Field 2: Input Email
                OutlinedTextField(
                    value = email, // [Anatomi Field: Value]
                    onValueChange = { // [EVENT]
                        email = it
                        berhasil = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = roundedFieldShape,
                    label = { Text("Email Aktif") }, // [Anatomi Field: Label]
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) }, // [Anatomi Field: Leading Icon]
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // [Anatomi Field: Keyboard Options]
                    isError = errorEmail, // [Anatomi Field & Validasi: Status Error]
                    supportingText = { if (errorEmail) Text("Masukkan alamat email yang valid") }
                )

                // Anatomi Field 3: Input Nomor Telepon
                OutlinedTextField(
                    value = noHp,
                    onValueChange = {
                        noHp = it
                        berhasil = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = roundedFieldShape,
                    label = { Text("Nomor WhatsApp / HP") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) }, // [MODERN UI] Ikon diperbaiki ke Phone
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = errorNoHp,
                    supportingText = { if (errorNoHp) Text("Nomor HP minimal 10 digit angka") }
                )

                // Anatomi Field 4: Input Kata Sandi
                OutlinedTextField(
                    value = password, // [Anatomi Field: Value]
                    onValueChange = { // [EVENT]
                        password = it
                        berhasil = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = roundedFieldShape,
                    label = { Text("Kata sandi yang kuat") }, // [Anatomi Field: Label]
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) }, // [Anatomi Field: Leading Icon]
                    trailingIcon = { // [Anatomi Field: Trailing Icon] Tombol toggle visibilitas kata sandi
                        IconButton(onClick = { // [EVENT] Toggle nilai boolean passwordTerlihat saat ikon diklik
                            passwordTerlihat = !passwordTerlihat
                        }) {
                            Icon(
                                imageVector = if (passwordTerlihat) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordTerlihat) "Sembunyikan kata sandi" else "Tampilkan kata sandi"
                            )
                        }
                    },
                    visualTransformation = if (passwordTerlihat) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorPassword, // [Anatomi Field & Validasi: Status Error]
                    supportingText = { if (errorPassword) Text("Kata sandi minimal 8 karakter") }
                )

                // Anatomi Field 5: Input Konfirmasi Kata Sandi
                OutlinedTextField(
                    value = konfirmasiPassword, // [Anatomi Field: Value]
                    onValueChange = { // [EVENT]
                        konfirmasiPassword = it
                        berhasil = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = roundedFieldShape,
                    label = { Text("Konfirmasi kata sandi anda") }, // [Anatomi Field: Label]
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) }, // [Anatomi Field: Leading Icon]
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorKonfirmasi, // [Anatomi Field & Validasi: Status Error]
                    supportingText = { if (errorKonfirmasi) Text("Konfirmasi harus sama dengan kata sandi") }
                )

                // Checkbox Persetujuan Ketentuan (Event & Validasi)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = setuju, // [State] Menunjukkan status centang
                        onCheckedChange = { // [EVENT] Dipanggil saat Checkbox diklik
                            setuju = it
                            berhasil = false
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Saya menyetujui ketentuan penggunaan", fontSize = 13.sp, color = Color(0xFF475569))
                }
                if (errorPersetujuan) { // [Form Validation: Pesan error persetujuan]
                    Text("Persetujuan wajib dicentang", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // =========================================================================
                // 4. EVENT SUBMIT FORMULIR
                // =========================================================================
                Button(
                    onClick = { // [EVENT] Dipanggil saat tombol "Daftar" diklik oleh pengguna
                        sudahSubmit = true // Memicu pembaruan state bahwa formulir telah dikirim
                        berhasil = formValid // Menyimpan hasil validasi formulir
                        if (formValid) {
                            onLoginSuccess(nama.trim()) // Pemicu pindah halaman ke dashboard lewat parameter lambda
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp), // [MODERN UI] Tombol dibuat lebih tebal & ergonomis
                    shape = roundedFieldShape, // [MODERN UI] Bentuk lengkung serasi dengan text field
                    colors = ButtonDefaults.buttonColors(containerColor = ModernDarkSlate)
                ) {
                    Text("Daftar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // Tampilan Pesan Sukses jika validasi berhasil
                if (berhasil) {
                    HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFECFDF5), RoundedCornerShape(12.dp)) // [MODERN UI] Container notifikasi hijau
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                        Column {
                            Text("Validasi berhasil", color = Color(0xFF065F46), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Halo, ${nama.trim()}! Formulir siap diproses.", color = Color(0xFF047857), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// HALAMAN 2: ADAPTIVE UI, COMPOSE, EVENT
// ==========================================

/**
- PENJELASAN PARAMETER:
- @param studentName [STATE HOISTING]: Menerima data nama mahasiswa hasil operan dari layar Login/RegistrationScreen.
- @param onNavigateSettings [EVENT CALLBACK]: Fungsi aksi tanpa return (lambda) untuk menembak event pindah ke layar Settings.
- @param onLogout [EVENT CALLBACK]: Fungsi aksi (lambda) untuk menembak event logout balik ke layar Registrasi.
 */
@Composable
fun DashboardScreen(
    studentName: String,
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit
) {
    // =========================================================================
    // STATE MANAGEMENT DI JETPACK COMPOSE
    // =========================================================================
    // [STATE 1]: Menyimpan status boolean apakah kotak jadwal kuliah sedang dibuka atau disembunyikan.
    var showSchedule by rememberSaveable { mutableStateOf(false) }

    // [STATE 2]: Menyimpan angka counter tugas yang terselesaikan.
    var activityCount by rememberSaveable { mutableIntStateOf(0) }

    // [COMPOSE LAYOUT]: Column utama membungkus seluruh konten secara vertikal dan mendukung scrolling
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
    ) {
        // HEADER HERO BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaroonUmsida)
                .padding(24.dp)
        ) {
            Column {
                Text("Aplikasi Portal UMSIDA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Dashboard Mahasiswa", color = Color.LightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // CONTAINER KONTEN UTAMA
        Column(modifier = Modifier.padding(16.dp)) {
            // KARTU SAMBUTAN MAHASISWA & JADWAL KULIAH
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp), // [EFEK VISUAL]: Menggeser kartu ke atas 30dp
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // [COMPOSE - MENGGUNAKAN PARAMETER]: Data nama mahasiswa dirender di sini
                    Text("Halo, $studentName!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaroonUmsida)
                    Text("Selamat datang di dashboard akademik mobile.", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    // =========================================================================
                    // EVENT 1: TOGGLE VISIBILITY JADWAL
                    // =========================================================================
                    Button(
                        // [EVENT]: Aksi klik tombol membalik nilai boolean State
                        onClick = { showSchedule = !showSchedule },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ModernDarkSlate)
                    ) {
                        Text(if (showSchedule) "Sembunyikan Jadwal" else "Lihat Jadwal Kuliah")
                    }

                    // [COMPOSE CONDITIONAL RENDERING]: Jadwal kuliah hanya dirender jika TRUE
                    if (showSchedule) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text("08.00 • Pemrograman Web", color = ModernDarkSlate, fontWeight = FontWeight.Medium)
                            Text("10.00 • Basis Data", color = ModernDarkSlate, fontWeight = FontWeight.Medium)
                            Text("13.00 • Jaringan Komputer", color = ModernDarkSlate, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Text("Ringkasan Akademik", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaroonUmsida)
            Spacer(modifier = Modifier.height(8.dp))

            // =========================================================================
            // ADAPTIVE UI CONTAINER (BoxWithConstraints)
            // =========================================================================
            BoxWithConstraints {
                // [LOGIKA BREAKPOINT ADAPTIF]: 600.dp adalah patokan Material Design
                if (maxWidth > 600.dp) {
                    // --- SKENARIO 1: TAMPILAN LAYAR LEBAR (Tablet / Landscape) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryCard(title = "SKS Lulus", value = "84", modifier = Modifier.weight(1f))
                        SummaryCard(title = "IPK", value = "3.75", modifier = Modifier.weight(1f))
                        SummaryCard(title = "Tugas Aktif", value = "5", modifier = Modifier.weight(1f))
                    }
                } else {
                    // --- SKENARIO 2: TAMPILAN LAYAR SEMPIT (Smartphone Potrait) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryCard(title = "SKS", value = "84", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        SummaryCard(title = "IPK", value = "3.75", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        SummaryCard(title = "Tugas", value = "5", modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // EVENT 2: COUNTER AKTIVITAS BELAJAR (INTERAKTIF STATE MUTATION)
            // =========================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Aktivitas Belajar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Catat tugas selesai", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Menampilkan nilai State 'activityCount' secara real-time
                        Text("$activityCount tugas terselesaikan", fontWeight = FontWeight.Bold, color = ModernDarkSlate)

                        // [REAKTIF UI LOGIC]: Pesan reward otomatis muncul
                        if (activityCount >= 5) {
                            Text("🎉 Luar biasa! Target tercapai!", color = Color(0xFF388E3C), fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // [EVENT +1]: Menambah nilai State activityCount
                        Button(
                            onClick = { activityCount++ },
                            colors = ButtonDefaults.buttonColors(containerColor = ModernDarkSlate),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("+1")
                        }

                        // [EVENT RESET]: Mengembalikan nilai State ke angka 0
                        OutlinedButton(
                            onClick = { activityCount = 0 },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("RESET", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // =========================================================================
            // EVENT 3: NAVIGASI HALAMAN (MENGGUNAKAN PARAMETER LAMBDA CALLBACK)
            // =========================================================================
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // [EVENT NAVIGASI SETTINGS]
                OutlinedButton(
                    onClick = onNavigateSettings,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pengaturan")
                }

                // [EVENT LOGOUT]
                Button(
                    onClick = onLogout,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Keluar")
                }
            }
        }
    }
}

/**
- [REUSABLE COMPONENT]: Komponen kartu kecil untuk menampilkan ringkasan data akademik.
- PENJELASAN PARAMETER:
- @param title Label kartu (misal: "SKS", "IPK")
- @param value Nilai data yang dicetak tebal
- @param modifier Modifier fleksibel yang di-pass dari parent
 */
@Composable
fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp), // Mengunci tinggi kartu seragam
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 14.sp, color = Color.Gray)
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaroonUmsida)
        }
    }
}

// ==========================================
// HALAMAN 3: SETTINGS
// ==========================================
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    // =========================================================================
    // STATE MANAGEMENT: TOGGLE SWITCH
    // =========================================================================
    var isNotifEnabled by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(24.dp)
    ) {
        Text("Pengaturan", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaroonUmsida)
        Spacer(modifier = Modifier.height(32.dp))

        // =========================================================================
        // KARTU TOGGLE PENGATURAN NOTIFIKASI
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sisi Kiri: Label Pengaturan & Status Reaktif
                Column {
                    Text("Notifikasi Jadwal Kuliah", fontSize = 16.sp, fontWeight = FontWeight.Medium)

                    // [REAKTIF UI]: Teks berubah mengikuti State 'isNotifEnabled'
                    Text(
                        text = if (isNotifEnabled) "Aktif" else "Nonaktif",
                        fontSize = 14.sp,
                        color = if (isNotifEnabled) Color.Green else Color.Red
                    )
                }

                // Sisi Kanan: Komponen Interaktif Switch
                // [STATE DOWN & EVENT UP]
                Switch(
                    checked = isNotifEnabled,
                    onCheckedChange = { isNotifEnabled = it }
                )
            }
        }

        // =========================================================================
        // TRIK LAYOUT: FLEXIBLE SPACER (PUSHER)
        // =========================================================================
        // Spacer dengan 'weight(1f)' menyerap seluruh sisa ruang kosong vertikal.
        Spacer(modifier = Modifier.weight(1f))

        // =========================================================================
        // EVENT KEMBALI KE DASHBOARD
        // =========================================================================
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kembali ke Dashboard")
        }
    }
}

// =============================================================================
// PREVIEW TOOLING (ANDROID STUDIO)
// =============================================================================
@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    Pertemuan6Theme {
        // Memanggil RegistrationScreen tanpa parameter karena memiliki nilai default
        RegistrationScreen()
    }
}