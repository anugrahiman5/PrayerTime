package com.prayertime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayertime.ui.theme.PrayerCornerTokens

/**
 * Model data doa harian.
 *
 * @property title Judul doa.
 * @property arabic Lafadz teks Arab berharakat.
 * @property latin Transliterasi bacaan latin.
 * @property translation Arti dan terjemahan dalam Bahasa Indonesia.
 */
data class DoaItem(
    val title: String,
    val arabic: String,
    val latin: String,
    val translation: String
)

/**
 * Layar kumpulan doa harian dan dzikir setelah shalat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoaScreen(
    modifier: Modifier = Modifier
) {
    val doaList = listOf(
        DoaItem(
            title = "Doa Sesudah Adzan",
            arabic = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ",
            latin = "Allahumma rabba hadzihid da'watit tammah, wash shalatil qa'imah, ati muhammadanil wasilata wal fadhilah, wab'atshu maqamam mahmudalladzi wa'adtah.",
            translation = "Ya Allah, Tuhan pemilik seruan yang sempurna dan shalat yang tegak, berilah Nabi Muhammad wasilah dan keutamaan, serta bangkitkanlah beliau di tempat terpuji yang Engkau janjikan."
        ),
        DoaItem(
            title = "Doa Masuk Masjid",
            arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            latin = "Allahummaftah lii abwaaba rahmatik.",
            translation = "Ya Allah, bukakanlah untukku pintu-pintu rahmat-Mu."
        ),
        DoaItem(
            title = "Doa Keluar Masjid",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            latin = "Allahumma inni as-aluka min fadhlik.",
            translation = "Ya Allah, sesungguhnya aku memohon keutamaan dari-Mu."
        ),
        DoaItem(
            title = "Sayyidul Istighfar",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            latin = "Allahumma anta rabbi laa ilaha illa anta khalaqtani wa ana 'abduka wa ana 'ala 'ahdika wa wa'dika mastatha'tu, a'udzubika min syarri ma shana'tu, abu-u laka bini'matika 'alayya, wa abu-u bidzanbi faghfirli fa-innahu laa yaghfirudz dzunuba illa anta.",
            translation = "Ya Allah, Engkau adalah Tuhanku, tiada tuhan selain Engkau yang telah menciptakan aku. Aku adalah hamba-Mu dan aku berada dalam perjanjian serta janji-Mu semampuku..."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Doa Harian & Dzikir",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(doaList) { doa ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), PrayerCornerTokens.TimelineCard),
                    shape = PrayerCornerTokens.TimelineCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = doa.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Teks Arab
                        Text(
                            text = doa.arabic,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 21.sp,
                                lineHeight = 36.sp,
                                textAlign = TextAlign.End
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Transliterasi Latin
                        Text(
                            text = doa.latin,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Terjemahan
                        Text(
                            text = "\"${doa.translation}\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
