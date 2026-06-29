package com.p2ptaskmanager.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.p2ptaskmanager.android.R.array.com_google_android_gms_fonts_certs
)

private val LiterataFont = GoogleFont("Literata")
private val LatoFont = GoogleFont("Lato")

@Composable
actual fun bujoFontFamily(): FontFamily = FontFamily(
    Font(googleFont = LiterataFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = LiterataFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = LiterataFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = LatoFont, fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Italic)
)
