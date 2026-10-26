package com.fajralarm.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fajralarm.alarm.AlarmRingingService
import com.fajralarm.data.AlarmEntity
import com.fajralarm.data.AlarmRepository
import com.fajralarm.data.WakeEventEntity
import com.fajralarm.missions.MissionRules
import com.fajralarm.missions.photo.PhotoLabeler
import com.fajralarm.missions.pushups.PoseRepCounter
import com.fajralarm.missions.shake.ShakeMission
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@HiltViewModel
class RingViewModel @Inject constructor(private val repo: AlarmRepository) : ViewModel() {
    var alarm by mutableStateOf<AlarmEntity?>(null)
        private set
    private var startedAt = System.currentTimeMillis()
    fun load(id: Long) = viewModelScope.launch { alarm = repo.byId(id); startedAt = System.currentTimeMillis() }
    fun complete(id: Long, context: Context, successful: Boolean = true) = viewModelScope.launch {
        repo.record(WakeEventEntity(alarmId = id, startedAt = startedAt, completedAt = System.currentTimeMillis(), successful = successful))
        AlarmRingingService.stop(context)
        (context as? androidx.activity.ComponentActivity)?.finish()
    }
}

@Composable
fun AlarmTaskScreen(alarmId: Long, vm: RingViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val alarm by remember(vm, alarmId) { vm.load(alarmId); derivedStateOf { vm.alarm } }
    var emergencyVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(10 * 60 * 1000L); emergencyVisible = true }
    Column(Modifier.fillMaxSize().background(Color(0xFF0B1117)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("GOOD MORNING", color = Color(0xFFB7D8CC), letterSpacing = 4.sp)
        Spacer(Modifier.height(14.dp))
        Text(alarm?.label ?: "Wake up", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(32.dp))
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF151E26)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("COMPLETE YOUR MISSION", color = Color(0xFFE7BE87), style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
                Spacer(Modifier.height(16.dp))
                when (alarm?.mission ?: "SHAKE") {
                    "SHAKE" -> ShakeTask(alarm?.missionTarget ?: 30) { vm.complete(alarmId, context) }
                    "MATH" -> MathTask(alarm?.missionTarget ?: 5, context) { vm.complete(alarmId, context) }
                    "TEXT" -> TextTask(context) { vm.complete(alarmId, context) }
                    "STEPS" -> StepsTask(alarm?.missionTarget ?: 50, context) { vm.complete(alarmId, context) }
                    "PHOTO" -> PhotoTask(context) { vm.complete(alarmId, context) }
                    "PUSH_UPS" -> PushupTask(alarm?.missionTarget ?: 10, context) { vm.complete(alarmId, context) }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("No snooze. You’ve got this.", color = Color.White.copy(alpha = .55f))
        if (emergencyVisible) {
            Spacer(Modifier.height(24.dp))
            var held by remember { mutableIntStateOf(0) }
            TextButton(onClick = { held++; if (held >= 5) vm.complete(alarmId, context, successful = false) }) { Text(if (held == 0) "Emergency option" else "Tap 5 times to stop", color = Color.White.copy(alpha = .35f), style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable private fun MissionButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) { Text(text, fontSize = 17.sp) }
}

@Composable fun ShakeTask(target: Int, done: () -> Unit) {
    val context = LocalContext.current
    var count by remember { mutableIntStateOf(0) }
    val mission = remember(target) { ShakeMission(target) }
    DisposableEffect(Unit) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) { if (mission.onAcceleration(event.values[0], event.values[1], event.values[2], android.os.SystemClock.elapsedRealtime())) { count = mission.count; done() } }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensor?.let { manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        onDispose { manager.unregisterListener(listener) }
    }
    Text("Shake your phone", color = Color.White, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp)); Text("$count / $target", color = Color(0xFFB7D8CC), fontSize = 38.sp)
    LinearProgressIndicator(progress = { (count.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
    Text(if (count >= target) "Mission complete" else "Keep going to turn off the alarm", color = Color.White.copy(alpha = .7f))
}

@Composable fun MathTask(target: Int, context: Context, done: () -> Unit) {
    var solved by remember { mutableIntStateOf(0) }
    var answer by remember { mutableStateOf("") }
    var seed by remember { mutableIntStateOf(1) }
    val a = 7 + (seed * 3 % 12); val b = 4 + (seed * 5 % 9)
    Text("Solve to wake your mind", color = Color.White, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp)); Text("$a + $b = ?", color = Color(0xFFB7D8CC), fontSize = 40.sp)
    OutlinedTextField(value = answer, onValueChange = { answer = it.filter(Char::isDigit).take(3) }, label = { Text("Answer") }, singleLine = true)
    Spacer(Modifier.height(10.dp)); Text("$solved / $target correct", color = Color.White.copy(alpha = .7f))
    MissionButton("Check answer", { if (answer.toIntOrNull() == a + b) { solved++; seed++; answer = ""; if (solved >= target) done() } else answer = "" })
}

private const val MORNING_TEXT = "Today is a new beginning."
@Composable fun TextTask(context: Context, done: () -> Unit) {
    var text by remember { mutableStateOf("") }
    Text("Type this sentence exactly", color = Color.White, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp)); Text(MORNING_TEXT, color = Color(0xFFB7D8CC), fontSize = 20.sp)
    OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Your response") })
    Spacer(Modifier.height(12.dp)); MissionButton("Verify", { if (MissionRules.answerMatches(MORNING_TEXT, text)) done() })
}

@Composable fun StepsTask(target: Int, context: Context, done: () -> Unit) {
    var permission by remember { mutableStateOf(context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) }
    var steps by remember { mutableIntStateOf(0) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission = it }
    LaunchedEffect(Unit) { if (!permission && android.os.Build.VERSION.SDK_INT >= 29) permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) }
    DisposableEffect(permission) {
        if (!permission) return@DisposableEffect onDispose { }
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        var start = -1f
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) { if (start < 0) start = event.values[0]; steps = (event.values[0] - start).toInt(); if (steps >= target) done() }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensor?.let { manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }
        onDispose { manager.unregisterListener(listener) }
    }
    Text("Walk a few steps", color = Color.White, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(12.dp))
    Text("$steps / $target", color = Color(0xFFB7D8CC), fontSize = 38.sp)
    if (!permission) Text("Allow activity recognition to use this mission.", color = Color.White.copy(alpha = .7f))
}

@Composable fun PhotoTask(context: Context, done: () -> Unit) {
    var message by remember { mutableStateOf("Take a clear photo of the sky") }
    val labeler = remember { PhotoLabeler() }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) labeler.verify(bitmap, "sky") { matched, labels -> message = if (matched) { done(); "Photo verified" } else "Try again. Detected: ${labels.take(3).joinToString()}" }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) cameraLauncher.launch(null) else message = "Camera permission is needed for this mission." }
    Text("Photo mission", color = Color.White, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(8.dp)); Text(message, color = Color.White.copy(alpha = .75f)); Spacer(Modifier.height(12.dp))
    MissionButton("Open camera", { if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(null) else permissionLauncher.launch(Manifest.permission.CAMERA) })
}

