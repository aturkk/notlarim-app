# Notism (Notlar Uygulaması) - Eksiksiz Özellik & Mimari Kataloğu
> **Sürüm:** v2.3.0 (versionCode: 25)  
> **Paket Adı:** `com.applenotes.ai`  
> **Hedef Platform:** Android (minSdk 26, targetSdk 35)  
> **Tasarım Dili:** Apple iOS 18 / macOS Sequoia Cupertino Design Language  
> **Animasyon & Hissiyat:** Emil Kowalski Physics-Based Motion & Apple Tactile Haptics  
> **Mimari:** Clean Architecture + MVVM + Jetpack Compose + Room Local-First  

Bu belge, **Notism** uygulamasında mevcut olan tüm modülleri, ekranları, görünüm modlarını, animasyon yapılarını, sistem entegrasyonlarını, şablonlarını, donanımsal güvenlik katmanlarını ve yapay zeka özelliklerini eksiksiz olarak listeler.

---

## 📑 İÇİNDEKİLER
1. [Görünüm Modları & Ana Not Listesi (Liste, Kompakt, Kanban, Takvim, Graph)](#1-görünüm-modları--ana-not-listesi)
2. [Gelişmiş Not Düzenleyici & Sayfa Özellikleri](#2-gelişmiş-not-düzenleyici--sayfa-özellikleri)
3. [Yerleşik Şablon Kütüphanesi & Özel Şablon Yöneticisi](#3-yerleşik-şablon-kütüphanesi--özel-şablon-yöneticisi)
4. [Defter Kağıdı Dokuları & Serbest Çizim Tuvali](#4-defter-kağıdı-dokuları--serbest-çizim-tuvali)
5. [Sesli Notlar, Canlı Dalga & Hızlı Widget Kaydı](#5-sesli-notlar-canlı-dalga--hızlı-widget-kaydı)
6. [Yapay Zeka (AI) Motoru (7 Sağlayıcı & AI Hub)](#6-yapay-zeka-ai-motoru-7-sağlayıcı--ai-hub)
7. [Fizik Tabanlı Animasyonlar & Mikro-Etkileşimler (Emil Kowalski Sistemi)](#7-fizik-tabanlı-animasyonlar--mikro-etkileşimler)
8. [Evrensel Komut Paleti (Spotlight / Raycast Stili)](#8-evrensel-komut-paleti)
9. [Hatırlatıcılar & Akıllı Bildirimler](#9-hatırlatıcılar--akıllı-bildirimler)
10. [Android Sistem Entegrasyonları (Quick Tile, Widget, Web Clipper, SAF)](#10-android-sistem-entegrasyonları)
11. [Dışa Aktarma, Sosyal Medya Kartları & Güvenli Paylaşım](#11-dışa-aktarma-sosyal-medya-kartları--güvenli-paylaşım)
12. [Güvenlik, Biyometri & Donanımsal Şifreleme (AES-256)](#12-güvenlik-biyometri--donanımsal-şifreleme)
13. [Depolama Analizi, Önbellek Temizleme & Otomatik Yedekleme](#13-depolama-analizi-önbellek-temizleme--otomatik-yedekleme)
14. [Görsel Tema, Tipografi & Buzlu Cam (Frosted Glass)](#14-görsel-tema-tipografi--buzlu-cam-frosted-glass)

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
- **Akıllı Not Sıralama (`NoteSortOrder`):**
  - Listenin sağ üst `⋯` menüsünden veya komut paletinden seçilebilen 4 sıralama ölçütü:
    - 🕒 **Son Güncellenen** (Varsayılan)
    - 📅 **Oluşturulma Tarihi**
    - 🔤 **Başlığa Göre (A-Z)**
    - ⚡ **Önceliğe Göre (Acil → Düşük)**
  - Sabitlenen (Pinned) notlar her sıralamada otomatik olarak listenin en başında tutulur.
- **Peek & Pop Hızlı Aksiyon Menüsü (Uzun Basış):**
  - Kart üzerine uzun basıldığında açılan alt menü (`NoteContextMenuBottomSheet`):
    - 📌 Başa Sabitle / Kaldır
    - 📋 Notu Çoğalt (Duplicate Note)
    - 🔒 Biyometrik Kilit Ekle / Aç
    - 📄 **PDF Olarak Paylaş / Dışa Aktar** (Notun içine girmeden anında PDF üretip paylaşma)
    - 🖼️ **Görsel Kartı (PNG) Paylaş** (Instagram/Twitter için 4:5 sosyal paylaşım kartı üretimi)
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
- **Görsel Markdown & Katlanabilir Başlık Önizleme (`MarkdownPreviewBottomSheet`):**
  - `AppleSegmentedControl` ile iki güçlü görünüm arasında tek dokunuşla geçiş:
    - **✨ KaTeX & Şemalar:** KaTeX matematik formülleri (`$E=mc^2$`), Mermaid akış şemaları ve görsel zengin metin önizleme.
    - **📂 Katlanabilir Başlıklar:** Native Jetpack Compose (`CollapsibleMarkdownView`) tabanlı interaktif akordeon başlık katlama ve odaklı okuma.
- **Sağ Üst Daha Fazla (`⋯`) Menüsü:**
  - ⏳ **Zaman Makinesi (Sürüm Geçmişi):** Notun geçmiş sürümlerine göz atma ve tek tıkla geri yükleme (`VersionHistoryBottomSheet`).
  - 📂 **Katlanabilir Başlıklar (Okuma Modu):** Notu doğrudan katlanabilir başlık modunda açma.
  - 📋 **Şablon Uygula / Ekle:** Not dolu olsa dahi şablon kütüphanesinden şablon seçip mevcut nota uygulayabilme (`TemplatePickerBottomSheet`).
  - ⏰ Hatırlatıcı Kur / Düzenle, 📌 Başa Sabitle, 🔒 Notu Kilitle, 📋 Şablon Olarak Kaydet, 💬 Notla Sohbet Et, 📑 İçindekiler Tablosu, ⏱️ Pomodoro Sayacı, 📜 Kağıt Deseni ve 🗑️ Notu Sil.
- **İnteraktif Tablo Düzenleyici (`TableEditorDialog`):** Satır ve sütun ekleyip çıkarılabilen, hücreleri kolayca doldurulan görsel tablo oluşturucu.
- **Zen Daktilo Modu (`ZenFocusModeDialog`):** Tüm butonları ve dikkat dağıtıcıları gizleyen, daktilo odaklı tam ekran yazı modu.
- **Pomodoro Odak Zamanlayıcısı (`PomodoroTimerDialog`):** Not yazarken 25 dakikalık çalışma ve 5 dakikalık mola döngülerini yöneten entegre sayaç.
- **Sürüm Geçmişi & Geri Alma:**
  - Anlık Geri Al (`Undo`) ve İleri Al (`Redo`) yığını.
  - Room veritabanında saklanan geçmiş sürümler (`NoteHistoryEntity`) ve tek tıkla önceki sürüme dönebilme.
- **Karakter, Kelime & Okuma Süresi Sayacı:** Notun başındaki detaylar panelinde anlık istatistikler.

---

## 3. Yerleşik Şablon Kütüphanesi & Özel Şablon Yöneticisi

Uygulama, profesyonel not alma yöntemlerini içeren zengin bir yerleşik şablon seti (`NoteTemplates`) ve kullanıcının kendi şablonlarını üretmesini sağlayan bir altyapı (`CustomTemplateManager`) içerir:
1. 🎯 **Haftalık Planlayıcı & Alışkanlık Takipçisi:** Haftalık hedefler, 7 günlük alışkanlık matrisi (su, okuma, egzersiz, meditasyon), katlanabilir günlük akışlar (`<details>`) ve retrospektif.
2. 📋 **Toplantı Notları & Aksiyon Planı:** Katılımcılar, gündem maddeleri, tartışmalar, alınan kararlar ve sorumlu/tarih içeren görev tablosu.
3. 🎓 **Cornell Not Alma Tekniği:** Sol tarafta anahtar kavramlar/ipuçları (Cue Column), sağda detaylı ders notları ve en altta 1 paragraflık sentez özeti.
4. 📚 **Kitap & Medya İncelemesi:** Yazar, tür, 5 yıldızlı puanlama, 1 cümlelik özet, ana çıkarımlar, alıntılar ve hayat uygulama planı.
5. 💰 **Aylık Bütçe & Harcama Takipçisi:** Toplam gelir, sabit giderler tablosu, değişken harcama limitleri ve yatırım hedefleri.
6. 🚀 **Proje & Sprint Takipçisi:** Sprint hedefi, Backlog iş listesi, In Progress, Done ve risk/bağımlılık değerlendirmesi.
7. ➕ **Kullanıcıya Özel Şablon Kaydetme:** Herhangi bir notu tek tıkla (`Şablon Olarak Kaydet`) şablon kütüphanesine ekleme ve yeni not oluştururken kullanma.

---

## 4. Defter Kağıdı Dokuları & Serbest Çizim Tuvali

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

## 5. Sesli Notlar, Canlı Dalga & Hızlı Widget Kaydı

- **Canlı iOS Ses Dalgası Görselleştiricisi (`AudioWaveformVisualizer`):**
  - Kayıt esnasında mikrofonun anlık genliğine (`MediaRecorder.maxAmplitude`) göre dinamik dans eden dikey ses dalgası çubukları.
- **Arka Planda Ses Kaydı:** Yüksek kaliteli `.m4a` formatında ses kaydı alma (`AudioRecorderHelper`).
- **Not İçi Dahili Ses Oynatıcı:** Ses dosyasını uygulama dışına çıkmadan dinleyebilme, süre çubuğu ve oynat/durdur kontrolleri.
- **Widget Üzerinden Tek Dokunuşla Sesli Not:** Ana ekran widget'ındaki mikrofon butonuna basıldığında uygulamanın doğrudan mikrofonu açarak kayda başlaması (`ACTION_VOICE_NOTE`).

---

## 6. Yapay Zeka (AI) Motoru (7 Sağlayıcı & AI Hub)

- **7 Farklı Yapay Zeka Sağlayıcısı (`AiServiceManager`):**
  1. **Google Gemini API:** `gemini-2.5-flash`, `gemini-1.5-pro`
  2. **OpenAI API:** `gpt-4o`, `gpt-4o-mini`
  3. **Anthropic Claude API:** `claude-3-5-sonnet-20241022`, `claude-3-haiku`
  4. **Groq API:** `llama-3.3-70b-versatile` (milisaniyeler düzeyinde ultra hızlı yanıt)
  5. **OpenRouter API:** Açık kaynak (Llama, Mistral, DeepSeek) ve tescilli tüm modeller
  6. **Google Vertex AI:** Kurumsal bulut altyapısı ve özel proje kimliği
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

## 7. Fizik Tabanlı Animasyonlar & Mikro-Etkileşimler

Uygulamada Emil Kowalski'nin ödüllü hareket tasarımı prensipleri uygulanmıştır (`EmilMotionComponents`):
- **`bouncyClickable`:** Butonlara, kartlara ve sekmelere dokunulduğunda elemanı fiziksel bir yay (`Spring.DampingRatioMediumBouncy`) ile %96'ya küçülten ve haptik titreşim veren elastik basış efekti.
- **`AppleSegmentedControl`:** Apple iOS tarzı, arkasında yumuşak gölgeli beyaz/koyu hap kayan fiziksel görünüm değiştirici.
- **`SonnerFloatingToast`:** Vercel Sonner / iOS Dynamic Island esintili, ekranın üstünden kayarak inen (`spring scale & fade`), zarif bildirim hapı. Standart Android Toast mesajları yerine kullanılır.
- **`AiSmartPillHeader`:** Ana ekranda degrade parıltılı AI Asistanı kapsül butonu.

---

## 8. Evrensel Komut Paleti

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

## 9. Hatırlatıcılar & Akıllı Bildirimler

- **Hassas Zamanlı Hatırlatıcılar (Exact Alarms):** `AlarmManager` ile takvimden tarih ve saat seçilerek kurulan bildirimler (`ReminderScheduler`).
- **Cihaz Yeniden Başlatma Koruması:** Telefon kapandığında alarmları hafızada tutup açılışta (`BOOT_COMPLETED`) otomatik geri yükleyen alıcı (`ReminderReceiver`).
- **Eylemli Bildirimler (Actionable Notifications):**
  - `⏰ 15 Dk Ertele`: Bildirimden ayrılmadan hatırlatıcıyı 15 dakika sonrasına erteleme.
  - `✓ Tamamlandı`: Notu ve hatırlatıcıyı tek tıkla tamamlandı olarak işaretleme.

---

## 10. Android Sistem Entegrasyonları

- **Android 14+ Hızlı Ayarlar Perdesi Kutucuğu (`QuickNoteTileService`):**
  - Bildirim panelini aşağı çekince Wi-Fi/Bluetooth yanına yerleşen **"Notism Hızlı Not"** kutucuğu.
  - Tek dokunuşla bildirim perdesini indirip doğrudan yeni not ekranını açar (Android 14+ `PendingIntent` tam uyumlu).
- **Masaüstü Araç Takımı (`QuickNotesWidgetProvider`):**
  - Android ana ekranına eklenebilen widget.
  - Son notları listeleme, tek tıkla yeni not açma (`ACTION_NEW_NOTE`), tek tıkla ses kaydı başlatma (`ACTION_VOICE_NOTE`) ve tüm notları görüntüleme.
- **Akıllı Web Kırpıcı & Otomatik AI Özeti (`WebClipperHelper`):**
  - Chrome, Twitter/X, Instagram veya herhangi bir tarayıcıdan "Paylaş" seçeneğinde Notism seçildiğinde devreye girer.
  - Sayfa başlığı, URL ve HTML meta açıklamalarını otomatik çeker.
  - Yapay zeka aktifse, içeriği otomatik olarak analiz edip **"3 Maddelik Yapay Zeka Özeti"** oluşturur, otomatik `#Web` etiketi ve `🌐` ikonu ekler.

---

## 11. Dışa Aktarma, Sosyal Medya Kartları & Güvenli Paylaşım

- **PDF Olarak Dışa Aktarma (`NoteExporter.exportToPdf`):** A4 standart sayfa formatında, tipografik başlık, güncellenme tarihi, ayraç çizgisi ve temiz metin düzeniyle PDF dosyası üretme.
- **Estetik Sosyal Medya Kartı (PNG Export - `NoteExporter.exportToImageCard`):**
  - Instagram, Twitter veya LinkedIn için 1080x1350 piksel (4:5 en-boy oranı), yuvarlak köşeli kart, yumuşak gölge (`setShadowLayer`), Notism altın sarısı rozeti ve filigranla yüksek çözünürlüklü görsel üretimi.
- **ZIP Arşiv Dışa Aktarma (`createBackupZip`):** Notları `notes_backup.json` formatında paketleyen şık ZIP yedekleme.
- **Markdown & Düz Metin:** `.md` veya `.txt` dosyası olarak doğrudan kaydetme.
- **Android FileProvider Paylaşımı:** Güvenli URI (`FileProvider.getUriForFile`) ile WhatsApp, Telegram, Gmail vb. uygulamalara doğrudan dosya aktarımı.

---

## 12. Güvenlik, Biyometri & Donanımsal Şifreleme

- **Donanımsal Şifreli Tercihler (`SecurePreferences`):**
  - Tüm API anahtarları, model adları, kişisel bulut şifreleri Android Keystore donanım çipleriyle **AES-256 GCM** ve **AES-256 SIV** algoritmalarıyla şifrelenir (`EncryptedSharedPreferences`).
- **Cihaz İçi Biyometrik Kilit (`BiometricAuthHelper`):**
  - Uygulama genelinde veya not bazında Parmak İzi / Yüz Tanıma / Kilit Ekranı PIN koruması (`BiometricPrompt`).
- **Son Uygulamalarda Gizlilik Koruması (App Switcher Privacy):**
  - Uygulama arka plana atıldığında ekran görüntüsü alınmasını ve çoklu görev menüsünde içeriğin görünmesini engelleyen gizlilik modu (`FLAG_SECURE` / Blur).
- **%100 Çevrimdışı & Yerel:** Notlar sadece cihazdaki yerel SQLite/Room veritabanında saklanır; kullanıcının izni olmadan hiçbir sunucuya veri aktarılmaz.

---

## 13. Depolama Analizi, Önbellek Temizleme & Otomatik Yedekleme

- **Depolama Analizi & Temizleme (`StorageHelper`):**
  - Veritabanı (`apple_notes_db`, WAL, SHM), Medya (çizimler, ses kayıtları, ekler) ve Önbellek (`cacheDir`) boyutlarını bayt hassasiyetinde hesaplama.
  - Tek tıkla geçici önbelleği temizleme (`clearCache`).
- **Yedekleme & Bulut Senkronizasyonu (Mükerrersiz & Sadeleştirilmiş Tasarım):**
  - **Kişisel Bulut (WebDAV / Nextcloud - `CloudSyncDialog`):** Üçüncü parti sunuculara bağımlı kalmadan Nextcloud, ownCloud veya kişisel WebDAV sunucunuzla şifreli iki yönlü senkronizasyon (HTTP PUT/GET).
  - **Cihaza & Google Drive'a Yedekle (SAF ZIP):** Android Depolama Erişim Çerçevesi (SAF) üzerinden doğrudan Google Drive veya yerel depolama klasörü seçerek tek dokunuşla tam ZIP yedeği alma.
  - **Yedekten Geri Yükle (SAF ZIP):** Google Drive veya cihazdaki herhangi bir ZIP yedeğini seçip tüm notları eksiksiz geri yükleme.
- **Otomatik Arka Plan Yedekleme Zamanlayıcısı (`AutoBackupScheduler`):**
  - Günlük veya haftalık otomatik arka plan yedekleme.
  - Cihaz boştayken (`setAndAllowWhileIdle`) veritabanını şifreli zip/json olarak otomatik yedekler.
- **Uygulama İçi Güncelleyici (`GitHubUpdateService` / `UpdateDialog`):**
  - GitHub Releases API'si üzerinden yeni sürümleri kontrol etme (`aturkk/notlarim-app`).
  - Uygulama içerisinden doğrudan APK indirme, indirme ilerleme çubuğu ve tek tıkla kurulum.

---

## 14. Görsel Tema, Tipografi & Buzlu Cam (Frosted Glass)

- **Otantik Apple Cupertino Arayüzü:** SF Pro tipografisi hissi, 16dp yuvarlatılmış köşeler, Apple haptik titreşimleri.
- **Dinamik Açık/Koyu Tema:** Sistem temasını takip edebilme veya zorunlu Koyu / Açık tema seçebilme.
- **Buzlu Cam Efekti (`BlurModifiers.frostedGlass`):** Android 12+ (API 31+) cihazlarda donanım seviyesinde render-node tabanlı iOS akrilik/buzlu cam bulanıklığı (`.blur(20.dp)`), alt sürümlerde yarı saydam degrade katman koruması.
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
