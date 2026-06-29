package com.p2ptaskmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.p2ptaskmanager.ui.theme.LabelColors

@Composable
fun ColorLabelPicker(
    selected: Int?,
    onSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        LabelColors.forEachIndexed { index, color ->
            val isSelected = selected == index
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = Color.White,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .clickable { onSelected(if (isSelected) null else index) }
            )
            Spacer(Modifier.width(6.dp))
        }
    }
}

@Composable
fun ColorLabelStripe(colorLabel: Int?, modifier: Modifier = Modifier) {
    if (colorLabel != null && colorLabel in LabelColors.indices) {
        Box(
            modifier = modifier
                .size(width = 3.dp, height = 32.dp)
                .background(LabelColors[colorLabel])
        )
    }
}
