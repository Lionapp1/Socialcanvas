package com.forzagundem.socialcanvas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forzagundem.socialcanvas.data.AspectRatio
import com.forzagundem.socialcanvas.data.EditorState
import com.forzagundem.socialcanvas.data.TemplateType
import com.forzagundem.socialcanvas.ui.components.AppSlider
import com.forzagundem.socialcanvas.ui.components.AppTextField
import com.forzagundem.socialcanvas.ui.components.CanvasPreview
import com.forzagundem.socialcanvas.ui.components.DarkOutlinedButton
import com.forzagundem.socialcanvas.ui.components.GoldButton
import com.forzagundem.socialcanvas.ui.components.RatioChip
import com.forzagundem.socialcanvas.ui.components.SectionLabel
import com.forzagundem.socialcanvas.ui.components.ToolToggle
import com.forzagundem.socialcanvas.ui.theme.Bg
import com.forzagundem.socialcanvas.ui.theme.Gold
import com.forzagundem.socialcanvas.ui.theme.TextPrimary
import com.forzagundem.socialcanvas.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    initialTemplate: TemplateType,
    onBack: () -> Unit
) {
    var state by remember {
        mutableStateOf(
            EditorState(
                template = initialTemplate,
                headline = initialTemplate.defaultHeadline,
                subtitle = initialTemplate.defaultSub
            )
        )
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Bg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            state.template.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "Düzenleyici",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            snackbar.showSnackbar("PNG dışa aktarma production sürümünde aktif olacak")
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "İndir", tint = Gold)
                    }
                    IconButton(onClick = {
                        scope.launch {
                            snackbar.showSnackbar("Paylaşım production sürümünde aktif olacak")
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Paylaş", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Bg,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CanvasPreview(
                state = state,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Bg)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                SectionLabel("Oran")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatio.entries.forEach { ratio ->
                        RatioChip(
                            label = ratio.label,
                            selected = state.ratio == ratio,
                            onClick = { state = state.copy(ratio = ratio) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                SectionLabel("Metin")
                AppTextField(
                    value = state.headline,
                    onValueChange = { state = state.copy(headline = it) },
                    label = "Başlık"
                )
                Spacer(Modifier.height(10.dp))
                AppTextField(
                    value = state.subtitle,
                    onValueChange = { state = state.copy(subtitle = it) },
                    label = "Alt metin"
                )

                Spacer(Modifier.height(16.dp))
                AppSlider(
                    value = state.darken,
                    onValueChange = { state = state.copy(darken = it) },
                    label = "Arka plan karartma",
                    valueRange = 0f..0.85f
                )

                Spacer(Modifier.height(12.dp))
                SectionLabel("Odak & katman")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ToolToggle(
                        label = "Kişiyi odakla",
                        active = state.focusPerson,
                        onClick = {
                            val next = !state.focusPerson
                            state = state.copy(focusPerson = next)
                            scope.launch {
                                snackbar.showSnackbar(
                                    if (next) "Kişi öne alındı" else "Kişi odağı kapatıldı"
                                )
                            }
                        },
                        emoji = "👤"
                    )
                    ToolToggle(
                        label = "Metin arkada",
                        active = state.textBehind,
                        onClick = {
                            state = state.copy(textBehind = !state.textBehind)
                        },
                        emoji = "📑"
                    )
                    ToolToggle(
                        label = "Güvenli alan",
                        active = state.showSafeArea,
                        onClick = {
                            state = state.copy(showSafeArea = !state.showSafeArea)
                        },
                        emoji = "⬚"
                    )
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DarkOutlinedButton(
                        text = "Sıfırla",
                        onClick = {
                            state = EditorState(
                                template = state.template,
                                headline = state.template.defaultHeadline,
                                subtitle = state.template.defaultSub
                            )
                            scope.launch { snackbar.showSnackbar("Ayarlar sıfırlandı") }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GoldButton(
                        text = "PNG Dışa Aktar",
                        onClick = {
                            scope.launch {
                                snackbar.showSnackbar("PNG export production modülünde eklenecek • UI hazır")
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        icon = Icons.Default.Download
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
