package com.forzagundem.socialcanvas.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forzagundem.socialcanvas.data.AspectRatio
import com.forzagundem.socialcanvas.data.CanvasFont
import com.forzagundem.socialcanvas.data.DesignExporter
import com.forzagundem.socialcanvas.data.EditorState
import com.forzagundem.socialcanvas.data.TemplateType
import com.forzagundem.socialcanvas.data.TextAlignMode
import com.forzagundem.socialcanvas.ui.components.*
import com.forzagundem.socialcanvas.ui.theme.Bg
import com.forzagundem.socialcanvas.ui.theme.Border
import com.forzagundem.socialcanvas.ui.theme.CardElevated
import com.forzagundem.socialcanvas.ui.theme.Gold
import com.forzagundem.socialcanvas.ui.theme.TextPrimary
import com.forzagundem.socialcanvas.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(initialTemplate: TemplateType, onBack: () -> Unit) {
    var state by remember {
        mutableStateOf(EditorState(
            template = initialTemplate,
            headline = initialTemplate.defaultHeadline,
            subtitle = initialTemplate.defaultSub
        ))
    }
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }

    fun keepUri(uri: android.net.Uri): String {
        runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        return uri.toString()
    }

    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) state = state.copy(imageUri = keepUri(uri))
    }
    val subjectPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) state = state.copy(subjectUri = keepUri(uri), subjectOnTop = true)
    }

    fun export(share: Boolean) {
        if (exporting) return
        scope.launch {
            exporting = true
            try {
                val uri = DesignExporter.save(context, state)
                if (share) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        clipData = android.content.ClipData.newUri(context.contentResolver, "SocialCanvas tasarımı", uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Tasarımını paylaş"))
                }
                snackbar.showSnackbar(if (share) "Tasarım paylaşım için hazırlandı." else "PNG galeriye kaydedildi.")
            } catch (error: Exception) {
                snackbar.showSnackbar("Dışa aktarma başarısız: ${error.localizedMessage ?: "Bilinmeyen hata"}")
            } finally {
                exporting = false
            }
        }
    }

    Scaffold(
        containerColor = Bg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.template.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Canlı tasarım önizlemesi", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { backgroundPicker.launch(arrayOf("image/*")) }) {
                        Icon(Icons.Default.AddPhotoAlternate, "Arka plan fotoğrafı", tint = Gold)
                    }
                    IconButton(onClick = { subjectPicker.launch(arrayOf("image/png")) }) {
                        Icon(Icons.Default.Person, "Şeffaf PNG kişi ekle", tint = Gold)
                    }
                    IconButton(onClick = { export(false) }, enabled = !exporting) {
                        Icon(Icons.Default.Download, "PNG kaydet", tint = Gold)
                    }
                    IconButton(onClick = { export(true) }, enabled = !exporting) {
                        Icon(Icons.Default.Share, "Paylaş", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            CanvasPreview(state, Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) {
                state = it
            }
            Column(
                Modifier.fillMaxWidth().background(Bg).verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                SectionLabel("Platform önizlemesi")
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AspectRatio.entries.forEach { ratio ->
                        RatioChip(ratio.label, state.ratio == ratio) { state = state.copy(ratio = ratio) }
                    }
                }
                Text(state.ratio.platform, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))

                Spacer(Modifier.height(18.dp))
                SectionLabel("Arka plan ve fotoğraf")
                ActionRow("Arka plan fotoğrafı seç", "Fotoğraf galerisi") { backgroundPicker.launch(arrayOf("image/*")) }
                if (state.imageUri != null) {
                    EditorSlider("Fotoğraf yakınlaştırma", state.imageScale, 0.7f..2.5f) { state = state.copy(imageScale = it) }
                    EditorSlider("Fotoğraf yatay konum", state.imageX, -0.5f..0.5f) { state = state.copy(imageX = it) }
                    EditorSlider("Fotoğraf dikey konum", state.imageY, -0.5f..0.5f) { state = state.copy(imageY = it) }
                    DarkOutlinedButton("Fotoğrafı kaldır", { state = state.copy(imageUri = null, imageX = 0f, imageY = 0f, imageScale = 1f) }, Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(18.dp))
                SectionLabel("Kişi katmanı · şeffaf PNG")
                ActionRow("Ön plan kişisi ekle", "Arka planı kaldırılmış PNG") { subjectPicker.launch(arrayOf("image/png")) }
                if (state.subjectUri != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        RatioChip("Yazının önünde", state.subjectOnTop) { state = state.copy(subjectOnTop = true) }
                        RatioChip("Yazının arkasında", !state.subjectOnTop) { state = state.copy(subjectOnTop = false) }
                    }
                    EditorSlider("Kişi boyutu", state.subjectScale, 0.35f..2.2f) { state = state.copy(subjectScale = it) }
                    EditorSlider("Kişi yatay konum", state.subjectX, -0.8f..0.8f) { state = state.copy(subjectX = it) }
                    EditorSlider("Kişi dikey konum", state.subjectY, -0.8f..0.8f) { state = state.copy(subjectY = it) }
                    DarkOutlinedButton("Kişi katmanını kaldır", { state = state.copy(subjectUri = null, subjectX = 0f, subjectY = 0.08f, subjectScale = 1f) }, Modifier.fillMaxWidth())
                    Text("Not: Kişinin yazının arkasında görünmesi için arka planı şeffaf PNG kullan. Fotoğraftan otomatik kişi ayırma ayrıca eklenecek.", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                }

                Spacer(Modifier.height(18.dp))
                SectionLabel("Metin içeriği")
                AppTextField(state.headline, { state = state.copy(headline = it) }, "Ana başlık", singleLine = false)
                Spacer(Modifier.height(10.dp))
                AppTextField(state.subtitle, { state = state.copy(subtitle = it) }, "Alt başlık", singleLine = false)

                Spacer(Modifier.height(18.dp))
                SectionLabel("Yazı tipi")
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CanvasFont.entries.forEach { font ->
                        RatioChip(font.label, state.font == font) { state = state.copy(font = font) }
                    }
                }
                EditorSlider("Başlık boyutu", state.headlineSize, 18f..72f) { state = state.copy(headlineSize = it) }
                EditorSlider("Alt yazı boyutu", state.subtitleSize, 10f..36f) { state = state.copy(subtitleSize = it) }

                Spacer(Modifier.height(12.dp))
                SectionLabel("Yazı rengi")
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorChoice("Altın", 0xFFFFB700, state.headlineColor == 0xFFFFB700L) { state = state.copy(headlineColor = 0xFFFFB700) }
                    ColorChoice("Beyaz", 0xFFFFFFFF, state.headlineColor == 0xFFFFFFFFL) { state = state.copy(headlineColor = 0xFFFFFFFF) }
                    ColorChoice("Kırmızı", 0xFFFF4242, state.headlineColor == 0xFFFF4242L) { state = state.copy(headlineColor = 0xFFFF4242) }
                    ColorChoice("Mavi", 0xFF65B7FF, state.headlineColor == 0xFF65B7FFL) { state = state.copy(headlineColor = 0xFF65B7FF) }
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorChoice("Altın", 0xFFFFB700, state.subtitleColor == 0xFFFFB700L) { state = state.copy(subtitleColor = 0xFFFFB700) }
                    ColorChoice("Beyaz", 0xFFFFFFFF, state.subtitleColor == 0xFFFFFFFFL) { state = state.copy(subtitleColor = 0xFFFFFFFF) }
                    ColorChoice("Gri", 0xFFCCCCCC, state.subtitleColor == 0xFFCCCCCCL) { state = state.copy(subtitleColor = 0xFFCCCCCC) }
                }

                Spacer(Modifier.height(12.dp))
                SectionLabel("Hizalama ve konum")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextAlignMode.entries.forEach { mode ->
                        RatioChip(mode.label, state.textAlign == mode) { state = state.copy(textAlign = mode) }
                    }
                }
                EditorSlider("Metin yatay konum", state.textX, -0.45f..0.45f) { state = state.copy(textX = it) }
                EditorSlider("Metin dikey konum", state.textY, -0.45f..0.45f) { state = state.copy(textY = it) }

                Spacer(Modifier.height(16.dp))
                SectionLabel("Görünüm")
                EditorSlider("Fotoğraf karartma", state.darken, 0f..0.85f) { state = state.copy(darken = it) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RatioChip("Güvenli alan", state.showSafeArea) { state = state.copy(showSafeArea = !state.showSafeArea) }
                    RatioChip("Metin gölgesi", state.textShadow) { state = state.copy(textShadow = !state.textShadow) }
                }

                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DarkOutlinedButton("Sıfırla", {
                        state = EditorState(template = state.template, headline = state.template.defaultHeadline, subtitle = state.template.defaultSub)
                        scope.launch { snackbar.showSnackbar("Tasarım başlangıç ayarlarına döndürüldü.") }
                    }, Modifier.weight(1f))
                    GoldButton(if (exporting) "Hazırlanıyor…" else "PNG olarak kaydet", { export(false) }, Modifier.weight(1.5f), enabled = !exporting, icon = Icons.Default.Download)
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun EditorSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(String.format("%.2f", value), color = Gold, fontSize = 11.sp)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold, inactiveTrackColor = Border)
        )
    }
}

@Composable
private fun ActionRow(title: String, detail: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(CardElevated, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(detail, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
        GoldButton("Seç", onClick, Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}

@Composable
private fun ColorChoice(label: String, color: Long, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Gold.copy(alpha = 0.20f),
            selectedLabelColor = Gold,
            containerColor = CardElevated,
            labelColor = TextSecondary
        )
    )
}
