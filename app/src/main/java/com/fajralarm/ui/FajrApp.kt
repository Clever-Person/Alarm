package com.fajralarm.ui

import android.app.TimePickerDialog
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fajralarm.data.AlarmEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class Tab { ALARMS, INSIGHTS, SETTINGS }

@Composable
fun FajrApp(vm: AlarmViewModel = hiltViewModel()) {
    val alarms by vm.alarms.collectAsState()
    val events by vm.events.collectAsState()
    val dark by vm.darkTheme.collectAsState()
    val onboardingComplete by vm.onboardingComplete.collectAsState()
    val timeFormat by vm.timeFormat.collectAsState()
    val defaultTarget by vm.defaultTarget.collectAsState()
    var onboardingPage by remember { mutableIntStateOf(0) }
    var tab by remember { mutableStateOf(Tab.ALARMS) }
    var editor by remember { mutableStateOf<AlarmEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val exactAlarmPermission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { vm.rescheduleEnabled() }

    MaterialTheme(colorScheme = if (dark) androidx.compose.material3.darkColorScheme(primary = Color(0xFFB8E2D1), secondary = Color(0xFFFFC48E), background = Color(0xFF0B1115), surface = Color(0xFF141D22), surfaceVariant = Color(0xFF1D292E), onBackground = Color(0xFFE8F0EC), onSurface = Color(0xFFE8F0EC), onSurfaceVariant = Color(0xFFA5B2B1), outline = Color(0xFF344247)) else androidx.compose.material3.lightColorScheme(primary = Color(0xFF245B4B), secondary = Color(0xFF9A552A), background = Color(0xFFF3F6F3), surface = Color(0xFFFFFFFF), surfaceVariant = Color(0xFFE8EFEB), onSurfaceVariant = Color(0xFF596963))) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) { NavigationBarItem(selected = tab == Tab.ALARMS, onClick = { tab = Tab.ALARMS }, icon = { Icon(Icons.Default.Alarm, null) }, label = { Text("Alarms") }); NavigationBarItem(selected = tab == Tab.INSIGHTS, onClick = { tab = Tab.INSIGHTS }, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Insights") }); NavigationBarItem(selected = tab == Tab.SETTINGS, onClick = { tab = Tab.SETTINGS }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") }) } },
            floatingActionButton = { if (tab == Tab.ALARMS) FloatingActionButton(onClick = { editor = null; showEditor = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(20.dp)) { Icon(Icons.Default.Add, "Add alarm") } }) { padding ->
            when (tab) {
                Tab.ALARMS -> AlarmList(alarms, Modifier.padding(padding), timeFormat, onEdit = { editor = it; showEditor = true }, onToggle = { alarm ->
                    if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    vm.toggle(alarm)
                }, onDelete = { vm.delete(it); Unit })
                Tab.INSIGHTS -> Insights(events, Modifier.padding(padding))
                Tab.SETTINGS -> SettingsScreen(dark, vm::setDark, timeFormat, vm::setTimeFormat, defaultTarget, vm::setDefaultTarget, Modifier.padding(padding))
            }
        }
        if (showEditor) AlarmEditorDialog(editor, defaultTarget, timeFormat == "24h", onDismiss = { showEditor = false }, onSave = {
            vm.save(it)
            if (Build.VERSION.SDK_INT >= 31 && !(context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms())) {
                exactAlarmPermission.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
            }
            showEditor = false
        })
        if (!onboardingComplete) OnboardingDialog(onContinue = { if (onboardingPage < 2) onboardingPage++ else vm.finishOnboarding() }, page = onboardingPage)
    }
}

