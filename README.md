# 🍎 Notism - Apple Notes AI (Android)

<p align="center">
  <img src="https://img.shields.io/badge/Version-v2.3.0-FFB300?style=for-the-badge&logo=android&logoColor=black" alt="Version" />
  <img src="https://img.shields.io/badge/Platform-Android_8.0+_(API_26--35)-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Design-Apple_Cupertino_(iOS_18)-007AFF?style=for-the-badge&logo=apple&logoColor=white" alt="Design" />
  <img src="https://img.shields.io/badge/Motion-Emil_Kowalski_Springs-FF2D55?style=for-the-badge" alt="Motion" />
  <img src="https://img.shields.io/badge/Security-AES--256_Keystore-34C759?style=for-the-badge&logo=private-internet-access&logoColor=white" alt="Security" />
  <img src="https://img.shields.io/badge/Architecture-Clean_+_Local--First-purple?style=for-the-badge" alt="Architecture" />
</p>

**Notism**, Apple iOS 18 ve macOS Sequoia tasarım standartlarını (*Cupertino Design Language*) ve Emil Kowalski fizik tabanlı akıcı mikro animasyonlarını Android platformuna taşıyan; sunucusuz (**BYOK - Bring Your Own Key**), gizlilik odaklı (**Local-First**) ve yapay zeka destekli yeni nesil not alma uygulamasıdır.

Notlarınız yalnızca cihazınızda yerel SQLite/Room veritabanında saklanır. Kendi API anahtarınızla dilediğiniz yapay zeka modelini bağlayabilir veya internetsiz yerel cihaz içi modelleri kullanabilirsiniz.

---

## 📑 İçindekiler

