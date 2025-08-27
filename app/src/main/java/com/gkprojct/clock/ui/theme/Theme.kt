package com.gkprojct.clock.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Placeholder: customize or expand based on screen sizes (Compact, Medium, Expanded, etc.)
data class WindowSizeClass(val width: Int, val height: Int)

val LocalWindowSize = staticCompositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

@SuppressLint("ContextCast")
@Composable
fun ClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = Color.Cyan,
            onPrimary = Color.Black,
            background = Color.Black,
            onBackground = Color.White
        )

        else -> lightColorScheme(
            primary = Color.Blue,
            onPrimary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            secondary = Color.Gray
        )
    }

    val activity = context as? ComponentActivity
    val windowSize = activity?.let { calculateWindowSizeClass(it) } ?: WindowSizeClass(0, 0)

    ProvideWindowInsets {
        CompositionLocalProvider(
            LocalWindowSize provides windowSize
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography,
                content = content
            )
        }
    }
}

@Composable
fun ProvideWindowInsets(content: @Composable () -> Unit) {
    // If you’re using Accompanist Insets, you can add real insets logic here
    content()
}

// Temporary stub — replace with real logic based on device screen size or WindowMetrics
@Suppress("DEPRECATION")
fun calculateWindowSizeClass(activity: ComponentActivity): WindowSizeClass {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = activity.windowManager.currentWindowMetrics
        val bounds = metrics.bounds
        WindowSizeClass(bounds.width(), bounds.height())
    } else {
        val displayMetrics = android.util.DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        WindowSizeClass(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
}