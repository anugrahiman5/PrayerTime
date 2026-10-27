package com.prayertime.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayertime.data.repository.PrayerRepository
import com.prayertime.ui.theme.DirectionAccentRed
import com.prayertime.ui.theme.PrayerCornerTokens
import com.prayertime.ui.theme.prayerColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Layar kompas arah kiblat interaktif real-time.
 * Memanfaatkan sensor rotasi perangkat ([Sensor.TYPE_ROTATION_VECTOR]) dengan frekuensi [SensorManager.SENSOR_DELAY_UI].
 * Menghitung sudut kiblat Great Circle menuju Ka'bah di Makkah, menampilkan piringan kompas berputar halus,
 * serta memberikan umpan balik getaran haptik ([HapticFeedbackType.LongPress]) saat arah ponsel tepat menghadap Ka'bah (toleransi ±3.5°).
 *
 * @param onNavigateBack Callback aksi kembali ke layar sebelumnya.
 * @param latitude Lintang koordinat pengguna.
 * @param longitude Bujur koordinat pengguna.
 * @param locationName Nama kota/daerah tampilan saat ini.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaCompassScreen(
    onNavigateBack: () -> Unit,
    latitude: Double = PrayerRepository.DEFAULT_LATITUDE,
    longitude: Double = PrayerRepository.DEFAULT_LONGITUDE,
    locationName: String = PrayerRepository.DEFAULT_LOCATION_NAME,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val repository = remember { PrayerRepository() }
    val qiblaResult = remember(latitude, longitude) {
        repository.calculateQibla(latitude, longitude)
    }

    var azimuth by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }

    // Sensor Listener untuk kompas real-time via ROTATION_VECTOR
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            hasSensor = false
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (degrees + 360f) % 360f
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Hitung selisih derajat perangkat dengan arah kiblat
    val qiblaTarget = qiblaResult.degree.toFloat()
    val diffAngle = ((qiblaTarget - azimuth + 360f) % 360f)
    val isFacingQibla = abs(diffAngle) <= 3.5f || abs(diffAngle - 360f) <= 3.5f

    var wasFacingQibla by remember { mutableStateOf(false) }
    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla && !wasFacingQibla) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasFacingQibla = isFacingQibla
    }

    // Animasi putaran jarum kompas yang halus
    val animatedCompassRotation by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = tween(durationMillis = 150),
        label = "CompassRotation"
    )

    val dialColor by animateColorAsState(
        targetValue = if (isFacingQibla) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
        label = "DialColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Arah Kiblat",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Kartu Ringkasan Derajat & Jarak
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), PrayerCornerTokens.TimelineCard),
                shape = PrayerCornerTokens.TimelineCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = qiblaResult.degreeFormatted,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = qiblaResult.directionText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.prayerColors.qiblaAccent
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Jarak ke Ka'bah",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = qiblaResult.distanceFormatted,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status Alignment Badge
            Surface(
                shape = PrayerCornerTokens.Pill,
                color = if (isFacingQibla) MaterialTheme.colorScheme.primary else MaterialTheme.prayerColors.subtlePillBg,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFacingQibla) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tepat Menghadap Ka'bah!",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val roundedAzimuth = Math.round(azimuth)
                        Text(
                            text = "Sudut Saat Ini: $roundedAzimuth° (Putar HP Anda)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Kompas Visual Dial & Kaaba Pointer
            Box(
                modifier = Modifier
                    .size(290.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(3.dp, dialColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Dial Kompas yang berputar mengikuti rotasi perangkat
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedCompassRotation)
                ) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Gambar garis-garis penanda derajat
                    for (i in 0 until 360 step 15) {
                        val angleRad = Math.toRadians(i.toDouble() - 90)
                        val isMajor = i % 90 == 0
                        val tickLength = if (isMajor) 18.dp.toPx() else 9.dp.toPx()
                        val stroke = if (isMajor) 2.5f else 1.2f

                        val startX = center.x + (radius - tickLength) * cos(angleRad).toFloat()
                        val startY = center.y + (radius - tickLength) * sin(angleRad).toFloat()
                        val endX = center.x + radius * cos(angleRad).toFloat()
                        val endY = center.y + radius * sin(angleRad).toFloat()

                        drawLine(
                            color = if (isMajor) Color.Gray else Color.LightGray,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = stroke
                        )
                    }
                }

                // Jarum Penunjuk Ka'bah
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedCompassRotation + qiblaTarget),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        // Ikon Ka'bah / Panah Penunjuk Arah Hijau
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🕋",
                                fontSize = 18.sp
                            )
                        }

                        // Garis Panah
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(85.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                // Titik Pusat Kompas
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(3.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Posisikan smartphone secara mendatar untuk akurasi maksimal",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
