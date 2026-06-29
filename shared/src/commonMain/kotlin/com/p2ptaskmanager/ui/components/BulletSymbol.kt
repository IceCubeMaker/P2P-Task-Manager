package com.p2ptaskmanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2ptaskmanager.data.model.BujoState

/** BuJo-style bullet symbol that cycles through states on tap. */
@Composable
fun BulletSymbol(
    state: BujoState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = state.symbol(),
            color = if (state == BujoState.COMPLETED || state == BujoState.MIGRATED || state == BujoState.SCHEDULED)
                color.copy(alpha = 0.6f) else color,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default
        )
    }
}

fun BujoState.symbol(): String = when (this) {
    BujoState.OPEN -> "•"
    BujoState.COMPLETED -> "×"
    BujoState.MIGRATED -> ">"
    BujoState.SCHEDULED -> "<"
    BujoState.NOTE -> "—"
    BujoState.EVENT -> "○"
}

fun BujoState.next(): BujoState = when (this) {
    BujoState.OPEN -> BujoState.COMPLETED
    BujoState.COMPLETED -> BujoState.MIGRATED
    BujoState.MIGRATED -> BujoState.OPEN
    BujoState.NOTE -> BujoState.NOTE
    BujoState.EVENT -> BujoState.COMPLETED
    BujoState.SCHEDULED -> BujoState.OPEN
}
