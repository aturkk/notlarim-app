package com.applenotes.ai.core.templates

data class NoteTemplate(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val coverUrl: String?,
    val content: String,
    val defaultTags: List<String>
)

object NoteTemplates {
    val templates = listOf(
        NoteTemplate(
            id = "weekly_planner",
            title = "Haftalık Planlayıcı & Alışkanlık Takipçisi",
            description = "Haftalık hedefler, günlük yapılacaklar ve alışkanlık matrisi",
            icon = "🎯",
            coverUrl = "https://images.unsplash.com/photo-1506784983877-45594efa4cbe?w=1200&q=80",
            defaultTags = listOf("plan", "verimlilik", "aliskanlik"),
            content = """
# 🎯 Haftanın Odak Noktaları
> 💡 *“Gününüzü planlamazsanız, başkalarının planlarının bir parçası olursunuz.”*

- [ ] Öncelik 1: 
- [ ] Öncelik 2: 
- [ ] Öncelik 3: 

---

## 📊 Alışkanlık Takipçisi (Habit Tracker)
| Alışkanlık | Pzt | Sal | Çar | Per | Cum | Cmt | Paz |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 💧 2L Su İç | [ ] | [ ] | [ ] | [ ] | [ ] | [ ] | [ ] |
| 📖 20 Sayfa Oku | [ ] | [ ] | [ ] | [ ] | [ ] | [ ] | [ ] |
| 🏃‍♂️ Egzersiz (30dk) | [ ] | [ ] | [ ] | [ ] | [ ] | [ ] | [ ] |
| 🧘‍♂️ Meditasyon | [ ] | [ ] | [ ] | [ ] | [ ] | [ ] | [ ] |

---

## 📅 Günlük Akış
<details>
<summary><b>Pazartesi</b></summary>

- [ ] 
- [ ] 
</details>

<details>
<summary><b>Salı</b></summary>

- [ ] 
- [ ] 
</details>

<details>
<summary><b>Çarşamba</b></summary>

- [ ] 
- [ ] 
</details>

<details>
<summary><b>Perşembe</b></summary>

- [ ] 
- [ ] 
</details>

<details>
<summary><b>Cuma</b></summary>

- [ ] 
- [ ] 
</details>

<details>
<summary><b>Hafta Sonu</b></summary>

- [ ] Dinlenme ve hobi aktiviteleri
- [ ] Gelecek haftanın planı
</details>

---

## 📝 Hafta Sonu Retrospektifi
- **Bu hafta ne iyi gitti?**: 
- **Neyi daha iyi yapabilirdim?**: 
- **Haftanın Puanı (1-10)**: 
            """.trimIndent()
        ),

        NoteTemplate(
            id = "meeting_notes",
            title = "Toplantı Notları & Aksiyon Planı",
            description = "Katılımcılar, gündem maddeleri, alınan kararlar ve görev dağılımı",
            icon = "📋",
            coverUrl = "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=1200&q=80",
            defaultTags = listOf("is", "toplanti", "yonetim"),
            content = """
# 📋 Toplantı Notları
> **Tarih:** 2026-09-06  
> **Katılımcılar:** @Ahmet, @Ayşe, @Mehmet  
> **Konu:** Q4 Hedefleri ve Ürün Geliştirme

---

## 📌 Gündem Maddeleri
1. Önceki aksiyon maddelerinin gözden geçirilmesi
2. Yeni özellik setinin mimari analizi
3. Lansman takvimi ve risk değerlendirmesi

---

## 💡 Tartışılan Konular & Notlar
- Mimari geçiş için belirlenen zaman planı: 2 hafta.
- Test kapsamının %85 üzerine çıkarılması kararlaştırıldı.
- Müşteri geri bildirimleri incelendi ve UI iyileştirmelerine öncelik verildi.

---

## ⚡ Alınan Kararlar
- [x] Room v3 veritabanı şemasına geçiş onaylandı.
- [x] Notion tarzı blok sistemi devreye alındı.

---

## 🚀 Aksiyon Maddeleri (Görev Dağılımı)
| Görev | Sorumlu | Teslim Tarihi | Durum |
| :--- | :--- | :---: | :---: |
| API Entegrasyonunu Tamamla | @Ahmet | 12 Eylül | Devam Ediyor |
| Tasarım İncelemesi | @Ayşe | 10 Eylül | Beklemede |
| Test Senaryoları | @Mehmet | 15 Eylül | Başlamadı |
            """.trimIndent()
        ),

        NoteTemplate(
            id = "cornell_notes",
            title = "Cornell Not Alma Tekniği",
            description = "Etkili öğrenme için sol sütun ipuçları, sağ sütun notlar ve alt özet",
            icon = "🎓",
            coverUrl = "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=1200&q=80",
            defaultTags = listOf("ogrenme", "ders", "arastirma"),
            content = """
# 🎓 Konu: [Ders / Konu Başlığı]
> **Tarih:** 2026-09-06  
> **Kaynak / Eğitmen:** [Kitap / Profesör Adı]

---

## 🔑 Anahtar Kavramlar & İpuçları (Cue Column)
- **Soru 1:** Temel problem nedir?
- **Kavram 2:** Ana prensipler nelerdir?
- **Önemli Terim:** [Terim Açıklaması]

---

## 📝 Detaylı Notlar (Lecture Notes)
- Ana argüman ve destekleyici kanıtlar:
  - 1. Kanıt noktası
  - 2. Deney veya vaka incelemesi
- Formül veya Temel Kural:
```
F = m * a
```
- Kritik uyarılar ve istisnalar:
  > ⚠️ Bu kural uç durumlarda geçerli olmayabilir!

---

## 📌 Özet (Summary)
*Bu konunun ana fikri nedir ve gerçek hayatta nasıl uygulanır?*

> **Sonuç:** Burada tartışılan ana prensip, sistemlerin ölçeklenebilirliğini doğrudan etkiler. Pratik uygulamada ilk adım analiz, ikinci adım optimizasyon olmalıdır.
            """.trimIndent()
        ),

        NoteTemplate(
            id = "book_review",
            title = "Kitap & Medya İncelemesi",
            description = "Yazar, ana fikir, en çarpıcı alıntılar ve kişisel çıkarımlar",
            icon = "📚",
            coverUrl = "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=1200&q=80",
            defaultTags = listOf("kitap", "kisisel-gelisim", "kultur"),
            content = """
# 📚 Kitap: [Kitap Adı]
> **Yazar:** [Yazar Adı]  
> **Tür:** Kurgu Dışı / Felsefe / Bilim  
> **Değerlendirme:** ⭐⭐⭐⭐⭐ (5/5)  
> **Okuma Süresi:** [Başlangıç] - [Bitiş]

---

## 💡 Kitabın 1 Cümlelik Özeti
Kitap, insanın anlam arayışını ve zorluklar karşısındaki zihinsel dayanıklılığını inceliyor.

---

## 🔑 Ana Çıkarımlar & Fikirler
1. **Düşünce 1:** Tepkilerimiz olaylardan değil, olaylara yüklediğimiz anlamdan doğar.
2. **Düşünce 2:** Küçük alışkanlıklar bileşik getiri gibi zamanla devasa farklar yaratır.
3. **Düşünce 3:** Odaklanma becerisi 21. yüzyılın en değerli süper gücüdür.

---

## 💬 Unutulmaz Alıntılar (Quotes)
> *"Yaşamak için bir nedeni olan kişi, hemen her 'nasıl'a katlanabilir."*

> *"Başarı bir hedef değil, sürekli bir iyileşme sürecidir."*

---

## 🎯 Hayatıma Nasıl Uygulayacağım?
- [ ] Her sabah 15 dakika kitap okuma rutini oluştur
- [ ] Çalışma alanımı dikkat dağıtıcı unsurlardan arındır
            """.trimIndent()
        ),

        NoteTemplate(
            id = "budget_tracker",
            title = "Aylık Bütçe & Harcama Takipçisi",
            description = "Gelir, sabit ve değişken gider tabloları ile tasarruf hedefleri",
            icon = "💰",
            coverUrl = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=1200&q=80",
            defaultTags = listOf("finans", "butce", "tasarruf"),
            content = """
# 💰 Aylık Bütçe Planı: [Ay / Yıl]
> **Toplam Gelir:** ₺0.00  
> **Tahmini Gider:** ₺0.00  
> **Net Tasarruf:** ₺0.00 (%0)

---

## 💵 Gelir Kalemleri
| Kaynak | Beklenen | Gerçekleşen | Notlar |
| :--- | :---: | :---: | :--- |
| Maaş | ₺0.00 | ₺0.00 | Ayın 1'i |
| Yan Gelir / Freelance | ₺0.00 | ₺0.00 | Proje bazlı |
| Yatırım Getirisi | ₺0.00 | ₺0.00 | Temettü |

---

## 🏠 Sabit Giderler
| Gider | Kategori | Tutar | Ödendi mi? |
| :--- | :--- | :---: | :---: |
| Kira / Konut | Barınma | ₺0.00 | [ ] |
| Faturalar (Elek, Su, Gaz) | Hizmet | ₺0.00 | [ ] |
| İnternet & Abonelikler | Teknoloji | ₺0.00 | [ ] |

---

## 🛒 Değişken Harcamalar & Limitler
| Kategori | Haftalık Limit | Aylık Toplam | Durum |
| :--- | :---: | :---: | :--- |
| Mutfak & Market | ₺0.00 | ₺0.00 | Normal |
| Sosyal & Yeme-İçme | ₺0.00 | ₺0.00 | Normal |
| Ulaşım & Yakıt | ₺0.00 | ₺0.00 | Normal |

---

## 🎯 Birikim & Yatırım Hedefleri
- [ ] Acil Durum Fonuna Ekle: ₺0.00
- [ ] Hisse / Fon Yatırımı: ₺0.00
            """.trimIndent()
        ),

        NoteTemplate(
            id = "project_sprint",
            title = "Proje & Sprint Takipçisi",
            description = "Sprint hedefleri, kapsam, görev listesi ve retrospektif",
            icon = "🚀",
            coverUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1200&q=80",
            defaultTags = listOf("proje", "sprint", "yazilim"),
            content = """
# 🚀 Sprint #1: [Proje Başlığı]
> **Sprint Süresi:** [Tarih Aralığı]  
> **Sprint Hedefi:** Kullanıcı deneyimini ve performans skorunu 2 katına çıkarmak.

---

## 📋 İş Listesi (Backlog)
- [ ] Veritabanı indexleme optimizasyonu
- [ ] Kullanıcı arayüzüne karanlık mod iyileştirmesi
- [ ] Çevrimdışı önbellek senkronizasyonu

---

## ⚡ Yapım Aşamasında (In Progress)
- [ ] Slash komut menüsü bileşeni
- [ ] Kanban pano görünümü entegrasyonu

---

## ✅ Tamamlananlar (Done)
- [x] Room v3 migrasyonu
- [x] Release anahtar yönetimi

---

## 🔍 Riskler & Bağımlılıklar
> ⚠️ **Dikkat:** Harici API kotalarına dikkat edilmeli, hız aşımları durumunda yedek model devreye alınmalı.
            """.trimIndent()
        )
    )
}
