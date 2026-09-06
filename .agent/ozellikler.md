# Notism (Notlar Uygulaması) - Eksiksiz Özellik & Mimari Kataloğu
> **Sürüm:** v2.3.0 (versionCode: 25)  
> **Paket Adı:** `com.applenotes.ai`  
> **Hedef Platform:** Android (minSdk 26, targetSdk 35)  
> **Tasarım Dili:** Apple iOS 18 / macOS Sequoia Cupertino Design Language  
> **Mimari:** Clean Architecture + MVVM + Jetpack Compose + Room Local-First  

Bu belge, **Notism** uygulamasında yer alan tüm ekranları, modülleri, görünüm modlarını, sistem entegrasyonlarını, donanımsal güvenlik yapılarını ve yapay zeka özelliklerini eksiksiz olarak listeler.

---

## 📑 İÇİNDEKİLER
1. [Görünüm Modları & Ana Not Listesi (Liste, Kompakt, Kanban, Takvim, Graph)](#1-görünüm-modları--ana-not-listesi)
2. [Gelişmiş Not Düzenleyici & Sayfa Özellikleri](#2-gelişmiş-not-düzenleyici--sayfa-özellikleri)
3. [Defter Kağıdı Dokuları & Serbest Çizim](#3-defter-kağıdı-dokuları--serbest-çizim)
4. [Sesli Notlar, Canlı Dalga & Hızlı Kayıt](#4-sesli-notlar-canlı-dalga--hızlı-kayıt)
5. [Yapay Zeka (AI) Motoru (7 Sağlayıcı & AI Hub)](#5-yapay-zeka-ai-motoru-7-sağlayıcı--ai-hub)
6. [Evrensel Komut Paleti (Spotlight / Raycast Stili)](#6-evrensel-komut-paleti)
7. [Hatırlatıcılar & Akıllı Bildirimler](#7-hatırlatıcılar--akıllı-bildirimler)
8. [Android Sistem Entegrasyonları (Tile, Widget, Clipper, Actions)](#8-android-sistem-entegrasyonları)
9. [Dışa Aktarma & Sosyal Medya Kartları (PDF, PNG, Markdown)](#9-dışa-aktarma--sosyal-medya-kartları)
10. [Güvenlik, Biyometri & Donanımsal Şifreleme (AES-256)](#10-güvenlik-biyometri--donanımsal-şifreleme)
11. [Yedekleme, Senkronizasyon (WebDAV + SAF) & Otomatik Zamanlayıcı](#11-yedekleme-senkronizasyon--otomatik-zamanlayıcı)
12. [Görsel Tema, Tipografi & Kişiselleştirme](#12-görsel-tema-tipografi--kişiselleştirme)

---

## 1. Görünüm Modları & Ana Not Listesi

- **Dinamik Küçülen Başlık (Scroll-Aware Collapsible Header):** Sayfa aşağı kaydırıldığında büyük iOS başlığı ve özet barı ipeksi bir animasyonla küçülür; ekran alanının %90'ından fazlası notlara ayrılır. En üste çıkıldığında başlık eski haline döner.
- **5 Farklı Not Görüntüleme Modu:**
  1. **Detaylı Liste / Grid Görünümü:** Başlık, özet, görsel kapaklar, etiketler ve meta bilgilerle zengin kartlar.
  2. **Kompakt Liste Modu:** Rozetleri gizleyen, dikey boşlukları daraltan ve tek ekranda maksimum not gösteren minimalist liste.
  3. **📋 Kanban Pano Görünümü (`KanbanBoardView`):** Notları durumlarına göre `TODO` (Yapılacaklar), `IN_PROGRESS` (Devam Edenler) ve `DONE` (Tamamlandı) sütunlarına ayıran, sütunlar arası kart taşıma imkanı sunan pano.
  4. **📅 Takvim Görünümü (`CalendarView`):** Notları aylık interaktif takvim üzerinde gün bazında gösteren, geçmiş/gelecek günlerdeki notları filtreleyen ve seçilen güne doğrudan not eklemeyi sağlayan takvim matrisi.
  5. **🕸️ İnteraktif Bilgi Ağı Grafiği (`GraphViewDialog`):** Obsidian tarzı 2D fizik motorlu ağ grafiği; `[[Not Bağlantısı]]` ile birbirine bağlı notları görsel düğümler halinde sunar ve tıklanan nota gider.
- **Klasör Yönetimi & Güvenli Silme:**
  - Özel ikonlu klasörler oluşturma ve yeniden adlandırma.
  - Klasör silme desteği: *"Bu klasördeki notlar silinmez, klasörsüz alana aktarılır"* güvencesiyle veriler korunur.
  - Hızlı klasör sekmeleri: `Tüm Notlar`, `Sabitlenenler`, `Kilitli Notlar`, `Çöp Kutusu` ve kullanıcı klasörleri.
- **Kaydırılabilir Not Kartları (`SwipeableNoteCard`):**
  - **Sola Kaydırarak Silme:** Donma/takılma yapmayan `key(note.id)` ve `AnimatedVisibility` (shrinkVertically + fadeOut) tabanlı animasyonlu silme.
  - **Sağa Kaydırarak Sabitleme:** Başa tutturma/sabitlemeyi kaldırma.
  - **Kart Üzerinde Doğrudan Görev Tikleme (Inline Task Check):** Notun içine girmeye gerek kalmadan, kart üzerindeki `- [ ]` kontrol maddelerine doğrudan dokunarak tik atabilme ve veritabanını anında güncelleme.
  - **Akıllı Web Alan Adı Rozeti (Smart Link Badge):** Notta yer alan web bağlantılarını otomatik tespit edip kartta temiz bir alan adı rozeti (`🔗 github.com`, `🔗 medium.com`) olarak gösterme.
  - **Görsel Önizleme:** Notun kapak görseli, çizimi veya ekli fotoğraflarını kart üzerinde küçük görsel olarak önizleme.
- **Peek & Pop Hızlı Aksiyon Menüsü (Uzun Basış):**
  - Kart üzerine uzun basıldığında açılan alt menü (`NoteContextMenuBottomSheet`):
    - 📌 Başa Sabitle / Kaldır
    - 📋 Notu Çoğalt (Duplicate Note)
    - 🔒 Biyometrik Kilit Ekle / Aç
    - 📁 Klasöre Taşı
    - ☑️ Çoklu Seçim Modunu Başlat
    - 🗑️ Çöp Kutusuna Gönder
- **Çoklu Seçim Modu (Batch Actions):** Birden fazla not seçilerek toplu silme, toplu klasöre taşıma veya toplu sabitleme.
- **Canlı Arama & Vurgulama:** Başlık, içerik ve etiketlerde anlık arama, arama terimlerini sarı renkle vurgulama.
- **Detaylı Çöp Kutusu Yönetimi (`TrashBottomSheet`):**
  - Silinen notları listeleme, silinme tarihini görme.
  - Tek tıkla nota geri dönme (`Restore`), kalıcı olarak silme (`Delete Forever`) ve çöp kutusunu tamamen boşaltma (`Empty Trash`).
  - 30 günden eski silinmiş notların arka planda otomatik olarak veritabanından temizlenmesi.

---

## 2. Gelişmiş Not Düzenleyici & Sayfa Özellikleri

- **Çoklu Not Sekmeleri (`EditorTabsBar`):** Üst çubuk altında son açılan 6 not arasında sekme değiştirir gibi tek tıkla geçiş yapabilme.
- **Notion Tarzı Sayfa Özellikleri Çubuğu (`PagePropertiesBar`):**
  - **Öncelik:** Düşük, Orta, Yüksek, Acil rozetleri.
  - **Durum:** Başlanmadı, Devam Ediyor, Tamamlandı, Beklemede.
  - **İlerleme Yüzdesi (Progress Slider):** %0 - %100 arası elle ilerleme belirleme veya nottaki checklist maddelerinden otomatik yüzde hesaplama.
- **Gerçek Zamanlı WYSIWYG Markdown Dönüştürme (`MarkdownVisualTransformation`):**
  - Metin yazılırken `#`, `**`, `*`, `~~`, `>` etiketlerini görünmez yaparak canlı biçimlendirme; başlık boyutları, kalınlık, yatıklık ve alıntı blokları doğrudan editör alanında gerçek zamanlı görünür.
- **Katlanabilir / Açılabilir Başlıklar (`CollapsibleMarkdownRenderer`):**
  - `# H1`, `## H2`, `### H3` başlıklarının yanındaki oklara dokunarak altındaki paragrafları katlama veya genişletme.
- **Notion Tarzı Kapak & Emoji Başlığı:**
  - Unsplash koleksiyonundan veya cihaz galerisinden not kapağı ekleme.
  - Apple stili emoji seçiciyle not simgesi belirleme.
- **Apple Cupertino Biçimlendirme Çubuğu (`CupertinoFormatBar`):**
  - Kalın (`**`), İtalik (`*`), Üstü Çizili (`~~`)
  - Başlık 1 (`#`), Başlık 2 (`##`)
  - Görev Listesi (`- [ ]`), Madde İmleri (`-`), Numaralı Liste (`1.`)
  - Alıntı Bloğu (`>`), Kod Bloğu (` ``` `)
  - Not İçi Bağlantı Ekleme (`[[Not Başlığı]]`)
- **Slash Komutları Menüsü (`/`):** Not içinde `/` yazıldığında açılan ve başlık, tablo, kod, görev listesi veya ayraç eklemeyi sağlayan Apple tarzı hızlı komut menüsü.
- **İki Yönlü Not Bağlantıları (Wiki-Links):** `[[` yazılarak diğer notlara bağlantı verme ve tek tıkla bağlı nota atlama.
- **İçindekiler Tablosu (`TableOfContentsBottomSheet`):** Not içindeki başlıklardan otomatik içindekiler listesi oluşturma ve nota ekleme.
- **Görsel Markdown Önizleme (`MarkdownPreviewBottomSheet`):** KaTeX matematik formülleri (`$E=mc^2$`), Mermaid akış şemaları ve görsel zengin metin önizleme modalı.
- **İnteraktif Tablo Düzenleyici (`TableEditorDialog`):** Satır ve sütun ekleyip çıkarılabilen, hücreleri kolayca doldurulan görsel tablo oluşturucu.
- **Zen Daktilo Modu (`ZenFocusModeDialog`):** Tüm butonları ve dikkat dağıtıcıları gizleyen, daktilo odaklı tam ekran yazı modu.
- **Pomodoro Odak Zamanlayıcısı (`PomodoroTimerDialog`):** Not yazarken 25 dakikalık çalışma ve 5 dakikalık mola döngülerini yöneten entegre sayaç.
- **Sürüm Geçmişi & Geri Alma (`VersionHistoryDialog`):**
  - Anlık Geri Al (`Undo`) ve İleri Al (`Redo`) yığını.
  - Room veritabanında saklanan geçmiş sürümler (`NoteHistoryEntity`) ve tek tıkla önceki sürüme dönebilme.
- **Şablon Yöneticisi (`CustomTemplateManager`):** Sık kullanılan formatları şablon olarak kaydetme ve tek dokunuşla yeni notlara uygulama.
- **Karakter, Kelime & Okuma Süresi Sayacı:** Notun başındaki detaylar panelinde anlık istatistikler.

---

## 3. Defter Kağıdı Dokuları & Serbest Çizim

- **Canvas Kağıt Dokuları (`PaperTexture`):**
  - 📄 **Düz Sayfa (Blank):** Sade, modern arka plan.
  - 📏 **Çizgili Defter (Lined):** Klasik Apple Notes defter çizgileri.
  - 📐 **Kareli Defter (Grid):** Matematik, mühendislik ve planlar için ızgara kareleri.
  - 🔘 **Noktalı Defter (Dot Grid):** Bullet journal stilinde zarif noktalar.
  - 📜 **Sıcak Parşömen (Sepia):** Sıcak nostaljik kağıt tonu ve göz yormayan satırlar.
  - Sağ üstteki `⋯` menüsünden modal alt sayfa ile anında değiştirilir.
- **Apple Tarzı Çizim Tuvali (`AppleDrawingCanvas` / `AppleDrawingDialog`):**
  - Serbest el çizim, karalama ve el yazısı desteği.
  - Fırça kalınlığı, renk paleti, silgi, geri al/ileri al.
  - Çizimi PNG olarak doğrudan notun içerisine ekleme.

---

## 4. Sesli Notlar, Canlı Dalga & Hızlı Kayıt

- **Canlı iOS Ses Dalgası Görselleştiricisi (`AudioWaveformVisualizer`):**
  - Kayıt esnasında mikrofonun anlık genliğine (`MediaRecorder.maxAmplitude`) göre dans eden dikey çubuklar.
- **Arka Planda Ses Kaydı:** Yüksek kaliteli `.m4a` formatında ses kaydı alma.
- **Not İçi Dahili Ses Oynatıcı:** Ses dosyasını uygulama dışına çıkmadan dinleyebilme, süre çubuğu ve oynat/durdur kontrolleri.
- **Widget Üzerinden Tek Dokunuşla Sesli Not:** Widget'taki mikrofon butonuna basıldığında uygulamanın doğrudan ses kaydı başlatan yeni bir not açması (`ACTION_VOICE_NOTE`).

---

## 5. Yapay Zeka (AI) Motoru (7 Sağlayıcı & AI Hub)

- **7 Farklı Yapay Zeka Sağlayıcısı (`AiServiceManager`):**
  1. **Google Gemini API:** `gemini-2.5-flash`, `gemini-1.5-pro`
  2. **OpenAI API:** `gpt-4o`, `gpt-4o-mini`
  3. **Anthropic Claude API:** `claude-3-5-sonnet-20241022`, `claude-3-haiku`
  4. **Groq API:** `llama-3.3-70b-versatile` (ultra hızlı yanıt süreleri)
  5. **OpenRouter API:** İstediğiniz tüm açık kaynak veya özel modeller
  6. **Google Vertex AI:** Kurumsal Google Cloud Vertex API ve proje kimliği
  7. **Cihaz İçi Yerel LLM (On-Device MediaPipe):** İnternetsiz, tamamen cihaz üzerinde çalışan yerel dil modelleri (Gemma)
- **Yapay Zeka Merkezi (`AiHubBottomSheet`):**
  - ☀️ **Sabah Özeti (Morning Digest):** Günün ilk açılışında tüm notları tarayarak günlük yapılacakları ve önemli notları özetleme.
  - 🧠 **Çoklu Not Sentezi (Multi-Note Synthesis):** Farklı notlar arasındaki gizli bağlantıları, ortak temaları ve içgörüleri sentezleyen derin analiz aracı.
  - 💬 **Global AI Asistanı (`GlobalAiChatBottomSheet`):** Uygulama genelinde serbestçe soru sorulabilen yapay zeka sohbet botu.
- **Not İçin Özel AI Sihirbazı Eylemleri:**
  - 📝 **Özet Çıkar (Summarize):** Notun ana fikirlerini maddeler halinde çıkarma.
  - ✍️ **Yazım ve İmla Denetimi (Fix Grammar):** Dilbilgisi ve noktalama hatalarını düzeltme.
  - 👔 **Üslup Değiştirme:** Profesyonel, Samimi veya Kısa/Öz biçime dönüştürme.
  - ☑️ **Görevleri Çıkar (Extract Action Items):** Notun içindeki eylemleri kontrol listesine (`- [ ]`) çevirme.
  - 🏷️ **Akıllı Başlık ve Etiketler (Auto Title & Tags):** Nota uygun başlık ve `#etiket` önerileri oluşturma.
  - 🌐 **Çeviri (Translate):** Notu farklı dillere çevirme.
  - 🚀 **Yazmaya Devam Et (Continue Writing):** Yarım kalan düşünceyi tamamlama.
  - 🗂️ **Hafıza Kartları Oluştur (Flashcards):** Soru-cevap kartları üreterek çalışma modunda ezber yapma (`AiFlashcardsDialog`).
  - 🧠 **Zihin Haritası Çıkar (Mindmap):** Hiyerarşik kavram ağacı oluşturma.
  - ⏰ **Akıllı Hatırlatıcı Tespiti (Extract Reminders):** Tarih ve saat içeren ifadeleri algılayıp alarm önerme.
- **Notla Sohbet Et (`AiChatBottomSheet`):** Sadece açık olan notun içeriğine dayalı sorular sorup yanıt alabildiğiniz özel sohbet botu.

---

## 6. Evrensel Komut Paleti

- **Spotlight / Raycast Stili Komut Paleti (`CommandPaletteBottomSheet`):**
  - Tek dokunuşla veya kısayolla açılan evrensel komut satırı.
  - **Hızlı Komutlar:**
    - 🔍 Tüm notlar arasında canlı arama ve doğrudan nota zıplama
    - ➕ Yeni Not Oluştur
    - 📅 Günlük Not Aç (Daily Note)
    - ☁️ Bulut Senkronizasyonunu Başlat
    - 🗑️ Çöp Kutusunu Aç
    - ⚙️ Ayarları Aç
    - ✨ Yapay Zeka Merkezini (AI Hub) Aç
    - 📁 Akıllı Klasörlere Git

---

## 7. Hatırlatıcılar & Akıllı Bildirimler

- **Hassas Zamanlı Hatırlatıcılar (Exact Alarms):** `AlarmManager` ile takvimden tarih ve saat seçilerek kurulan bildirimler (`ReminderScheduler`).
- **Cihaz Yeniden Başlatma Koruması:** Telefon kapandığında alarmları hafızada tutup açılışta (`BOOT_COMPLETED`) otomatik geri yükleyen alıcı (`ReminderReceiver`).
- **Eylemli Bildirimler (Actionable Notifications):**
  - `⏰ 15 Dk Ertele`: Bildirimden ayrılmadan hatırlatıcıyı 15 dakika sonrasına erteleme.
  - `✓ Tamamlandı`: Notu ve hatırlatıcıyı tek tıkla tamamlandı olarak işaretleme.

---

## 8. Android Sistem Entegrasyonları

- **Android 14+ Hızlı Ayarlar Perdesi Kutucuğu (`QuickNoteTileService`):**
  - Bildirim panelini aşağı çekince Wi-Fi/Bluetooth yanına yerleşen **"Notism Hızlı Not"** kutucuğu.
  - Tek dokunuşla bildirim perdesini indirip doğrudan yeni not ekranını açar (Android 14+ `PendingIntent` tam uyumlu).
- **Masaüstü Araç Takımı (`QuickNotesWidgetProvider`):**
  - Android ana ekranına eklenebilen widget.
  - Son notları listeleme, tek tıkla yeni not açma (`ACTION_NEW_NOTE`) ve tek tıkla ses kaydı başlatma (`ACTION_VOICE_NOTE`).
- **Web Kırpıcı (`WebClipperHelper`):**
  - Chrome, Twitter/X, Instagram gibi uygulamalardan "Paylaş" denildiğinde Notism'i seçerek web sayfası bağlantısını, sayfa başlığını ve özetini otomatik yeni not yapma (`Intent.ACTION_SEND`).

---

## 9. Dışa Aktarma & Sosyal Medya Kartları

- **PDF Olarak Dışa Aktarma (`NoteExporter.exportToPdf`):** Notu şık tipografik sayfa düzeniyle standart PDF belgesine dönüştürme.
- **Estetik Sosyal Medya Kartı (PNG Export - `NoteExporter.exportToImageCard`):**
  - Instagram, Twitter veya LinkedIn için degradeli, şık logolu, kartvizit estetiğinde yüksek çözünürlüklü görsel oluşturma.
- **Markdown & Düz Metin:** `.md` veya `.txt` dosyası olarak kaydetme.
- **Sistem Paylaşımı:** Not içeriğini WhatsApp, Mail, Telegram vb. uygulamalara tek tıkla iletme.

---

## 10. Güvenlik, Biyometri & Donanımsal Şifreleme

- **Donanımsal Şifreli Tercihler (`SecurePreferences`):**
  - Tüm API anahtarları, model isimleri ve gizli ayarlar Android Keystore donanım anahtarlarıyla **AES-256 GCM** ve **AES-256 SIV** algoritmalarıyla şifrelenir (`EncryptedSharedPreferences`).
- **Cihaz İçi Biyometrik Kilit (`BiometricAuthHelper`):**
  - Uygulama genelinde veya not bazında Parmak İzi / Yüz Tanıma / Kilit Ekranı PIN koruması (`BiometricPrompt`).
- **Son Uygulamalarda Gizlilik Koruması (App Switcher Privacy):**
  - Uygulama arka plana atıldığında ekran görüntüsü alınmasını ve çoklu görev menüsünde içeriğin görünmesini engelleyen gizlilik modu (`FLAG_SECURE` / Blur).
- **%100 Çevrimdışı & Yerel:** Notlar sadece cihazdaki yerel SQLite/Room veritabanında saklanır; kullanıcının izni olmadan hiçbir sunucuya veri aktarılmaz.

---

## 11. Yedekleme, Senkronizasyon & Otomatik Zamanlayıcı

- **İki Yönlü Bulut & Dizin Senkronizasyonu (`CloudSyncService`):**
  - **WebDAV / Nextcloud:** Kendi özel bulut sunucunuzla şifreli senkronizasyon.
  - **SAF (Storage Access Framework) Dizin Senkronizasyonu:** Cihaz depolamasındaki veya harici SD karttaki özel bir klasörle doğrudan dosya senkronizasyonu.
- **Otomatik Arka Plan Yedekleme Zamanlayıcısı (`AutoBackupScheduler` & `AutoBackupReceiver`):**
  - Günlük veya haftalık otomatik arka plan yedekleme.
  - Cihaz boştayken (`setAndAllowWhileIdle`) veritabanını şifreli zip/json olarak otomatik yedekler.
- **Manuel JSON/ZIP Yedekleme & Geri Yükleme (`BackupRestoreHelper`):** Tek tıkla tüm notları, klasörleri ve ekleri arşiv dosyası olarak kaydetme ve geri yükleme.
- **Uygulama İçi Güncelleyici (`GitHubUpdateService` / `UpdateDialog`):**
  - GitHub Releases API'si üzerinden yeni sürümleri kontrol etme (`aturkk/notlarim-app`).
  - Uygulama içerisinden doğrudan APK indirme, indirme ilerleme çubuğu ve tek tıkla kurulum.

---

## 12. Görsel Tema, Tipografi & Kişiselleştirme

- **Otantik Apple Cupertino Arayüzü:** SF Pro tipografisi hissi, 16dp yuvarlatılmış köşeler, Apple haptik titreşimleri.
- **Dinamik Açık/Koyu Tema:** Sistem temasını takip edebilme veya zorunlu Koyu / Açık tema seçebilme.
- **6 Farklı iOS Vurgu Rengi (`AppAccentColor`):**
  - 🟡 Apple Altın Sarısı (Varsayılan)
  - 🔵 iOS Mavisi
  - 🟢 Nane Yeşili
  - 🟣 Mor
  - 🟠 Canlı Turuncu
  - 🔴 Apple Kırmızısı
- **3 Farklı Tipografi & Yazı Tipi Ailesi (`AppFontFamily`):**
  - 🔤 **SYSTEM:** Modern, temiz Cupertino / SF Pro arayüz tipi.
  - 📖 **SERIF:** Kitap, edebiyat ve uzun okumalar için klasik tırnaklı yazı tipi.
  - 💻 **MONOSPACE:** Kod blokları, teknik notlar ve markdown odaklı daktilo yazı tipi.
- **Dokunsal Haptik Motoru (`HapticFeedbackHelper`):**
  - Seçim (`selection`), onay (`tick`), silme (`delete`), açılma (`heavy`) ve sürükleme durumlarında özelleştirilmiş fiziksel titreşimler.
