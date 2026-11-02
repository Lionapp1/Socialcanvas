# SocialCanvas Android

Profesyonel sosyal medya görsel editörü — Android / Jetpack Compose.

## Uygulama özellikleri

### Ana ekran
- 6 şablon kartı: Haber, Transfer, Skor, Alıntı, Threads/X, Boş Tuval
- Temiz, koyu tema + altın vurgu

### Düzenleyici
- **Canlı tuval önizlemesi** — seçilen orana göre anlık boyut değişimi
- Oranlar: 4:5 Instagram · 1:1 Post · 9:16 Story · Threads/X
- Başlık ve alt metin düzenleme
- Arka plan karartma kaydırıcısı (animasyonlu)
- **Kişiyi odakla** — kişi alanı öne çıkar
- **Metin arkada** — metin katmanı arkaya alınır
- Güvenli alan göstergesi
- Sıfırla + PNG dışa aktar (UI hazır, export production modülü)
- Snackbar geri bildirimleri

### Teknik
- Jetpack Compose + Material 3
- Navigation Compose
- Koyu tema, tutarlı tipografi ve spacing
- GitHub Actions → debug APK artifact

## APK almak
1. **Actions** → **Android APK** workflow
2. Bitince artifact: `SocialCanvas-debug-apk`

## Yerel
```bash
gradle assembleDebug
# veya
./gradlew assembleDebug
```
