package com.forzagundem.socialcanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Studio()
        }
    }
}

@Composable
fun Studio() {
    var ratio by remember { mutableStateOf("4:5 Instagram") }
    var dark by remember { mutableFloatStateOf(0.35f) }
    var title by remember { mutableStateOf("TRANSFER GELİŞMESİ") }

    val bg = Color(0xFF090A0E)
    val card = Color(0xFF151821)
    val gold = Color(0xFFFFB000)

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(18.dp)
        ) {
            Text(
                text = "SOCIAL CANVAS",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Mobil sosyal medya tasarım stüdyosu",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("4:5 Instagram", "1:1 Post", "9:16 Story", "Threads / X")) { item ->
                    FilterChip(
                        selected = ratio == item,
                        onClick = { ratio = item },
                        label = { Text(item) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(card, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text("FOTOĞRAF ALANI", color = Color.Gray)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = title,
                        color = gold,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Kişiyi öne çıkar • metni arkaya taşı",
                        color = Color.White
                    )
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = Color.Black.copy(alpha = dark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Önizleme: $ratio",
                        modifier = Modifier.padding(12.dp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Başlık") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = gold,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = gold,
                    focusedLabelColor = gold,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Text("Arka plan karartma", color = Color.White)
            Slider(
                value = dark,
                onValueChange = { dark = it },
                valueRange = 0f..0.8f
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { /* placeholder - focus person */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = gold)
                ) {
                    Text("Kişiyi Odakla", color = Color.Black)
                }
                Button(
                    onClick = { /* placeholder - export PNG */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("PNG Dışa Aktar")
                }
            }

            Text(
                text = "Şablonlar: Haber • Transfer • Skor • Alıntı • Threads/X",
                color = Color.Gray,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}