- [✨ Öne Çıkan Özellikler](#-öne-çıkan-özellikler)
  - [1. Apple Cupertino Arayüzü & Akıcı Animasyonlar](#1-apple-cupertino-arayüzü--akıcı-animasyonlar)
  - [2. 5 Farklı Not Görüntüleme Modu](#2-5-farklı-not-görüntüleme-modu)
  - [3. Gelişmiş Not Düzenleyici](#3-gelişmiş-not-düzenleyici)
  - [4. Yerleşik & Özel Şablon Kütüphanesi](#4-yerleşik--özel-şablon-kütüphanesi)
  - [5. Defter Kağıdı Dokuları & Çizim Tuvali](#5-defter-kağıdı-dokuları--çizim-tuvali)
  - [6. Sesli Notlar & Canlı Dalga Görselleştiricisi](#6-sesli-notlar--canlı-dalga-görselleştiricisi)
  - [7. Çok Modelli Yapay Zeka Motoru (7 Sağlayıcı & AI Hub)](#7-çok-modelli-yapay-zeka-motoru-7-sağlayıcı--ai-hub)
  - [8. Evrensel Komut Paleti (Raycast / Spotlight Stili)](#8-evrensel-komut-paleti-raycast--spotlight-stili)
  - [9. Hatırlatıcılar & Akıllı Bildirimler](#9-hatırlatıcılar--akıllı-bildirimler)
  - [10. Android Sistem Entegrasyonları](#10-android-sistem-entegrasyonları)
  - [11. Dışa Aktarma & Sosyal Medya Kartları](#11-dışa-aktarma--sosyal-medya-kartları)
  - [12. Donanımsal Güvenlik & Biyometrik Kilit](#12-donanımsal-güvenlik--biyometrik-kilit)
  - [13. Bulut Senkronizasyonu & Yedekleme](#13-bulut-senkronizasyonu--yedekleme)
  - [14. Otomatik Uygulama İçi Güncelleyici](#14-otomatik-uygulama-içi-güncelleyici)
- [🛠️ Teknolojiler ve Mimari](#️-teknolojiler-ve-mimari)
- [🚀 Kurulum ve Derleme](#-kurulum-ve-derleme)
- [🔐 BYOK (Kendi Anahtarını Getir) Kurulumu](#-byok-kendi-anahtarını-getir-kurulumu)
- [📄 Lisans](#-lisans)

---

## ✨ Öne Çıkan Özellikler

### 1. Apple Cupertino Arayüzü & Akıcı Animasyonlar
- **Buzlu Cam (Frosted Glass):** Android 12+ donanım seviyesinde dinamik render-node arka plan bulanıklığı (`.blur(20.dp)`).
- **Emil Kowalski Fizik Tabanlı Yay Animasyonları:**
  - `bouncyClickable`: Tüm butonlarda, kartlarda ve etkileşimli elemanlarda basış anında hissedilen organik yay tepkisi (`Spring.DampingRatioMediumBouncy`) ve Apple dokunsal haptikleri.
  - `AppleSegmentedControl`: Fiziksel olarak kayan gölgeli gösterge kapsülü.
  - `SonnerFloatingToast`: Standart Android Toast'ları yerine ekranın üstünden zarifçe süzülen bildirim hapı.
- **Dinamik Küçülen Başlık (Scroll-Aware Header):** Liste aşağı kaydırıldığında alanın %90'ından fazlasını notlara bırakacak şekilde pürüzsüzce küçülen iOS başlık barı.
- **6 Apple Vurgu Rengi:** Altın Sarısı, iOS Mavisi, Nane Yeşili, Mor, Turuncu ve Apple Kırmızısı.
- **3 Tipografi Ailesi:** System (SF Pro), Serif (Kitap & Edebiyat) ve Monospace (Kod & Daktilo).

---

### 2. 5 Farklı Not Görüntüleme Modu
1. **📋 Detaylı Liste / Grid Görünümü:** Kapak görselleri, etiketler, önizleme ve meta bilgiler.
2. **📱 Kompakt Liste:** Dikey boşlukları azaltan, rozetleri gizleyen ve tek ekranda maksimum not gösteren minimalist mod.
3. **📊 Kanban Pano Görünümü (`KanbanBoardView`):** Notları `Yapılacaklar (TODO)`, `Devam Edenler (IN_PROGRESS)` ve `Tamamlandı (DONE)` sütunlarına ayıran interaktif pano.
4. **📅 Takvim Görünümü (`CalendarView`):** Notları aylık interaktif takvim üzerinde gün bazında filtreleme ve seçilen güne doğrudan not ekleme.
5. **🕸️ 2D Bilgi Ağı Grafiği (`GraphViewDialog`):** Obsidian tarzı 2D fizik motoruyla notlar arasındaki `[[Wiki-Link]]` bağlantılarını görselleştiren interaktif bilgi ağı.

> **Hızlı Eylemler:**
> - **Kart Üzerinde Doğrudan Görev Tikleme:** Notun içine girmeden karttaki `- [ ]` kutucuklarına dokunarak görevleri tamamlama.
> - **Akıllı Web Alan Adı Rozetleri:** Not içerisindeki bağlantılardan otomatik alan adı rozeti (`🔗 github.com`, `🔗 medium.com`).
> - **Akıllı Sıralama:** Son Güncellenen, Oluşturulma Tarihi, Başlık (A-Z) ve Öncelik Sıralaması (Sabitlenen notlar her zaman en üstte kalır).
> - **Peek & Pop Menüsü (Uzun Basış):** Notu açmadan anında PDF veya Sosyal Medya Görsel Kartı (PNG) olarak dışa aktarma, çoğaltma, kilitleme veya klasörleme.

---

### 3. Gelişmiş Not Düzenleyici
- **Çoklu Not Sekmeleri:** Son açılan notlar arasında masaüstü tarayıcı rahatlığında tek tıkla geçiş.
- **Notion Tarzı Sayfa Özellikleri:** Öncelik (Düşük/Orta/Yüksek/Acil), Durum, İlerleme Çubuğu (%0-%100 veya görev listesinden otomatik hesaplama).
- **Canlı WYSIWYG Markdown Biçimlendirme:** Başlıklar, kalın, italik, alıntı ve kod blokları yazılırken editörde anında görselleşir.
- **📂 Katlanabilir Başlıklar:** `# H1`, `## H2`, `### H3` başlıklarını akordeon şeklinde katlayıp açarak uzun belgelerde odaklanma (`CollapsibleMarkdownRenderer`).
- **✨ Görsel Matematik & Akış Şemaları:** KaTeX formülleri (`$E=mc^2$`) ve Mermaid diagram önizlemesi.
- **İnteraktif Tablo Düzenleyici:** Kolayca satır ve sütun eklenebilen görsel tablo editörü.
- **Zen Daktilo Modu:** Tüm dikkat dağıtıcıları gizleyen tam ekran minimalist yazım deneyimi.
- **Pomodoro Sayacı:** Not alırken 25 dk çalışma / 5 dk mola odaklanma zamanlayıcısı.
- **Sürüm Geçmişi (Zaman Makinesi):** Geçmiş anlık kopyaları inceleme ve tek dokunuşla önceki sürüme dönebilme.
- **İki Yönlü Not Bağlantıları (Wiki-Links):** `[[` yazılarak diğer notlara hızlı köprü oluşturma.
- **Slash Komutları Menüsü:** `/` yazıldığında açılan Apple stili hızlı blok ekleme menüsü.

---

### 4. Yerleşik & Özel Şablon Kütüphanesi
- **Haftalık Planlayıcı & Alışkanlık Takipçisi:** 7 günlük su, kitap, egzersiz matrisi ve günlük akışlar.
- **Toplantı Notları & Aksiyon Planı:** Katılımcılar, gündem ve görev dağılım tablosu.
- **Cornell Not Alma Tekniği:** İpucu sütunu, ders notları ve sentez özeti.
- **Kitap & Medya İncelemesi:** Puanlama, ana çıkarımlar ve aksiyon planı.
- **Aylık Bütçe & Harcama:** Gelir, gider ve yatırım takibi.
- **Proje & Sprint Takipçisi:** Backlog, In-Progress, Done ve risk analizi.
- **Kullanıcıya Özel Şablonlar:** Herhangi bir notu tek tıkla şablon olarak kaydetme ve dolu notlara dahi sonradan şablon ekleyebilme.

---

### 5. Defter Kağıdı Dokuları & Çizim Tuvali
- **Canvas Kağıt Dokuları:**
  - 📄 Düz Sayfa (Blank)
  - 📏 Çizgili Defter (Lined)
  - 📐 Kareli Defter (Grid)
  - 🔘 Noktalı Defter (Dot Grid)
  - 📜 Sıcak Parşömen (Sepia)
- **Apple Tarzı Çizim Tuvali:** Fırça kalınlığı, silgi, renk paleti, geri al/ileri al ve çizimleri şeffaf PNG olarak doğrudan nota gömme.

---

### 6. Sesli Notlar & Canlı Dalga Görselleştiricisi
- **Canlı iOS Ses Dalgası:** Mikrofon genliğine (`maxAmplitude`) duyarlı, kayıt esnasında ritmik dans eden ses dalgaları.
- **Arka Planda Yüksek Kaliteli Kayıt:** `.m4a` formatında kesintisiz kayıt.
- **Dahili Oynatıcı:** Uygulama dışına çıkmadan dalga boyu ve süre kontrolüyle not içi dinleme.
- **Widget ile Tek Dokunuşla Kayıt:** Ana ekran widget'ındaki mikrofon butonuyla anında kayda başlama.

---

### 7. Çok Modelli Yapay Zeka Motoru (7 Sağlayıcı & AI Hub)

Notism, tek bir modele bağımlı değildir. Kendi API anahtarınızı girerek en popüler yapay zeka modellerini kullanabilirsiniz:

1. ⚡ **Google Gemini API:** `gemini-2.5-flash`, `gemini-1.5-pro`
2. 🧠 **OpenAI API:** `gpt-4o`, `gpt-4o-mini`
3. 🖋️ **Anthropic Claude API:** `claude-3-5-sonnet-20241022`, `claude-3-haiku`
4. 🚀 **Groq API:** `llama-3.3-70b-versatile` (milisaniyeler içinde ultra hızlı yanıt)
5. 🌐 **OpenRouter API:** DeepSeek, Llama, Mistral ve yüzlerce model
6. 🏢 **Google Vertex AI:** Kurumsal bulut ve özel proje desteği
7. 📴 **Cihaz İçi Yerel LLM (MediaPipe Gemma):** İnternetsiz, %100 yerel ve gizli çalışan cihaz içi yapay zeka

> **Yapay Zeka Merkezi (AI Hub) Yetenekleri:**
> - ☀️ **Sabah Özeti (Morning Digest):** Güne başlarken tüm notları tarayarak önemli görevleri özetleme.
> - 🧠 **Çoklu Not Sentezi (Multi-Note Synthesis):** Notlar arasındaki gizli bağlantıları ve temaları ortaya çıkarma.
> - 🗂️ **Hafıza Kartları Oluşturucu (AI Flashcards):** Notlardan anında sınav/çalışma kartları üretme.
> - 💬 **Notla ve Genel Not Defteriyle Sohbet:** Not içeriği bağlamında sorularınızı yanıtlama.
> - ✍️ **Akıllı Asistan:** Özet çıkarma, dilbilgisi düzeltme, üslup dönüştürme, görev çıkarma, otomatik başlık/etiket ve çeviri.

---

### 8. Evrensel Komut Paleti (Raycast / Spotlight Stili)
- Ekranın herhangi bir yerinden açılabilen güçlü komut satırı (`CommandPaletteBottomSheet`):
  - Notlar arasında anlık tam metin arama ve nota atlama
  - Yeni not veya günlük not (Daily Note) oluşturma
  - Bulut senkronizasyonunu tetikleme
  - Çöp kutusuna, ayarlara veya AI Hub'a anında geçiş

---

### 9. Hatırlatıcılar & Akıllı Bildirimler
- **Exact Alarms:** `AlarmManager` ile takvimden tarih ve saat seçilerek kurulan dakik bildirimler.
- **Cihaz Açılış Koruması (`BOOT_COMPLETED`):** Telefon yeniden başlatıldığında alarmlar otomatik olarak hafızadan geri yüklenir.
- **Eylemli Bildirimler:** Bildirimden ayrılmadan *⏰ 15 Dk Ertele* veya *✓ Tamamlandı* olarak işaretleme.

---

### 10. Android Sistem Entegrasyonları
- **Android 14+ Hızlı Ayarlar Kutucuğu (Quick Settings Tile):** Bildirim perdesine eklenebilen *"Notism Hızlı Not"* butonuyla anında not alma.
- **Masaüstü Widget:** Son notlar, yeni not oluşturma ve tek tıkla ses kaydı.
- **Akıllı Web Kırpıcı (Web Clipper):** Tarayıcıdan "Paylaş" ile gelen bağlantılardan otomatik sayfa başlığı, özet ve **3 maddelik AI özeti** çıkarma.

---

### 11. Dışa Aktarma & Sosyal Medya Kartları
- **📄 Profesyonel PDF Dışa Aktarma:** Standart A4 formatında, tipografik başlık ve tarihlerle temiz PDF çıktısı.
- **🖼️ Estetik Sosyal Medya Kartı (1080x1350 PNG):** Instagram, X ve LinkedIn için yuvarlatılmış köşeli, gölgeli ve filigranlı hazır paylaşım görseli.
- **📦 Tam ZIP Yedekleme:** SQLite veritabanı, medya ve ekleri tek arşivde paketleme.
- **📝 Markdown (.md) ve Düz Metin (.txt) Desteği.**

---

### 12. Donanımsal Güvenlik & Biyometrik Kilit
- **Android Keystore Donanımsal Şifreleme:** Tüm API anahtarları, şifreler ve kimlik bilgileri **AES-256 GCM** ve **AES-256 SIV** algoritmalarıyla donanım çipinde şifrelenir (`EncryptedSharedPreferences`).
- **Biyometrik Kilit:** Uygulama genelinde veya not bazında Parmak İzi, Yüz Tanıma ve PIN koruması.
- **Uygulama Değiştirici Gizliliği (App Switcher Privacy):** Çoklu görev menüsünde notların görünmesini ve ekran görüntüsü alınmasını engelleyen gizlilik modu.

---

### 13. Bulut Senkronizasyonu & Yedekleme
- **Kişisel Bulut (WebDAV / Nextcloud):** Üçüncü taraf sunuculara bağımlı kalmadan kendi Nextcloud, ownCloud veya WebDAV sunucunuzla şifreli iki yönlü senkronizasyon.
- **Cihaza & Google Drive'a Yedekleme (SAF ZIP):** Android Depolama Erişim Çerçevesi (SAF) üzerinden Google Drive klasörüne veya yerel depolamaya tek dokunuşla tam ZIP yedeği alma ve geri yükleme.
- **Otomatik Arka Plan Yedekleme:** Cihaz boştayken günlük veya haftalık sessiz yedekleme planı.

---

### 14. Otomatik Uygulama İçi Güncelleyici
- Uygulama açılışında veya ayarlar menüsünden GitHub Releases API'si (`aturkk/notlarim-app`) taranır.
- Yeni bir sürüm çıktığında değişiklik notları (Changelog) gösterilir ve APK doğrudan uygulama içinden indirilip kurulur.

---

## 🛠️ Teknolojiler ve Mimari

- **Dil:** Kotlin 100%
- **Arayüz Framework'ü:** Jetpack Compose (Material 3 + Apple Cupertino Bileşenleri)
- **Mimari:** Clean Architecture (Domain, Data, Presentation) + MVVM + MVI prensipleri
- **Veritabanı:** Room Database (SQLite) + WAL (Write-Ahead Logging) + Foreign Keys
- **Bağımlılık Yönetimi (DI):** Dagger Hilt
- **Asenkron Yapı:** Kotlin Coroutines & StateFlow / SharedFlow
- **Ağ & İletişim:** OkHttp3 + Retrofit + Moshi (JSON Serialization)
- **Güvenlik:** AndroidX Security Crypto (`EncryptedSharedPreferences` + Android Keystore)
- **Görsel & Medya:** Coil (Görsel Yükleme), Android MediaRecorder & MediaPlayer (Ses)
- **Animasyon:** Compose Animation (`spring`, `fadeIn`, `shrinkVertically`, `GraphicsLayer`)

---

## 🚀 Kurulum ve Derleme

### Ön Gereksinimler
- Android Studio Ladybug (2024.2.1+) veya daha yeni bir sürüm
- JDK 17
- Android SDK (minSdk: 26, targetSdk: 35)

### Projeyi Klonlama ve Derleme

```bash
# Depoyu klonlayın
git clone https://github.com/aturkk/notlarim-app.git

# Proje dizinine geçin
cd notlarim-app

# Debug APK derleyin
./gradlew assembleDebug

# İmzalı Release APK derleyin
./gradlew assembleRelease
```

Derlenen APK dosyaları `app/build/outputs/apk/release/` dizininde üretilecektir.

---

## 🔐 BYOK (Kendi Anahtarını Getir) Kurulumu

Notism, üçüncü taraf bir sunucuya ihtiyaç duymadan doğrudan resmi yapay zeka sağlayıcılarının API uç noktalarıyla konuşur. Tercih ettiğiniz servisin API anahtarını alıp **Ayarlar -> Yapay Zeka (AI) Ayarları** bölümüne girmeniz yeterlidir:

- **Google Gemini:** [Google AI Studio](https://aistudio.google.com/) üzerinden ücretsiz API anahtarı temin edebilirsiniz.
- **Groq:** [Groq Console](https://console.groq.com/) üzerinden ultra hızlı Llama modelleri için ücretsiz anahtar alabilirsiniz.
- **OpenAI:** [OpenAI Platform](https://platform.openai.com/) üzerinden API anahtarı alabilirsiniz.
- **Anthropic:** [Anthropic Console](https://console.anthropic.com/) üzerinden Claude anahtarı alabilirsiniz.
- **OpenRouter:** [OpenRouter.ai](https://openrouter.ai/) üzerinden tek anahtarla DeepSeek, Mistral ve yüzlerce modele erişebilirsiniz.

---

## 📄 Lisans

Bu proje kişisel ve açık kaynaklı kullanım için geliştirilmiştir. Detaylar için [LICENSE](LICENSE) dosyasına göz atabilirsiniz.