@Composable fun PushupTask(target: Int, context: Context, done: () -> Unit) {
    var granted by remember { mutableStateOf(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var reps by remember { mutableIntStateOf(0) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }
    Text("Push-up challenge", color = Color.White, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(8.dp)); Text("$reps / $target", color = Color(0xFFB7D8CC), fontSize = 32.sp)
    if (granted) PoseCameraPreview(onReps = { reps = it; if (it >= target) done() }) else Text("Camera permission is required.", color = Color.White.copy(alpha = .7f))
}

@Composable private fun PoseCameraPreview(onReps: (Int) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val counter = remember { PoseRepCounter() }
    AndroidView(factory = { ctx ->
        val view = PreviewView(ctx)
        val providerFuture = ProcessCameraProvider.getInstance(ctx)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
            val detector = PoseDetection.getClient(PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE).build())
            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            analysis.setAnalyzer(executor) { proxy ->
                val media = proxy.image
                if (media == null) proxy.close() else detector.process(InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees))
                    .addOnSuccessListener { onReps(counter.onPose(it)) }
                    .addOnCompleteListener { proxy.close() }
            }
            runCatching { provider.unbindAll(); provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis) }
        }, ContextCompat.getMainExecutor(ctx))
        view
    }, modifier = Modifier.fillMaxWidth().height(210.dp))
}
