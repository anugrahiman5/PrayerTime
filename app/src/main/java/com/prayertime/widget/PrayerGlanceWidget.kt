package com.prayertime.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import com.prayertime.MainActivity
import com.prayertime.R
import com.prayertime.data.datastore.PrayerPreferences
import com.prayertime.data.repository.CalculationMethodType
import com.prayertime.data.repository.PrayerRepository

/**
 * Token desain khusus antarmuka Jetpack Glance Widget yang mendukung adaptasi otomatis Light/Dark mode.
 */
object WidgetTokens {
    /** Latar belakang kontainer terluar widget */
    val ContainerBg = ColorProvider(day = Color(0xFFDCE7DF), night = Color(0xFF121A15))
    /** Latar belakang kartu putih di dalam widget */
    val CardBg = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1B2620))
    /** Latar belakang kapsul hitung mundur */
    val PillBg = ColorProvider(day = Color(0xFFDEEAE1), night = Color(0xFF24352C))
    /** Latar belakang kapsul lokasi */
    val LocationPillBg = ColorProvider(day = Color(0xFFF0F4F1), night = Color(0xFF223028))
    /** Warna hijau brand utama */
    val PrimaryGreen = ColorProvider(day = Color(0xFF1E4636), night = Color(0xFF5ED396))
    /** Warna teks jam digital besar */
    val DisplayTimeGreen = ColorProvider(day = Color(0xFF174332), night = Color(0xFFE8F3EC))
    /** Aksen merah */
    val AccentRed = ColorProvider(day = Color(0xFFBA3C2A), night = Color(0xFFFF7B6B))
    /** Warna teks gelap utama */
    val TextDark = ColorProvider(day = Color(0xFF1A231E), night = Color(0xFFE2ECE5))
    /** Warna teks sekunder / muted */
    val TextMuted = ColorProvider(day = Color(0xFF5F6E65), night = Color(0xFF8FABA0))
    /** Garis pemisah tipis */
    val DividerColor = ColorProvider(day = Color(0xFFEFF3EF), night = Color(0xFF293B32))

    // Shapes & Corner Radii
    val OuterCorner = 24.dp
    val HeroCardCorner = 18.dp
    val MiniCardCorner = 12.dp
    val PillCorner = 100.dp
}

/**
 * Item shalat mendatang yang ditampilkan pada kolom kanan widget versi Expanded (4x2).
 *
 * @property name Nama shalat (misal: "Maghrib").
 * @property time Jam masuk shalat (misal: "17:52").
 * @property iconResId Resource ID ikon cuaca/waktu shalat.
 */
data class UpcomingPrayerWidgetItem(
    val name: String,
    val time: String,
    val iconResId: Int
)

/**
 * Model state data UI yang dirender oleh Glance AppWidget.
 *
 * @property location Nama kota singkat.
 * @property nextPrayerName Nama shalat terdekat berikutnya.
 * @property nextPrayerTime Jam masuk shalat.
 * @property countdownShort Teks hitung mundur singkat untuk mode Compact.
 * @property countdownDetailed Teks hitung mundur rinci jam:menit:detik.
 * @property upcomingList Daftar 3 waktu shalat mendatang berikutnya.
 */
data class PrayerWidgetState(
    val location: String = "Jakarta",
    val nextPrayerName: String = "ASHAR",
    val nextPrayerTime: String = "15:24",
    val countdownShort: String = "dalam 1j 45m",
    val countdownDetailed: String = "Tersisa 01:45:12",
    val upcomingList: List<UpcomingPrayerWidgetItem> = listOf(
        UpcomingPrayerWidgetItem("Maghrib", "17:52", R.drawable.ic_widget_sunset),
        UpcomingPrayerWidgetItem("Isya", "19:03", R.drawable.ic_widget_moon),
        UpcomingPrayerWidgetItem("Subuh (Bsk)", "04:39", R.drawable.ic_widget_sunrise)
    )
)

