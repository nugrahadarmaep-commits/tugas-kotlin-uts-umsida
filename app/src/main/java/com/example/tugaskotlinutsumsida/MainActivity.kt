package com.example.tugaskotlinutsumsida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

enum class Screen { LOGIN, DASHBOARD, SETTINGS }

// Warna Kustom
val MaroonUmsida = Color(0xFF800000)
val BackgroundGray = Color(0xFFF3F4F6)

@Composable
fun PortalUmsidaApp() {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var loggedInName by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundGray) {
        when (currentScreen) {
            Screen.LOGIN -> LoginScreen { name ->
                loggedInName = name
                currentScreen = Screen.DASHBOARD
            }
            Screen.DASHBOARD -> DashboardScreen(
                studentName = loggedInName,
                onNavigateSettings = { currentScreen = Screen.SETTINGS },
                onLogout = { currentScreen = Screen.LOGIN }
            )
            Screen.SETTINGS -> SettingsScreen { currentScreen = Screen.DASHBOARD }
        }
    }
}

// ==========================================
// HALAMAN 1: FORM VALIDATION & STATE
// ==========================================
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var checkError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Portal Akademik", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaroonUmsida)
        Text("Isi data diri Anda di bawah ini secara lengkap.", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Nama Lengkap") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError) Text("Nama wajib diisi", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = false },
                    label = { Text("Email Aktif") },
                    isError = emailError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError) Text("Masukkan alamat email yang valid (@)", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it; checkError = false }
                    )
                    Text("Saya menyetujui ketentuan penggunaan", fontSize = 13.sp)
                }
                if (checkError) Text("Persetujuan wajib dicentang", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        nameError = name.isBlank()
                        emailError = !email.contains("@")
                        checkError = !isChecked

                        if (!nameError && !emailError && !checkError) {
                            onLoginSuccess(name)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonUmsida)
                ) {
                    Text("Daftar / Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
    var showSchedule by remember { mutableStateOf(false) }
    var activityCount by remember { mutableIntStateOf(0) }

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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Text(if (showSchedule) "Sembunyikan Jadwal" else "Lihat Jadwal Kuliah")
                    }

                    if (showSchedule) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(modifier = Modifier.background(Color(0xFFE8DEF8), RoundedCornerShape(8.dp)).padding(16.dp).fillMaxWidth()) {
                            Text("08.00 • Pemrograman Web")
                            Text("10.00 • Basis Data")
                            Text("13.00 • Jaringan Komputer")
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

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Aktivitas Belajar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Catat tugas selesai", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$activityCount tugas terselesaikan", fontWeight = FontWeight.Bold, color = MaroonUmsida)

                        if(activityCount >= 5) {
                            Text("🎉 Luar biasa! Target tercapai!", color = Color(0xFF388E3C), fontSize = 12.sp)
                        }
                    }
                    Row {
                        Button(onClick = { activityCount++ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))) { Text("+1") }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { activityCount = 0 }) { Text("RESET") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onNavigateSettings, modifier = Modifier.weight(1f)) { Text("Pengaturan") }
                Button(onClick = onLogout, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Keluar") }
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
    var isNotifEnabled by remember { mutableStateOf(true) }

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