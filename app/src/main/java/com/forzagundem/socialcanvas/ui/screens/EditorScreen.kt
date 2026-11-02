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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forzagundem.socialcanvas.data.AspectRatio
import com.forzagundem.socialcanvas.data.DesignExporter
import com.forzagundem.socialcanvas.data.EditorState
import com.forzagundem.socialcanvas.data.TemplateType
import com.forzagundem.socialcanvas.ui.components.*
import com.forzagundem.socialcanvas.ui.theme.Bg
import com.forzagundem.socialcanvas.ui.theme.Gold
import com.forzagundem.socialcanvas.ui.theme.TextPrimary
import com.forzagundem.socialcanvas.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(initialTemplate: TemplateType, onBack: () -> Unit) {
    var state by remember {
        mutableStateOf(EditorState(template = initialTemplate, headline = initialTemplate.defaultHeadline, subtitle = initialTemplate.defaultSub))
    }
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) state = state.copy(imageUri = uri.toString())
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
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Tasarımını paylaş"))
                }
                snackbar.showSnackbar(if (share) "Tasarım paylaşım için hazırlandı." else "PNG galeriye kaydedildi.")
            } catch (error: Exception) {
                snackbar.showSnackbar("Dışa aktarma başarısız: ${error.localizedMessage ?: "Bilinmeyen hata"}")
            } finally { exporting = false }
        }
    }

    Scaffold(
        containerColor = Bg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Column {
                    Text(state.template.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Tasarım düzenleyici", color = TextSecondary, fontSize = 12.sp)
                } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = TextPrimary) } },
                actions = {
                    IconButton(onClick = { imagePicker.launch("image/*") }) { Icon(Icons.Default.AddPhotoAlternate, "Fotoğraf ekle", tint = Gold) }
                    IconButton(onClick = { export(false) }, enabled = !exporting) { Icon(Icons.Default.Download, "PNG indir", tint = Gold) }
                    IconButton(onClick = { export(true) }, enabled = !exporting) { Icon(Icons.Default.Share, "Paylaş", tint = TextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            CanvasPreview(state, Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp))
            Column(Modifier.fillMaxWidth().background(Bg).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
                SectionLabel("Tuval oranı")
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AspectRatio.entries.forEach { ratio -> RatioChip(ratio.label, state.ratio == ratio) { state = state.copy(ratio = ratio) } }
                }
                Spacer(Modifier.height(16.dp))
                SectionLabel("Metin içeriği")
                AppTextField(state.headline, { state = state.copy(headline = it) }, "Ana başlık")
                Spacer(Modifier.height(10.dp))
                AppTextField(state.subtitle, { state = state.copy(subtitle = it) }, "Açıklama")
                Spacer(Modifier.height(16.dp))
                AppSlider(state.darken, { state = state.copy(darken = it) }, "Fotoğraf karartma", 0f..0.85f)
                Spacer(Modifier.height(12.dp))
                SectionLabel("Görünüm seçenekleri")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToolToggle("Odak vurgusu", state.focusPerson, { state = state.copy(focusPerson = !state.focusPerson) }, "◉")
                    ToolToggle("Metin üstte", state.textBehind, { state = state.copy(textBehind = !state.textBehind) }, "Aa")
                    ToolToggle("Güvenli alan", state.showSafeArea, { state = state.copy(showSafeArea = !state.showSafeArea) }, "▣")
                }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DarkOutlinedButton("Sıfırla", {
                        state = EditorState(template = state.template, headline = state.template.defaultHeadline, subtitle = state.template.defaultSub)
                        scope.launch { snackbar.showSnackbar("Tasarım başlangıç ayarlarına döndürüldü.") }
                    }, Modifier.weight(1f))
                    GoldButton(if (exporting) "Hazırlanıyor…" else "PNG olarak kaydet", { export(false) }, Modifier.weight(1.5f), enabled = !exporting, icon = Icons.Default.Download)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