/**
 * Implementasi widget layar depan Android (AppWidget) menggunakan Jetpack Glance.
 * Mendukung ukuran adaptif:
 * - **Compact Layout (4x1 / 2x2)**: Menampilkan nama kota, shalat berikutnya, jam, dan kapsul hitung mundur.
 * - **Expanded Layout (4x2)**: Menampilkan kartu hero shalat di sisi kiri dan daftar 3 shalat mendatang di sisi kanan.
 */
class PrayerGlanceWidget : GlanceAppWidget() {

    companion object {
        // Responsive break points for widget launcher sizing
        val COMPACT_MODE = DpSize(180.dp, 60.dp)
        val EXPANDED_MODE = DpSize(260.dp, 120.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(COMPACT_MODE, EXPANDED_MODE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetState = try {
            // Ambil data lokasi & metode hisab yang tersimpan di DataStore
            val preferences = PrayerPreferences(context)
            val savedLocation = preferences.getSavedLocationDirect()
            val savedMethodId = preferences.getSavedMethodDirect()
            val method = CalculationMethodType.fromId(savedMethodId)

            val repository = PrayerRepository()
            val data = repository.getWidgetData(
                latitude = savedLocation.latitude,
                longitude = savedLocation.longitude,
                locationShortName = savedLocation.cityName.split(",").firstOrNull()?.trim() ?: "Jakarta",
                method = method
            )
            val nextPrayerTimestamp = data.second

            // Daftarkan AlarmManager adaptif tepat saat waktu shalat berikutnya masuk atau per 30 menit
            val nowMillis = System.currentTimeMillis()
            val thirtyMinutesMillis = nowMillis + 30 * 60 * 1000L
            val targetTrigger = if (nextPrayerTimestamp in (nowMillis + 1)..thirtyMinutesMillis) {
                nextPrayerTimestamp
            } else {
                thirtyMinutesMillis
            }
            PrayerWidgetScheduler.scheduleNextPrayerAlarm(context, targetTrigger)

            data.first
        } catch (e: Exception) {
            Log.e("PrayerGlanceWidget", "Gagal memuat preferensi widget: ${e.message}", e)
            val fallbackRepo = PrayerRepository()
            fallbackRepo.getWidgetData().first
        }

        provideContent {
            val size = LocalSize.current
            val isExpanded = size.height >= 110.dp && size.width >= 240.dp

            if (isExpanded) {
                ExpandedWidgetLayout(state = widgetState)
            } else {
                CompactWidgetLayout(state = widgetState)
            }
        }
    }
}

// ============================================================================
// Layout Varian 1: Compact Layout (4x1 / 2x2)
// ============================================================================
@GlanceComposable
@Composable
fun CompactWidgetLayout(
    state: PrayerWidgetState,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .cornerRadius(WidgetTokens.HeroCardCorner)
            .background(WidgetTokens.CardBg)
            .clickable(actionStartActivity<MainActivity>())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top Row: Indicator Dot + "SHALAT BERIKUTNYA" | Location Badge
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Dot
                Box(
                    modifier = GlanceModifier
                        .size(6.dp)
                        .cornerRadius(WidgetTokens.PillCorner)
                        .background(WidgetTokens.PrimaryGreen)
                ) {}

                Spacer(modifier = GlanceModifier.width(6.dp))

                // Title Label
                Text(
                    text = "SHALAT BERIKUTNYA",
                    style = TextStyle(
                        color = WidgetTokens.PrimaryGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Location Pill Badge: Pin + "Jakarta"
                Row(
                    modifier = GlanceModifier
                        .cornerRadius(WidgetTokens.PillCorner)
                        .background(WidgetTokens.LocationPillBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_location),
                        contentDescription = "Lokasi",
                        colorFilter = ColorFilter.tint(WidgetTokens.TextMuted),
                        modifier = GlanceModifier.size(11.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(3.dp))
                    Text(
                        text = state.location,
                        style = TextStyle(
                            color = WidgetTokens.TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Bottom Row: "ASHAR 15:24" | Pill Badge "🕒 dalam 1j 45m"
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prayer Name & Time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.nextPrayerName,
                        style = TextStyle(
                            color = WidgetTokens.PrimaryGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = state.nextPrayerTime,
                        style = TextStyle(
                            color = WidgetTokens.TextDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Countdown Pill
                Row(
                    modifier = GlanceModifier
                        .cornerRadius(WidgetTokens.PillCorner)
                        .background(WidgetTokens.PillBg)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_clock),
                        contentDescription = "Waktu",
                        colorFilter = ColorFilter.tint(WidgetTokens.PrimaryGreen),
                        modifier = GlanceModifier.size(12.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(
                        text = state.countdownShort,
                        style = TextStyle(
                            color = WidgetTokens.PrimaryGreen,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// ============================================================================
// Layout Varian 2: Expanded Layout (4x2)
// ============================================================================
@GlanceComposable
@Composable
fun ExpandedWidgetLayout(
    state: PrayerWidgetState,
    modifier: GlanceModifier = GlanceModifier
) {
    // Outer Container: Soft sage green rounded card (tap to open app)
    Box(
        modifier = modifier
            .fillMaxSize()
            .cornerRadius(WidgetTokens.OuterCorner)
            .background(WidgetTokens.ContainerBg)
            .clickable(actionStartActivity<MainActivity>())
            .padding(10.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Hero Next Prayer Card (takes ~56% width)
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .cornerRadius(WidgetTokens.HeroCardCorner)
                    .background(WidgetTokens.CardBg)
                    .padding(12.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    // Top Row: Sun Icon + "ASHAR" | Bell Icon
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_sun),
                            contentDescription = "Ashar Icon",
                            colorFilter = ColorFilter.tint(WidgetTokens.PrimaryGreen),
                            modifier = GlanceModifier.size(15.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            text = state.nextPrayerName,
                            style = TextStyle(
                                color = WidgetTokens.PrimaryGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_bell),
                            contentDescription = "Pengingat",
                            colorFilter = ColorFilter.tint(WidgetTokens.PrimaryGreen),
                            modifier = GlanceModifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Big Display Time ("15:24")
                    Text(
                        text = state.nextPrayerTime,
                        style = TextStyle(
                            color = WidgetTokens.DisplayTimeGreen,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Countdown with Hourglass icon
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_hourglass),
                            contentDescription = "Countdown",
                            colorFilter = ColorFilter.tint(WidgetTokens.PrimaryGreen),
                            modifier = GlanceModifier.size(11.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            text = state.countdownDetailed,
                            style = TextStyle(
                                color = WidgetTokens.PrimaryGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Thin Divider Line
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(WidgetTokens.DividerColor)
                    ) {}

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Footer Action: "Buka Waktu Shalat ↗"
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .clickable(actionStartActivity<MainActivity>()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Buka Waktu Shalat",
                            style = TextStyle(
                                color = WidgetTokens.PrimaryGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_arrow_up_right),
                            contentDescription = "Buka",
                            colorFilter = ColorFilter.tint(WidgetTokens.PrimaryGreen),
                            modifier = GlanceModifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Right Stack: Column of 3 mini white cards (takes remaining width)
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
            ) {
                state.upcomingList.forEachIndexed { index, prayerItem ->
                    WidgetMiniPrayerCard(
                        item = prayerItem,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    if (index < state.upcomingList.size - 1) {
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }
                }
            }
        }
    }
}

// Mini prayer card component inside expanded right stack
@GlanceComposable
@Composable
private fun WidgetMiniPrayerCard(
    item: UpcomingPrayerWidgetItem,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .cornerRadius(WidgetTokens.MiniCardCorner)
            .background(WidgetTokens.CardBg)
            .clickable(actionStartActivity<MainActivity>())
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = item.name,
                    style = TextStyle(
                        color = WidgetTokens.TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = item.time,
                    style = TextStyle(
                        color = WidgetTokens.TextDark,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Image(
                provider = ImageProvider(item.iconResId),
                contentDescription = item.name,
                colorFilter = ColorFilter.tint(WidgetTokens.PrimaryGreen),
                modifier = GlanceModifier.size(15.dp)
            )
        }
    }
}
