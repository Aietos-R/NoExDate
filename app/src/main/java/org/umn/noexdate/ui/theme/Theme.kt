package org.umn.noexdate.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import org.umn.noexdate.utils.AppPreferences

//OPSI 1 - Deep Navy Emerald

private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF0B111A),      // Biru Navy sangat gelap (Midnight Blue)
    surface = Color(0xFF151F2A),         // Biru Navy (Card/Box)
    onBackground = Color(0xFFEDF2F4),    // Putih pucat/Off-white
    onSurface = Color(0xFFEDF2F4),       // Putih pucat/Off-white
    primary = Color(0xFFDCA855),         // Aksen Emas AIETOS (AccentYellow)
    onPrimary = Color(0xFF0B111A),       // Biru Navy (Teks di dalam tombol emas)
    outline = Color(0xFF2A3C45),         // Biru kehijauan gelap (Border)
    onSurfaceVariant = Color(0xFF8D99A1) // Abu-abu kebiruan (Teks mute)
)

////OPSI 2 - charcoal
//private val DarkColorScheme = darkColorScheme(
//    background = Color(0xFF121212),      // Hitam Arang / Material Dark
//    surface = Color(0xFF1E1E1E),         // Abu-abu Arang (Card/Box)
//    onBackground = Color(0xFFE8E4D9),    // Krem pudar
//    onSurface = Color(0xFFE8E4D9),       // Krem pudar
//    primary = Color(0xFFD36F45),         // Aksen Oranye Terra-cotta dari sayap logo
//    onPrimary = Color(0xFFFFFFFF),       // Putih (Teks di dalam tombol oranye)
//    outline = Color(0xFF333333),         // Abu-abu netral (Border)
//    onSurfaceVariant = Color(0xFF9E9E9E) // Abu-abu sedang (Teks mute)
//)


////OPSI 3: Deep Space Purple (AKTIF)
//private val DarkColorScheme = darkColorScheme(
//    background = BgDarkSpace,
//    surface = SurfaceDarkSpace,
//    onBackground = TextCreamSpace,
//    onSurface = TextCreamSpace,
//    primary = AietosGold,
//    onPrimary = BgDarkSpace,
//    outline = BorderDarkSpace,
//    onSurfaceVariant = TextMutedSpace,
//    secondary = AietosOrange
//)

private val LightColorScheme = lightColorScheme(
    background = BgLightCream,
    surface = SurfaceLightBox,
    onBackground = TextLightContrast,
    onSurface = TextLightContrast,
    primary = AietosGold,
    onPrimary = Color.White,
    outline = BorderLightSage,
    onSurfaceVariant = TextLightMuted,
    secondary = AietosOrange
)


//theme hijau awal
//private val DarkColorScheme = darkColorScheme(
//    background = BgDarkGreen,
//    surface = SurfaceGreen,
//    onBackground = TextCream,
//    onSurface = TextCream,
//    primary = AccentYellow,
//    onPrimary = BgDarkGreen,
//    outline = BorderGreen,
//    onSurfaceVariant = TextMuted
//)
//
//private val LightColorScheme = lightColorScheme(
//    background = BgLightGreen,
//    surface = SurfaceLight,
//    onBackground = TextDark,
//    onSurface = TextDark,
//    primary = AccentYellow,
//    onPrimary = TextDark,
//    outline = BorderLight,
//    onSurfaceVariant = TextMutedLight
//)

@Composable
fun NoExDateTheme(
    themeMode: String = "SYSTEM", // Terima parameter dari preferences
    content: @Composable () -> Unit
) {
    // 1. Cek apakah sistem saat ini sedang mode gelap
    val darkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme() // Ikuti Sistem
    }

    val colorScheme = when {
        // Jika Anda menggunakan Dynamic Color (Android 12+)
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
