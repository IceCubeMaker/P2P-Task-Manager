package com.p2ptaskmanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.p2ptaskmanager.ui.theme.GoldAccent

@Composable
fun ImportanceStars(
    importance: Float,
    onChanged: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
    maxStars: Int = 5
) {
    val filledStars = (importance * maxStars).toInt().coerceIn(0, maxStars)
    Row(modifier = modifier) {
        repeat(maxStars) { i ->
            val filled = i < filledStars
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "${i + 1} star",
                tint = if (filled) GoldAccent else androidx.compose.ui.graphics.Color(0xFFBBBBBB),
                modifier = Modifier
                    .size(16.dp)
                    .then(if (onChanged != null) Modifier.clickable {
                        onChanged((i + 1).toFloat() / maxStars)
                    } else Modifier)
            )
        }
    }
}
