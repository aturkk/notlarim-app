# Notism (Notlar Uygulaması) - Kapsamlı Özellik & Mimari Kataloğu
> **Sürüm:** v2.3.0 (versionCode: 25)  
> **Paket Adı:** `com.applenotes.ai`  
> **Hedef Platform:** Android (minSdk 26, targetSdk 35)  
> **Tasarım Dili:** Apple iOS 18 / macOS Sequoia Cupertino Design Language  
> **Mimari:** Clean Architecture + MVVM + Jetpack Compose + Room Local-First  

Bu belge, **Notism** uygulamasında mevcut olan tüm modülleri, ekranları, kullanıcı arayüzü (UI/UX) bileşenlerini, sistem entegrasyonlarını ve yapay zeka özelliklerini detaylı bir şekilde listeler. Gelecekteki geliştirmelerde mevcut özellikleri korumak ve mükerrer geliştirme yapmamak için ana başvuru kaynağıdır.

---

## 📑 İÇİNDEKİLER
1. [Ana Not Listesi & Navigasyon (NotesListScreen)](#1-ana-not-listesi--navigasyon)
2. [Gelişmiş Not Düzenleyici (NoteEditorScreen)](#2-gelişmiş-not-düzenleyici)
3. [Defter Kağıdı Dokuları & Çizim](#3-defter-kağıdı-dokuları--çizim)
4. [Sesli Notlar & Canlı Ses Dalgası](#4-sesli-notlar--canlı-ses-dalgası)
5. [Yapay Zeka (AI) Motoru & Yetenekleri](#5-yapay-zeka-ai-motoru--yetenekleri)
6. [Hatırlatıcılar & Akıllı Bildirimler](#6-hatırlatıcılar--akıllı-bildirimler)
7. [Android Sistem Entegrasyonları (Widget, Quick Tile, Web Clipper)](#7-android-sistem-entegrasyonları)
8. [Dışa Aktarma & Paylaşım (PDF, Görsel Kartı, Markdown)](#8-dışa-aktarma--paylaşım)
9. [Güvenlik, Biyometri & Gizlilik](#9-güvenlik-biyometri--gizlilik)
10. [Yedekleme, Senkronizasyon & Güncelleme](#10-yedekleme-senkronizasyon--güncelleme)
11. [Görsel Tema & Kişiselleştirme](#11-görsel-tema--kişiselleştirme)

---

## 1. Ana Not Listesi & Navigasyon

- **Dinamik Küçülen Başlık (Scroll-Aware Collapsible Header):** Sayfa aşağı kaydırıldığında büyük iOS başlığı ve özet barı ipeksi bir animasyonla küçülür; ekran alanının %90'ından fazlası notlara ayrılır. En üste çıkıldığında başlık eski haline döner.
- **Klasör Yönetimi & Silme (Folder Management):**
  - Özel ikonlu klasörler oluşturma ve yeniden adlandırma.
  - Klasör silme desteği: *"Bu klasördeki notlar silinmez, klasörsüz alana aktarılır"* güvencesiyle veriler korunur.
  - Hızlı klasör sekmeleri: `Tüm Notlar`, `Sabitlenenler`, `Kilitli Notlar`, `Çöp Kutusu` ve kullanıcı klasörleri.
- **Kaydırılabilir Not Kartları (Swipeable Note Cards):**
  - **Sola Kaydırarak Silme:** Donma/takılma yapmayan `key(note.id)` ve `AnimatedVisibility` (shrinkVertically + fadeOut) tabanlı animasyonlu silme.
  - **Sağa Kaydırarak Sabitleme:** Başa tutturma/sabitlemeyi kaldırma.
- **Kart Üzerinde Doğrudan Görev Tikleme (Inline Task Check):** Notun içine girmeye gerek kalmadan, kart üzerindeki `- [ ]` kontrol maddelerine doğrudan dokunarak tik atabilme ve veritabanını anında güncelleme.
- **Akıllı Web Alan Adı Rozeti (Smart Link Badge):** Notta yer alan web bağlantılarını otomatik tespit edip kartta temiz bir alan adı rozeti (`🔗 github.com`, `🔗 medium.com`) olarak gösterme.
- **Görsel Önizleme:** Notun kapak görseli, çizimi veya ekli fotoğraflarını kart üzerinde küçük görsel olarak önizleme.
- **Kart Yoğunluğu Ayarı (Card Density):**
  - **Detaylı Görünüm:** Başlık, özet, etiketler, önizleme ve meta bilgiler.
  - **Kompakt Görünüm:** Rozetleri gizleyen, satır aralıklarını daraltan ve tek ekranda maksimum not gösteren liste.
- **Peek & Pop Hızlı Aksiyon Menüsü (Uzun Basış):**
  - Kart üzerine uzun basıldığında açılan alt menü (`NoteContextMenuBottomSheet`):
    - 📌 Başa Sabitle / Kaldır
    - 📋 Notu Çoğalt (Duplicate Note)
    - 🔒 Biyometrik Kilit Ekle / Aç
    - 📁 Klasöre Taşı
    - ☑️ Çoklu Seçim Modunu Başlat
    - 🗑️ Çöp Kutusuna Gönder
- **Çoklu Seçim Modu (Batch Actions):** Birden fazla not seçilerek toplu silme, toplu klasöre taşıma veya toplu sabitleme.
- **Canlı Arama & Filtreleme:** Başlık, içerik ve etiketlerde anlık arama, arama terimlerini sarı renkle vurgulama.
- **Obsidian Tarzı İnteraktif Bilgi Grafiği (Graph View):** `[[Not Başlığı]]` bağlantılarını fizik motorlu interaktif bir 2D ağ düğüm haritasında görselleştirme; düğümlere tıklayarak nota gidebilme.
- **Günün Özeti / Akıllı AI Barı:** Notlardan derlenen günlük akıllı özet ve hızlı ipuçları.
- **30 Günlük Otomatik Çöp Kutusu Temizliği:** 30 günden eski silinmiş notların arka planda otomatik olarak veritabanından temizlenmesi.

---

## 2. Gelişmiş Not Düzenleyici

- **Çoklu Not Sekmeleri (Editor Tabs Bar):** Üst çubuk altında son açılan 6 not arasında sekme değiştirir gibi tek tıkla geçiş yapabilme.
- **Notion Tarzı Kapak & Emoji Başlığı:**
  - Unsplash koleksiyonundan veya galeriden not kapağı ekleme.
  - Apple stili emoji seçiciyle not simgesi belirleme.
- **Apple Cupertino Biçimlendirme Çubuğu (CupertinoFormatBar):**
  - Kalın (`**`), İtalik (`*`), Üstü Çizili (`~~`)
  - Başlık 1 (`#`), Başlık 2 (`##`)
  - Görev Listesi (`- [ ]`), Madde İmleri (`-`), Numaralı Liste (`1.`)
  - Alıntı Bloğu (`>`), Kod Bloğu (` ``` `)
  - Not İçi Bağlantı Ekleme (`[[Not Başlığı]]`)
- **Slash Komutları Menüsü (`/`):** Not içinde `/` yazıldığında açılan ve başlık, tablo, kod, görev listesi veya ayraç eklemeyi sağlayan Apple tarzı hızlı komut menüsü.
- **İki Yönlü Not Bağlantıları (Wiki-Links):** `[[` yazılarak diğer notlara bağlantı verme ve tek tıkla bağlı nota atlama.
- **İçindekiler Tablosu (Table of Contents):** Not içindeki başlıklardan otomatik içindekiler listesi oluşturma ve nota ekleme.
- **Görsel Markdown Önizleme:** KaTeX matematik formülleri (`$E=mc^2$`), Mermaid akış şemaları ve görsel zengin metin önizleme modalı.
- **İnteraktif Tablo Düzenleyici (Table Editor):** Satır ve sütun ekleyip çıkarılabilen, hücreleri kolayca doldurulan görsel tablo oluşturucu.
- **Zen Daktilo Modu (Zen Focus Mode):** Tüm butonları ve dikkat dağıtıcıları gizleyen, daktilo odaklı tam ekran yazı modu.
- **Pomodoro Odak Zamanlayıcısı (Pomodoro Timer):** Not yazarken 25 dakikalık çalışma ve 5 dakikalık mola döngülerini yöneten entegre sayaç.
- **Sürüm Geçmişi & Geri Alma (Version History):**
  - Anlık Geri Al (`Undo`) ve İleri Al (`Redo`) yığını.
  - Room veritabanında saklanan geçmiş sürümler (`NoteHistoryEntity`) ve tek tıkla önceki sürüme dönebilme.
- **Şablon Yöneticisi (Custom Templates):** Sık kullanılan formatları şablon olarak kaydetme ve tek dokunuşla yeni notlara uygulama.
- **Karakter, Kelime & Okuma Süresi Sayacı:** Notun başındaki detaylar panelinde anlık istatistikler.

---

## 3. Defter Kağıdı Dokuları & Çizim

- **Canvas Kağıt Dokuları (Paper Textures):**
  - 📄 **Düz Sayfa (Blank):** Sade, modern arka plan.
  - 📏 **Çizgili Defter (Lined):** Klasik Apple Notes defter çizgileri.
  - 📐 **Kareli Defter (Grid):** Matematik, mühendislik ve planlar için ızgara kareleri.
  - 🔘 **Noktalı Defter (Dot Grid):** Bullet journal stilinde zarif noktalar.
  - 📜 **Sıcak Parşömen (Sepia):** Sıcak nostaljik kağıt tonu ve göz yormayan satırlar.
  - Sağ üstteki `⋯` menüsünden modal alt sayfa ile anında değiştirilir.
- **Apple Tarzı Çizim Tuvali (AppleDrawingDialog):**
  - Serbest el çizim, karalama ve el yazısı desteği.
  - Fırça kalınlığı, renk paleti, silgi, geri al/ileri al.
  - Çizimi PNG olarak doğrudan notun içerisine ekleme.

---

## 4. Sesli Notlar & Canlı Ses Dalgası

- **Canlı iOS Ses Dalgası Görselleştiricisi (Live Waveform Visualizer):**
  - Kayıt esnasında mikrofonun anlık genliğine (`MediaRecorder.maxAmplitude`) göre dans eden dikey çubuklar.
- **Arka Planda Ses Kaydı:** Yüksek kaliteli `.m4a` formatında ses kaydı alma.
- **Not İçi Dahili Ses Oynatıcı:** Ses dosyasını uygulama dışına çıkmadan dinleyebilme, süre çubuğu ve oynat/durdur kontrolleri.

---

## 5. Yapay Zeka (AI) Motoru & Yetenekleri

- **Çoklu AI Sağlayıcı Desteği (AI Providers):**
  - Google Gemini API (gemini-1.5-flash, gemini-1.5-pro)
  - OpenAI API (gpt-4o, gpt-4o-mini)
  - Anthropic Claude API (claude-3-5-sonnet, claude-3-haiku)
  - Google Vertex AI
  - Yerel Cihaz İçi Yapay Zeka (On-Device MediaPipe LLM / Gemma)
- **Kapsamlı AI Sihirbazı Eylemleri:**
  - 📝 **Özet Çıkar (Summarize):** Notun ana fikirlerini maddeler halinde çıkarma.
  - ✍️ **Yazım ve İmla Denetimi (Fix Grammar):** Dilbilgisi ve noktalama hatalarını düzeltme.
  - 👔 **Üslup Değiştirme:** Profesyonel, Samimi veya Kısa/Öz biçime dönüştürme.
  - ☑️ **Görevleri Çıkar (Extract Action Items):** Notun içindeki eylemleri kontrol listesine (`- [ ]`) çevirme.
  - 🏷️ **Akıllı Başlık ve Etiketler (Auto Title & Tags):** Nota uygun başlık ve `#etiket` önerileri oluşturma.
  - 🌐 **Çeviri (Translate):** Notu farklı dillere çevirme.
  - 🚀 **Yazmaya Devam Et (Continue Writing):** Yarım kalan düşünceyi tamamlama.
  - 🗂️ **Hafıza Kartları Oluştur (Flashcards):** Soru-cevap kartları üreterek çalışma modunda ezber yapma.
  - 🧠 **Zihin Haritası Çıkar (Mindmap):** Hiyerarşik kavram ağacı oluşturma.
  - ⏰ **Akıllı Hatırlatıcı Tespiti (Extract Reminders):** Tarih ve saat içeren ifadeleri algılayıp alarm önerme.
- **Notla Sohbet Et (Chat with Note):** Sadece açık olan notun içeriğine dayalı sorular sorup yanıt alabildiğiniz özel sohbet botu.
- **Genel AI Asistanı:** Uygulama genelinde serbestçe soru sorulabilen yüzen sohbet ekranı.

---

## 6. Hatırlatıcılar & Akıllı Bildirimler

- **Hassas Zamanlı Hatırlatıcılar (Exact Alarms):** `AlarmManager` ile takvimden tarih ve saat seçilerek kurulan bildirimler.
- **Cihaz Yeniden Başlatma Koruması:** Telefon kapandığında alarmları hafızada tutup açılışta (`BOOT_COMPLETED`) otomatik yenileme.
- **Eylemli Bildirimler (Actionable Notifications):**
  - `⏰ 15 Dk Ertele`: Bildirimden ayrılmadan hatırlatıcıyı 15 dakika sonrasına erteleme.
  - `✓ Tamamlandı`: Notu ve hatırlatıcıyı tek tıkla tamamlandı olarak işaretleme.

---

## 7. Android Sistem Entegrasyonları

- **Android 14+ Hızlı Ayarlar Perdesi Kutucuğu (Quick Settings Tile):**
  - Bildirim panelini aşağı çekince Wi-Fi/Bluetooth yanına yerleşen **"Notism Hızlı Not"** kutucuğu (`QuickNoteTileService`).
  - Tek dokunuşla bildirim perdesini indirip doğrudan yeni not ekranını açar.
- **Masaüstü Araç Takımı (AppWidget):**
  - Android ana ekranına eklenebilen widget (`QuickNotesWidgetProvider`).
  - Son notları listeleme ve tek tıkla yeni not açma.
- **Web Kırpıcı (Web Clipper):**
  - Chrome, Twitter/X, Instagram gibi uygulamalardan "Paylaş" denildiğinde Notism'i seçerek web sayfası bağlantısını ve özetini otomatik yeni not yapma.

---

## 8. Dışa Aktarma & Paylaşım

- **PDF Olarak Dışa Aktarma:** Notu şık tipografik sayfa düzeniyle standart PDF belgesine dönüştürme.
- **Estetik Sosyal Medya Kartı (PNG Export):**
  - Instagram, Twitter veya LinkedIn için degradeli, şık logolu, kartvizit estetiğinde görsel oluşturma.
- **Markdown & Düz Metin:** `.md` veya `.txt` dosyası olarak kaydetme.
- **Sistem Paylaşımı:** Not içeriğini WhatsApp, Mail, Telegram vb. uygulamalara tek tıkla iletme.

---

## 9. Güvenlik, Biyometri & Gizlilik

- **Cihaz İçi Şifreleme & Biyometri:**
  - Uygulama geneline veya not bazında Parmak İzi / Yüz Tanıma / Kilit Ekranı PIN koruması (`BiometricPrompt`).
- **Son Uygulamalarda Gizleme (App Switcher Privacy):**
  - Uygulama arka plana atıldığında ekran görüntüsü alınmasını ve çoklu görev menüsünde içeriğin görünmesini engelleyen gizlilik modu (`FLAG_SECURE` / Blur).
- **%100 Çevrimdışı & Güvenli:** Notlar sadece cihazdaki şifrelenmiş SQLite/Room veritabanında saklanır; kullanıcının izni olmadan hiçbir sunucuya veri gönderilmez.

---

## 10. Yedekleme, Senkronizasyon & Güncelleme

- **Kişisel Bulut Senkronizasyonu (WebDAV / Nextcloud):** Kendi özel sunucunuzla uçtan uca şifreli yedekleme ve senkronizasyon.
- **Yerel JSON/ZIP Yedekleme & Geri Yükleme:** Tek tıkla tüm notları, klasörleri ve ekleri arşiv dosyası olarak cihaz depolamasına kaydetme ve geri yükleme.
- **Uygulama İçi Güncelleyici (GitHub In-App Updater):**
  - GitHub Releases API'si üzerinden yeni sürümleri kontrol etme (`GitHubUpdateChecker`).
  - Uygulama içerisinden APK indirme, ilerleme çubuğu ve tek tıkla güncelleme kurma.

---

## 11. Görsel Tema & Kişiselleştirme

- **Otantik Apple Cupertino Arayüzü:** SF Pro tipografisi hissi, 16dp yuvarlatılmış köşeler, Apple haptik titreşimleri (`HapticFeedbackHelper`).
- **Dinamik Açık/Koyu Tema:** Sistem temasını takip edebilme veya zorunlu Koyu / Açık tema seçebilme.
- **6 Farklı iOS Vurgu Rengi (Accent Colors):**
  - 🟡 Apple Altın Sarısı (Varsayılan)
  - 🔵 iOS Mavisi
  - 🟠 Canlı Turuncu
  - 🌸 Pembe / Fuşya
  - 🟣 Mor
  - 🟢 Nane Yeşili
- **Haptik Dokunsal Geri Bildirim:** Buton dokunuşlarında, silmelerde, tiklemelerde ve menü açılışlarında hafif, orta ve güçlü titreşimler.