@Composable private fun AlarmList(alarms: List<AlarmEntity>, modifier: Modifier, timeFormat: String, onEdit: (AlarmEntity) -> Unit, onToggle: (AlarmEntity) -> Unit, onDelete: (AlarmEntity) -> Unit) {
    val nextAlarm = alarms.filter { it.enabled }.minByOrNull { it.hour * 60 + it.minute }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 112.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .16f), modifier = Modifier.size(46.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Alarm, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) }
                }
                Column {
                    Text("FAJR", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                    Text("Make mornings count", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            if (nextAlarm != null) NextAlarmCard(nextAlarm, timeFormat)
            else EmptyAlarmCard()
        }
        item {
            Row(Modifier.fillMaxWidth().padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Your alarms", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("${alarms.size} ${if (alarms.size == 1) "alarm" else "alarms"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (alarms.isEmpty()) {
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("＋", color = MaterialTheme.colorScheme.primary, fontSize = 25.sp, fontWeight = FontWeight.Light)
                        Text("Tap the button below to set your first alarm and choose a wake-up mission.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else items(alarms.sortedWith(compareByDescending<AlarmEntity> { it.enabled }.thenBy { it.hour * 60 + it.minute }), key = { it.id }) { alarm ->
            AlarmCard(alarm, timeFormat, onEdit, onToggle, onDelete)
        }
    }
}

@Composable private fun NextAlarmCard(alarm: AlarmEntity, timeFormat: String) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Box {
            Box(Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 16.dp).size(76.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .08f), CircleShape))
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 19.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = .16f), shape = CircleShape) {
                        Text("UP NEXT", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(7.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(formatAlarmTime(alarm, timeFormat), fontSize = 58.sp, lineHeight = 62.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                Text(alarm.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .88f))
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .45f))
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) { Text("${alarm.missionTarget}", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                    Text("${missionName(alarm.mission)} mission", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Text(repeatSummary(alarm.repeatDays), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable private fun EmptyAlarmCard() {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(22.dp)) {
            Text("A fresh start is waiting", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Set an alarm and pair it with one small action to get out of bed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Choose a time", "Pick a mission", "Start your day").forEachIndexed { index, label ->
                    if (index > 0) Text("·", color = MaterialTheme.colorScheme.primary)
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable private fun AlarmCard(alarm: AlarmEntity, timeFormat: String, onEdit: (AlarmEntity) -> Unit, onToggle: (AlarmEntity) -> Unit, onDelete: (AlarmEntity) -> Unit) {
    Card(shape = RoundedCornerShape(23.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.fillMaxWidth().padding(start = 17.dp, end = 14.dp, top = 16.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(formatAlarmTime(alarm, timeFormat), fontSize = 38.sp, lineHeight = 42.sp, fontWeight = FontWeight.Light, letterSpacing = (-1).sp, color = if (alarm.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(alarm.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = alarm.enabled, onCheckedChange = { onToggle(alarm) })
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(repeatSummary(alarm.repeatDays), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                IconButton(onClick = { onEdit(alarm) }, modifier = Modifier.size(38.dp)) { Icon(Icons.Default.Edit, "Edit alarm", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(19.dp)) }
                IconButton(onClick = { onDelete(alarm) }, modifier = Modifier.size(38.dp)) { Icon(Icons.Default.Delete, "Delete alarm", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(19.dp)) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = if (alarm.enabled) .15f else .08f), shape = CircleShape) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(6.dp).background(if (alarm.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                        Text("${missionName(alarm.mission)} · ${alarm.missionTarget}", style = MaterialTheme.typography.labelMedium, color = if (alarm.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (alarm.vibrate) Text("VIBRATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .8f), letterSpacing = .6.sp)
            }
        }
    }
}

private fun formatAlarmTime(alarm: AlarmEntity, timeFormat: String): String = if (timeFormat == "24h") String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute) else LocalTime.of(alarm.hour, alarm.minute).format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))

private fun missionName(value: String): String = when (value) {
    "PUSH_UPS" -> "Push-ups"
    "SHAKE" -> "Shake"
    "MATH" -> "Math"
    "TEXT" -> "Read"
    "STEPS" -> "Steps"
    "PHOTO" -> "Photo"
    else -> value.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

private fun repeatSummary(value: String): String {
    val days = value.split(',').mapNotNull(String::toIntOrNull).toSet()
    if (days.isEmpty()) return "One time"
    if (days.size == 7) return "Every day"
    if (days == setOf(1, 2, 3, 4, 5)) return "Weekdays"
    if (days == setOf(0, 6)) return "Weekends"
    val labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    return days.sorted().joinToString(" · ") { labels.getOrElse(it) { "" } }
}

@Composable private fun AlarmEditorDialog(alarm: AlarmEntity?, defaultTarget: Int, is24Hour: Boolean, onDismiss: () -> Unit, onSave: (AlarmEntity) -> Unit) {
    val context = LocalContext.current
    var hour by remember { mutableIntStateOf(alarm?.hour ?: 5) }
    var minute by remember { mutableIntStateOf(alarm?.minute ?: 30) }
    var label by remember { mutableStateOf(alarm?.label ?: "Wake up") }
    var mission by remember { mutableStateOf(alarm?.mission ?: "SHAKE") }
    var sound by remember { mutableStateOf(alarm?.sound ?: "alarm") }
    var target by remember { mutableIntStateOf(alarm?.missionTarget ?: defaultTarget) }
    var volume by remember { mutableFloatStateOf((alarm?.volume ?: 75).toFloat()) }
    var vibrate by remember { mutableStateOf(alarm?.vibrate ?: true) }
    var showPreview by remember { mutableStateOf(false) }
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    val selected = remember { mutableStateListOf<Int>().apply { addAll(alarm?.repeatDays?.split(',')?.mapNotNull(String::toIntOrNull) ?: emptyList()) } }
    val missions = listOf("SHAKE", "MATH", "TEXT", "STEPS", "PHOTO", "PUSH_UPS")
    var menu by remember { mutableStateOf(false) }
    var soundMenu by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (alarm == null) "New alarm" else "Edit alarm") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, is24Hour).show() }, modifier = Modifier.fillMaxWidth()) { Text(if (is24Hour) String.format(Locale.getDefault(), "%02d:%02d", hour, minute) else java.time.LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())), fontSize = 30.sp) }
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Alarm label") }, singleLine = true)
                Text("Repeat", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { days.forEachIndexed { index, day -> FilterChip(selected = index in selected, onClick = { if (index in selected) selected.remove(index) else selected.add(index) }, label = { Text(day) }) } }
                Box {
                    OutlinedButton(onClick = { menu = true }) { Text("Mission: ${mission.replace('_',' ')} ▾") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) { missions.forEach { DropdownMenuItem(text = { Text(it.replace('_',' ')) }, onClick = { mission = it; menu = false }) } }
                }
                Box {
                    OutlinedButton(onClick = { soundMenu = true }) { Text("Sound: ${if (sound == "notification") "Notification tone" else "System alarm"} ▾") }
                    DropdownMenu(expanded = soundMenu, onDismissRequest = { soundMenu = false }) {
                        DropdownMenuItem(text = { Text("System alarm") }, onClick = { sound = "alarm"; soundMenu = false })
                        DropdownMenuItem(text = { Text("Notification tone") }, onClick = { sound = "notification"; soundMenu = false })
                    }
                }
                Text("${if (mission == "SHAKE") "Shakes" else if (mission == "STEPS") "Steps" else if (mission == "PUSH_UPS") "Push-ups" else "Challenges"}: $target")
                Slider(value = target.toFloat(), onValueChange = { target = it.toInt() }, valueRange = 1f..100f)
                Text("Alarm volume: ${volume.toInt()}%")
                Slider(value = volume, onValueChange = { volume = it }, valueRange = 10f..100f)
                Row(verticalAlignment = Alignment.CenterVertically) { Text("Vibrate", Modifier.weight(1f)); Switch(checked = vibrate, onCheckedChange = { vibrate = it }) }
                TextButton(onClick = { showPreview = true }) { Text("Preview mission") }
            }
        }, confirmButton = { TextButton(onClick = { onSave(AlarmEntity(id = alarm?.id ?: 0, hour = hour, minute = minute, repeatDays = selected.sorted().joinToString(","), label = label.ifBlank { "Wake up" }, mission = mission, missionTarget = target, sound = sound, volume = volume.toInt(), vibrate = vibrate)) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
    if (showPreview) {
        AlertDialog(onDismissRequest = { showPreview = false }, title = { Text("Try the mission") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (mission) {
                    "SHAKE" -> ShakeTask(target) { showPreview = false }
                    "MATH" -> MathTask(target, context) { showPreview = false }
                    "TEXT" -> TextTask(context) { showPreview = false }
                    "STEPS" -> StepsTask(target, context) { showPreview = false }
                    "PHOTO" -> PhotoTask(context) { showPreview = false }
                    else -> PushupTask(target, context) { showPreview = false }
                }
            }
        }, confirmButton = { TextButton(onClick = { showPreview = false }) { Text("Close") } })
    }
}

@Composable private fun Insights(events: List<com.fajralarm.data.WakeEventEntity>, modifier: Modifier) {
    val successful = events.filter { it.successful }
    val successRate = if (events.isEmpty()) 0 else (successful.size * 100 / events.size)
    val streak = calculateStreak(successful)
    val average = if (successful.isEmpty()) 0L else successful.map { it.completedAt - it.startedAt }.average().toLong() / 60_000L
    val weekStart = java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY)
    val dayCounts = (0..6).map { offset -> successful.count { java.time.Instant.ofEpochMilli(it.completedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate() == weekStart.plusDays(offset.toLong()) } }
    Column(modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Your mornings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricCard("Success", "$successRate%", Modifier.weight(1f)); MetricCard("Streak", "$streak days", Modifier.weight(1f)) }
        MetricCard("Average mission", "${average} min", Modifier.fillMaxWidth())
        Text("This week", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth().height(130.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) { listOf("M","T","W","T","F","S","S").forEachIndexed { index, day -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Box(Modifier.width(24.dp).height(if (dayCounts[index] > 0) 72.dp else 16.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .7f), RoundedCornerShape(12.dp))); Text(day, style = MaterialTheme.typography.labelSmall) } } }
        Text("Each successful wake-up is stored only on this device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f))
    }
}

private fun calculateStreak(events: List<com.fajralarm.data.WakeEventEntity>): Int {
    if (events.isEmpty()) return 0
    val days = events.map { java.time.Instant.ofEpochMilli(it.completedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }.distinct().sortedDescending()
    var streak = 1
    for (i in 1 until days.size) {
        if (days[i - 1].minusDays(1) == days[i]) streak++ else break
    }
    return streak
}

@Composable private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) { Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(18.dp)) { Text(title, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f)); Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium) } } }

@Composable private fun SettingsScreen(dark: Boolean, setDark: (Boolean) -> Unit, timeFormat: String, setTimeFormat: (String) -> Unit, defaultTarget: Int, setDefaultTarget: (Int) -> Unit, modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Card(shape = RoundedCornerShape(22.dp)) { Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Night theme", style = MaterialTheme.typography.titleMedium); Text("A softer screen after dark", style = MaterialTheme.typography.bodySmall) }; Switch(checked = dark, onCheckedChange = setDark) } }
        Card(shape = RoundedCornerShape(22.dp)) { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("Time format", style = MaterialTheme.typography.titleMedium); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = timeFormat == "12h", onClick = { setTimeFormat("12h") }, label = { Text("12-hour") }); FilterChip(selected = timeFormat == "24h", onClick = { setTimeFormat("24h") }, label = { Text("24-hour") }) } } }
        Card(shape = RoundedCornerShape(22.dp)) { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("Default mission difficulty", style = MaterialTheme.typography.titleMedium); Text("$defaultTarget repetitions / steps", style = MaterialTheme.typography.bodySmall); Slider(value = defaultTarget.toFloat(), onValueChange = { setDefaultTarget(it.toInt()) }, valueRange = 5f..100f) } }
        Card(shape = RoundedCornerShape(22.dp)) { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("Default sound", style = MaterialTheme.typography.titleMedium); Text("Uses the alarm tone selected in Android settings", style = MaterialTheme.typography.bodySmall) } }
        Card(shape = RoundedCornerShape(22.dp)) { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("Alarm reliability", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(6.dp)); Text("Allow exact alarms and full-screen notifications when Android asks. Some phones may also need Fajr Alarm set to unrestricted battery use.", style = MaterialTheme.typography.bodyMedium) } }
        Text("Fajr Alarm · 1.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .55f))
    }
}

@Composable private fun OnboardingDialog(page: Int, onContinue: () -> Unit) {
    val titles = listOf("A better way to wake", "Earn your quiet", "Permissions when needed")
    val messages = listOf("Fajr Alarm keeps the experience calm and focused, with a clear view of your upcoming alarms.", "Choose a short mission. The alarm continues until you finish it, with no snooze button.", "Camera, activity, notifications, and exact-alarm access are requested only when you first use the related feature.")
    AlertDialog(onDismissRequest = {}, title = { Text(titles[page]) }, text = { Column { Text(messages[page]); Spacer(Modifier.height(18.dp)); Text("${page + 1} / 3", color = MaterialTheme.colorScheme.primary) } }, confirmButton = { TextButton(onClick = onContinue) { Text(if (page < 2) "Next" else "Start") } })
}
