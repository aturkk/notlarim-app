# 🍎 Apple Notes AI (Android)

Apple iOS Human Interface Guidelines (HIG) tasarım standartlarını Jetpack Compose ile Android'e taşıyan, sunucusuz (**BYOK - Bring Your Own Key**) yapay zeka özellikli ve GitHub Releases üzerinden **kendi kendini güncelleyen** yerel Android not uygulaması.

---

## ✨ Özellikler

### 🎨 1. Apple (iOS) Tasarım & Deneyimi
- **Frosted Glass (Bulanık Cam Efekti):** Üst ve alt menülerde iOS tarzı yarı saydam bulanıklık efekti.
- **Gruplanmış İçe Çökük Listeler (*Inset Grouped Lists*):** Apple Notes kart hiyerarşisi.
- **Kaydırma Aksiyonları (*Swipe Actions*):** Sağa kaydırarak sabitleme (*Pin*), sola kaydırarak çöp kutusuna gönderme.
- **Apple Tarzı Arama & Etiket Filtreleri:** Canlı arama ve #etiket filtre çipleri.

### 🤖 2. Sunucusuz Cihaz İçi Yapay Zeka (BYOK)
- **Desteklenen Modeller:** Google Gemini (Flash / Pro), OpenAI (GPT-4o / mini), Anthropic Claude ve OpenRouter.
- **Donanım Destekli Güvenlik:** API anahtarlarınız doğrudan cihazınızın donanım güvenlik çipinde (**Android Keystore + Encrypted Storage**) şifrelenir.
- **Yapay Zeka Sihirbazı:**
  - 📝 **Akıllı Özet Çıkar:** Kilit noktaları maddeler halinde özetler.
  - 💼 **Profesyonel / Samimi / Sadeleştirilmiş Yeniden Yazım:** Metnin üslubunu düzenler.
  - 📋 **Yapılacaklar Listesi Çıkar:** Not içeriğinden eylemleri to-do maddelerine dönüştürür.
  - 🏷️ **Otomatik Başlık ve Etiket Öner:** Not içeriğine göre başlık ve etiket önerir.
  - 💬 **Notla Sohbet Et:** Not bağlamında sorularınızı yanıtlar.

### 🔄 3. GitHub Otomatik Güncelleme Motoru (In-App Updater)
- Uygulama açılışında veya Ayarlar menüsünden GitHub Releases API'si ile en son sürümü (1.0.1 vb.) denetler.
- Yeni sürüm varsa sürüm notlarını (Changelog) gösterir, APK'yı doğrudan uygulama içinden indirir ve tek dokunuşla kurulumu başlatır.

### 🚀 4. GitHub Actions CI/CD Pipeline
- Herhangi bir versiyon etiketi (Örn: 1.0.0) pushlandığında GitHub Actions otomatik olarak APK'yı derler, imzalar ve GitHub Releases sayfasına yükler.

---

## 🛠️ Yerel Geliştirme ve Derleme

`ash
# Projeyi klonlayın
git clone https://github.com/<KULLANICI>/AppleNotesAI.git

# Debug APK derleme
./gradlew assembleDebug

# Release APK derleme
./gradlew assembleRelease
`

---

## 🔐 API Anahtarları Nasıl Alınır?

- **Google Gemini:** [Google AI Studio](https://aistudio.google.com/) üzerinden ücretsiz API anahtarı alınabilir.
- **OpenAI:** [OpenAI Platform](https://platform.openai.com/) üzerinden API anahtarı alınabilir.
- **Anthropic Claude:** [Anthropic Console](https://console.anthropic.com/) üzerinden API anahtarı alınabilir.
- **OpenRouter:** [OpenRouter.ai](https://openrouter.ai/) üzerinden çoklu model anahtarı alınabilir.
