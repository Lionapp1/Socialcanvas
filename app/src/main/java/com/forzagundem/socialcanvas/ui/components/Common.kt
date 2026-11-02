package com.forzagundem.socialcanvas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forzagundem.socialcanvas.ui.theme.Border
import com.forzagundem.socialcanvas.ui.theme.Card
import com.forzagundem.socialcanvas.ui.theme.CardElevated
import com.forzagundem.socialcanvas.ui.theme.Gold
import com.forzagundem.socialcanvas.ui.theme.TextMuted
import com.forzagundem.socialcanvas.ui.theme.TextPrimary
import com.forzagundem.socialcanvas.ui.theme.TextSecondary

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = Color.Black,
            disabledContainerColor = Gold.copy(alpha = 0.35f),
            disabledContentColor = Color.Black.copy(alpha = 0.5f)
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun DarkOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    val keyboard = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
            imeAction = if (singleLine) ImeAction.Done else ImeAction.Default
        ),
        keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = Gold,
            unfocusedBorderColor = Border,
            cursorColor = Gold,
            focusedLabelColor = Gold,
            unfocusedLabelColor = TextMuted,
            focusedContainerColor = Card,
            unfocusedContainerColor = Card
        )
    )
}

@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondary, fontSize = 13.sp)
            Text(
                "${(value * 100).toInt()}%",
                color = Gold,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Gold,
                activeTrackColor = Gold,
                inactiveTrackColor = Border
            )
        )
    }
}

@Composable
fun RatioChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Gold,
            selectedLabelColor = Color.Black,
            containerColor = CardElevated,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Border,
            selectedBorderColor = Gold
        )
    )
}

@Composable
fun ToolToggle(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    emoji: String
) {
    val bg = if (active) Gold.copy(alpha = 0.18f) else CardElevated
    val borderColor = if (active) Gold else Border
    val textColor = if (active) Gold else TextSecondary

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
