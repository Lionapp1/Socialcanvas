# SocialCanvas Android

Profesyonel sosyal medya görsel editörü için Android / Jetpack Compose başlangıç projesi.

## Özellikler (UI)
- Instagram 4:5, kare (1:1), Story 9:16, Threads/X oran seçimleri
- Başlık editörü
- Arka plan karartma kontrolü
- Kişiyi odaklama / metni kişi arkasına alma için UI akışı
- Haber, transfer, skor ve alıntı şablon alanları
- GitHub Actions ile otomatik debug APK üretimi + artifact

## Önemli not
Bu paket çalışan editör arayüzü / proje iskeletidir.  
Kişi segmentasyonu, gerçek katman kompoziti ve bitmap PNG export motoru sonraki production modülleridir; butonlar bu sürümde UI placeholder'dır.

## APK almak
1. Bu repo'ya push edin (veya Actions sekmesinden **Run workflow**).
2. **Actions** → **Android APK** workflow'unun bitmesini bekleyin.
3. Artifact olarak `SocialCanvas-debug-apk` indirin.

## Yerel build
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```
