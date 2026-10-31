package com.example.tugaskotlinutsumsida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PortalUmsidaApp()
            }
        }
    }
}

// Enum untuk navigasi 3 Halaman
enum class Screen { LOGIN, DASHBOARD, SETTINGS }

@Composable
fun PortalUmsidaApp() {
    // STATE: Mengontrol halaman mana yang sedang aktif
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var loggedInName by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (currentScreen) {
            Screen.LOGIN -> LoginScreen(
                onLoginSuccess = { name ->
                    loggedInName = name
                    currentScreen = Screen.DASHBOARD
                }
            )
            Screen.DASHBOARD -> DashboardScreen(
                studentName = loggedInName,
                onNavigateSettings = { currentScreen = Screen.SETTINGS },
                onLogout = { currentScreen = Screen.LOGIN }
            )
            Screen.SETTINGS -> SettingsScreen(
                onBack = { currentScreen = Screen.DASHBOARD }
            )
        }
    }
}

// ==========================================
// HALAMAN 1: FORM VALIDATION & STATE
// ==========================================
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    // STATE untuk menampung inputan user
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // STATE untuk menampung error validasi
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Portal Mahasiswa UMSIDA", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        // EVENT: onValueChange terpicu saat ngetik, mengubah nilai State 'name'
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = false },
            label = { Text("Nama Lengkap") },
            isError = nameError,
            modifier = Modifier.fillMaxWidth()
        )
        if (nameError) {
            Text("Nama wajib diisi bro!", color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = false },
            label = { Text("Email Mahasiswa") },
            isError = emailError,
            modifier = Modifier.fillMaxWidth()
        )
        if (emailError) {
            Text("Email wajib pakai @ dan .com!", color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // EVENT & FORM VALIDATION: Aksi klik tombol ngecek kondisi teks
        Button(
            onClick = {
                // Logika Form Validation
                nameError = name.isBlank()
                emailError = !email.contains("@") || !email.endsWith(".com")

                if (!nameError && !emailError) {
                    onLoginSuccess(name) // Event sukses pindah halaman
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Masuk", fontSize = 16.sp)
        }
    }
}

// ==========================================
// HALAMAN 2: ADAPTIVE UI & COMPOSE
// ==========================================
@Composable
fun DashboardScreen(studentName: String, onNavigateSettings: () -> Unit, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Halo, $studentName! 👋", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Selamat datang di sistem akademik", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        // ADAPTIVE UI: Mengecek lebar layar untuk merubah Layout secara dinamis
        BoxWithConstraints {
            if (maxWidth > 600.dp) {
                // Tampilan Landscape / Layar Lebar -> Menyamping (Row)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DashboardCard("Jadwal Praktikum", Modifier.weight(1f))
                    DashboardCard("Data Nilai UTS", Modifier.weight(1f))
                }
            } else {
                // Tampilan Portrait / Layar Sempit -> Menumpuk (Column)
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DashboardCard("Jadwal Praktikum", Modifier.fillMaxWidth())
                    DashboardCard("Data Nilai UTS", Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Dorong tombol ke bawah

        Button(onClick = onNavigateSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Pengaturan")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Keluar", color = Color.Red)
        }
    }
}

// Komponen tambahan untuk kartu Dashboard
@Composable
fun DashboardCard(title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

// ==========================================
// HALAMAN 3: EVENT & STATE MANAGEMENT
// ==========================================
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    // STATE: Menyimpan nilai notifikasi (True/False)
    var isNotifEnabled by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Pengaturan", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Notifikasi Sistem", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = if (isNotifEnabled) "Status: Aktif" else "Status: Nonaktif",
                    fontSize = 14.sp,
                    color = if (isNotifEnabled) Color.Green else Color.Red
                )
            }

            // EVENT: Aksi user menggeser saklar akan mengubah State isNotifEnabled
            Switch(
                checked = isNotifEnabled,
                onCheckedChange = { newValue -> isNotifEnabled = newValue }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Kembali ke Dashboard")
        }
    }
}