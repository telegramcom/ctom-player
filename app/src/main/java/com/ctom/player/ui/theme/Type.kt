package com.ctom.player.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val CtomTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold),
        headlineMedium = headlineMedium.copy(fontSize = 25.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
        titleMedium = titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        bodyLarge = bodyLarge.copy(fontSize = 15.sp, lineHeight = 21.sp),
        bodyMedium = bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
        labelSmall = labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Medium),
    )
}