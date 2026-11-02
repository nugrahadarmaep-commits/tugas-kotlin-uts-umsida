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
 * Activity utama aplikasi yang menjadi titik masuk (entry point).
 * Mengatur tema dasar dan menampilkan layar pendaftaran [RegistrationScreen].
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
//val ModernDarkSlate = Color(0xFF0D47A1) //sample ujicoba saya pakai biru tua
val MaroonUmsida = ModernDarkSlate
val BackgroundGray = Color(0xFFF8FAFC)

@Composable
fun PortalUmsidaApp() {
    //var currentScreen by remember { mutableStateOf(Screen.REGISTRATION) } // kalo gapake rememberSaveable biar ga reset saat rotasi layar
    //var loggedInName by remember { mutableStateOf("") }
    var currentScreen by rememberSaveable { mutableStateOf(Screen.REGISTRATION) }
    var loggedInName by rememberSaveable { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (currentScreen) {
            Screen.REGISTRATION -> RegistrationScreen(
                onLoginSuccess = { name ->
                    loggedInName = name
                    currentScreen = Screen.DASHBOARD
                }
            )
            Screen.DASHBOARD -> DashboardScreen(
                studentName = loggedInName,
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
 * Screen-level state owner untuk contoh state hoisting dasar.
 * Memperagakan penerapan State, Event, Anatomi Field (OutlinedTextField), dan Form Validation di Jetpack Compose.
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
    //   Password bersifat sementara (transient) dan tidak disimpan dalam saved UI state demi alasan keamanan.
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
    // Evaluasi pesan error untuk setiap field (hanya ditampilkan setelah tombol submit ditekan)
    val errorNama = sudahSubmit && nama.trim().isEmpty()
    val errorEmail = sudahSubmit && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val errorNoHp = sudahSubmit && (noHp.trim().length < 10 || !noHp.all { it.isDigit() })
    val errorPassword = sudahSubmit && password.length < 8
    val errorKonfirmasi = sudahSubmit && konfirmasiPassword != password
    val errorPersetujuan = sudahSubmit && !setuju

    // Seluruh formulir dinyatakan valid jika semua kriteria terpenuhi:
    // 1. Nama tidak kosong.
    // 2. Email sesuai format alamat email yang valid.
    // 3. Nomor HP minimal 10 digit angka.
    // 4. Password minimal 8 karakter.
    // 5. Konfirmasi password sama dengan password.
    // 6. Checkbox persetujuan telah dicentang.
    val formValid = nama.trim().isNotEmpty() &&
            noHp.trim().length >= 10 &&
            noHp.all { it.isDigit() } &&
            Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
            password.length >= 8 && konfirmasiPassword == password && setuju

    // Layout utama layar dengan dukungan scroll vertikal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header aplikasi
        Text("Aolikasi Portal Umsida", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Login", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF20213A))
        Text("Isi data diri anda dibawah ini secara lengkap.", color = Color(0xFF666779), fontSize = 14.sp)

        // Kartu kontainer formulir
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // =========================================================================
                // 3. ANATOMI FIELD (OutlinedTextField) & EVENT HANDLING
                // Anatomi Field di Jetpack Compose terdiri dari:
                // - value: Nilai teks saat ini dari State.
                // - onValueChange (EVENT): Callback yang dipanggil saat pengguna mengetik/mengubah nilai.
                // - label: Label melayang di atas field.
                // - leadingIcon: Ikon di sisi kiri field.
                // - trailingIcon: Ikon aksi/pilihan di sisi kanan field.
                // - visualTransformation: Mengubah penampakan teks (misal: menyembunyikan password).
                // - isError: Indikator boolean untuk menandai status error.
                // - supportingText: Teks petunjuk atau pesan kesalahan di bawah field.
                // - keyboardOptions: Mengatur tipe input keyboard virtual (misal: Email, Password).
                // =========================================================================

                // Anatomi Field 1: Input Nama Lengkap
                OutlinedTextField(
                    value = nama, // [Anatomi Field: Value] State penampung teks nama
                    onValueChange = { // [EVENT] Dipanggil setiap ada perubahan teks dari input pengguna
                        nama = it
                        berhasil = false // [EVENT Effect] Reset status berhasil saat pengguna mengubah input
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nama") }, // [Anatomi Field: Label]
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
                    label = { Text("Email Aktif") }, // [Anatomi Field: Label]
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) }, // [Anatomi Field: Leading Icon]
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // [Anatomi Field: Keyboard Options]
                    isError = errorEmail, // [Anatomi Field & Validasi: Status Error]
                    supportingText = { if (errorEmail) Text("Masukkan alamat email yang valid") } // [Anatomi Field & Validasi: Supporting Text]
                )

                // Anatomi Field 3: Input Nomor Telepon
                OutlinedTextField(
                    value = noHp,
                    onValueChange = {
                        noHp = it
                        berhasil = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nomor WhatsApp / HP") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
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
                    visualTransformation = if (passwordTerlihat) VisualTransformation.None else PasswordVisualTransformation(), // [Anatomi Field: Visual Transformation]
                    singleLine = true,
                    isError = errorPassword, // [Anatomi Field & Validasi: Status Error]
                    supportingText = { if (errorPassword) Text("Kata sandi minimal 8 karakter") } // [Anatomi Field & Validasi: Supporting Text]
                )

                // Anatomi Field 5: Input Konfirmasi Kata Sandi
                OutlinedTextField(
                    value = konfirmasiPassword, // [Anatomi Field: Value]
                    onValueChange = { // [EVENT]
                        konfirmasiPassword = it
                        berhasil = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Konfirmasi kata sandi anda") }, // [Anatomi Field: Label]
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) }, // [Anatomi Field: Leading Icon]
                    visualTransformation = PasswordVisualTransformation(), // [Anatomi Field: Visual Transformation]
                    singleLine = true,
                    isError = errorKonfirmasi, // [Anatomi Field & Validasi: Status Error]
                    supportingText = { if (errorKonfirmasi) Text("Konfirmasi harus sama dengan kata sandi") } // [Anatomi Field & Validasi: Supporting Text]
                )

                // Checkbox Persetujuan Ketentuan (Event & Validasi)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = setuju, // [State] Menunjukkan status centang
                        onCheckedChange = { // [EVENT] Dipanggil saat Checkbox diklik
                            setuju = it
                            berhasil = false
                        }
                    )
                    Text("Saya menyetujui ketentuan penggunaan", fontSize = 13.sp)
                }
                if (errorPersetujuan) { // [Form Validation: Pesan error persetujuan]
                    Text("Persetujuan wajib dicentang", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // =========================================================================
                // 4. EVENT SUBMIT FORMULIR
                // Mengatur alur saat pengguna menekan tombol pendaftaran.
                // =========================================================================
                Button(
                    onClick = { // [EVENT] Dipanggil saat tombol "Daftar" diklik oleh pengguna
                        sudahSubmit = true // Memicu pembaruan state bahwa formulir telah dikirim
                        berhasil = formValid // Menyimpan hasil validasi formulir
                        if (formValid) {
                            onLoginSuccess(nama.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Daftar", modifier = Modifier.padding(vertical = 3.dp))
                }

                // Tampilan Pesan Sukses jika validasi berhasil
                if (berhasil) {
                    HorizontalDivider(color = Color(0xFFD9EAD9))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF27834A))
                        Column {
                            Text("Validasi berhasil", color = Color(0xFF237A42), fontWeight = FontWeight.Bold)
                            Text("Halo, ${nama.trim()}! Formulir siap diproses.", color = Color(0xFF44534A), fontSize = 13.sp)
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
@Composable
fun DashboardScreen(studentName: String, onNavigateSettings: () -> Unit, onLogout: () -> Unit) {
    var showSchedule by rememberSaveable { mutableStateOf(false) }
    //var showSchedule = false
    var activityCount by rememberSaveable { mutableIntStateOf(0) }
    //var activityCount by remember { mutableIntStateOf(0)}
    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray).verticalScroll(rememberScrollState())) {

        Box(modifier = Modifier.fillMaxWidth().background(MaroonUmsida).padding(24.dp)) {
            Column {
                Text("Aplikasi Portal UMSIDA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Dashboard Mahasiswa", color = Color.LightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().offset(y = (-30).dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Halo, $studentName!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaroonUmsida)
                    Text("Selamat datang di dashboard akademik mobile.", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showSchedule = !showSchedule },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ModernDarkSlate)
                    ) {
                        Text(if (showSchedule) "Sembunyikan Jadwal" else "Lihat Jadwal Kuliah")
                    }

                    if (showSchedule) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(modifier = Modifier.background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp)).padding(16.dp).fillMaxWidth()) {
                            Text("08.00 • Pemrograman Web", color = ModernDarkSlate, fontWeight = FontWeight.Medium)
                            Text("10.00 • Basis Data", color = ModernDarkSlate, fontWeight = FontWeight.Medium)
                            Text("13.00 • Jaringan Komputer", color = ModernDarkSlate, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Text("Ringkasan Akademik", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaroonUmsida)
            Spacer(modifier = Modifier.height(8.dp))

            BoxWithConstraints {
                if (maxWidth > 600.dp) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryCard("SKS Lulus", "84", Modifier.weight(1f))
                        SummaryCard("IPK", "3.75", Modifier.weight(1f))
                        SummaryCard("Tugas Aktif", "5", Modifier.weight(1f))
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryCard("SKS", "84", Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        SummaryCard("IPK", "3.75", Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        SummaryCard("Tugas", "5", Modifier.weight(1f))
                    }
                }
            }
            //Column(
            //    modifier = Modifier.fillMaxWidth(),
            //    verticalArrangement = Arrangement.spacedBy(8.dp)
            //) {
            //    SummaryCard("SKS Lulus", "84", Modifier.fillMaxWidth())
            //    SummaryCard("IPK", "3.75", Modifier.fillMaxWidth())
            //    SummaryCard("Tugas Aktif", "5", Modifier.fillMaxWidth())
            //}

            Spacer(modifier = Modifier.height(16.dp))

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
                        Text("$activityCount tugas terselesaikan", fontWeight = FontWeight.Bold, color = ModernDarkSlate)

                        if (activityCount >= 5) {
                            Text("🎉 Luar biasa! Target tercapai!", color = Color(0xFF388E3C), fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { activityCount++ },
                            colors = ButtonDefaults.buttonColors(containerColor = ModernDarkSlate),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("+1")
                        }
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

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onNavigateSettings, modifier = Modifier.weight(1f)) { Text("Pengaturan") }
                Button(onClick = onLogout, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) { Text("Keluar") }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 14.sp, color = Color.Gray)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaroonUmsida)
        }
    }
}

// ==========================================
// HALAMAN 3: SETTINGS
// ==========================================
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var isNotifEnabled by rememberSaveable { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray).padding(24.dp)) {
        Text("Pengaturan", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaroonUmsida)
        Spacer(modifier = Modifier.height(32.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Notifikasi Jadwal Kuliah", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(text = if (isNotifEnabled) "Aktif" else "Nonaktif", fontSize = 14.sp, color = if (isNotifEnabled) Color.Green else Color.Red)
                }
                Switch(checked = isNotifEnabled, onCheckedChange = { isNotifEnabled = it })
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Kembali ke Dashboard")
        }
    }
}

/**
 * Fungsi Preview untuk menampilkan tampilan [RegistrationScreen] pada Android Studio Interactive Preview.
 * Menggunakan tema [Pertemuan6Theme].
 */
@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    Pertemuan6Theme {
        RegistrationScreen()
    }
}