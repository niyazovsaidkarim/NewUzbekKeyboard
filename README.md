# Özbek Keyboard for Android

**Özbek Keyboard** (`uz.ozbekkeyboard`) — O‘zbek lotin yozuvining yangi islohotiga to‘liq moslashtirilgan, Samsung uslubidagi zamonaviy va 100% oflayn Android tizim klaviaturasi (Input Method Editor - IME).

Bu ilova oddiy matn muharriri yoki veb-simulyator emas, balki Android tizimining rasmiy `InputMethodService` va `InputConnection` arxitekturasida yaratilgan to‘laqonli tizim klaviaturasidir. Uni Telegram, WhatsApp, Chrome, Instagram, SMS va barcha boshqa Android dasturlarida standart klaviatura sifatida ishlatish mumkin.

---

## Asosiy Xususiyatlar

### 1. Yangi O‘zbek Lotin Alifbosi va Maxsus Harflar
Alifbo tartibi qulay standart QWERTY holatida saqlangan bo‘lib, ortiqcha joy egallamaslik uchun yangi islohot harflari uzoq bosish (long-press popup) orqali kiritiladi:
- **o → ö** (bosh harfi: **O → Ö**)
- **g → ğ** (bosh harfi: **G → Ğ**)
- **s → ş** (bosh harfi: **S → Ş**)
- **c → ç** (bosh harfi: **C → Ç**)
- **Tutuq belgisi (`ʼ` / `'`)**: `'` tugmasi va belgilar panelida rasmiy modifikator tutuq belgisi (`ʼ` - U+02BC) hamda standart apostrof mavjud.
- Maxsus harflarni kiritish uchun harf ustiga 400 ms (sozlamalarda moslanuvchan) bosib turiladi va variant ustiga siljib qo‘yib yuboriladi.

### 2. Space (Bo‘sh joy) Trekpadi — Kursor Boshqaruvi
- Qisqa bosish: oddiy bo‘sh joy (` `) kiritadi.
- Uzoq bosish: kursor harakatlantirish rejimini faollashtiradi (haptik tebranish bilan).
- Barmoqni gorizontal siljitish: matn ichida kursorni so‘zma-so‘z va harfma-harf aniq boshqaradi.
- Bo‘sh joy tugmasida har doim **"Özbekcha"** yozuvi ko‘rsatiladi.

### 3. Samsung Uslubidagi Backspace (O‘chirish)
- Samsung dizayniga to‘liq mos keluvchi chapga qaragan beshburchak (strelka) shakli va markazida ixcham `X` belgisi.
- Qisqa bosish: oldingi belgi yoki belgilangan matnni to‘liq o‘chiradi (surrogat juftliklar va emojilarni xavfsiz inobatga oladi).
- Uzoq bosish: uzluksiz tezkor o‘chirish rejimini ishga tushiradi.

### 4. Shift va Caps Lock
- 1 marta bosish: keyingi bitta harfni bosh harf qiladi (Shift).
- 2 marta tez bosish: Caps Lock rejimini qulflaydi (to‘liq bosh harflar).
- Avtomatik bosh harf: gap boshida, yangi qatorda va ism maydonlarida avtomatik bosh harfni faollashtiradi.

### 5. To‘liq Belgilar Paneli (2 sahifali)
`[!#1]` tugmasi bosilganda 1-sahifaga o‘tadi:
- **Sahifa 1**:
  `+ × ÷ = / _ < > [ ]`
  `! @ # £ % ^ & * ( )`
  `[1/2] - ' " : ; , ? [Del]`
- **Sahifa 2** (`[1/2]` bosilganda ochiladi):
  ``` ` ~ \ | { } € $ ₽ ₺ ```
  `° • ○ ● □ ■ ♡ ◇`
  `[2/2] ☆ · « » ¡ ¿ § [Del]`
- Pastki qatordagi tugma avtomatik `[ABC]`ga aylanadi.

### 6. Ixtiyoriy Raqamlar Qatori (1–0)
- Sozlamalarda **ON / OFF** qilish imkoniyati.
- Yoqilganda: QWERTY ustida doimiy `1 2 3 4 5 6 7 8 9 0` qatori ko‘rinadi.

### 7. Yuqori Asboblar Paneli (Toolbar)
5 ta qulay bo‘lim:
1. **Emoji**: Kategoriya bo‘yicha ajratilgan to‘liq oflayn kulgichlar to‘plami.
2. **Tarjima**: Xavfsizlik siyosatiga ko‘ra internetga ulanmaydi, oflayn rejim holati ko‘rsatiladi.
3. **Sozlamalar**: Tizim parametrlarini o‘zgartirish oynasini ochadi.
4. **Vaqtincha xotira (Clipboard)**: Foydalanuvchi roziligi (opt-in) asosida nusxalangan matnlar ro‘yxati.
5. **Ko‘proq**: Tezkor parametrlar menyusi.

### 8. Mavzular (Themes)
- **Qora va sariq (Samsung High Contrast)**:
  - Fon: deyarli qora (`#121212`)
  - Harflar: yorqin sariq (`#FFD500`)
  - Raqamlar: iliq to‘q sariq (`#FF8C00`)
  - Amallar (Shift, Del, Enter, Globe): oq (`#FFFFFF`)
  - Matn: qora (`#000000`)
  - Asboblar paneli: to‘q kulrang (`#1C1C1E`)
- **Tizimga moslashuvchan (Follow System)**
- **Minimal oq (Light)**
- **Minimal qora (Dark)**

### 9. 100% Xavfsizlik va Maxfiylik
- **INTERNET ruxsati mavjud emas.**
- Hech qanday ma’lumot tarmoqqa uzatilmaydi.
- Reklama, tahlil (analytics) va keylogger kodlari mutlaqo yo‘q.
- Parol kiritish maydonlarida tavsiyalar va xotira daftari avtomatik bloklanadi.

---

## O‘rnatish va Faollashtirish

### 1. APK faylini yuklash yoki o‘rnatish:
Tayyor debug APK fayli:
```bash
app/build/outputs/apk/debug/app-debug.apk
```

ADB orqali o‘rnatish:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Klaviaturani tizimda yoqish:
1. Ilovani oching (**Özbek Keyboard**).
2. **"1. Tizim sozlamalarini ochish"** tugmasini bosing va ro‘yxatda **Özbek Keyboard**ni faollashtiring.
3. **"2. Klaviaturani tanlash"** tugmasini bosib, kirish usuli sifatida **Özbek Keyboard**ni tanlang.
4. **"3. Sinab ko‘ring"** maydonida yozib ko‘ring.

---

## Loyihani Kompilyatsiya Qilish va Testlash

Loyihani build qilish:
```bash
./gradlew assembleDebug
```

Barcha avtomatlashtirilgan unit-testlarni ishga tushirish:
```bash
./gradlew testDebugUnitTest
```

---

## Texnik Tafsilotlar
- **Dasturlash tili**: Kotlin 2.0.21
- **Platforma**: Native Android (Min SDK 26, Target SDK 35)
- **Klaviatura dvigateli**: `InputMethodService` + Canvas custom view
- **Sozlamalar**: Jetpack DataStore Preferences
- **Paket nomi**: `uz.ozbekkeyboard`
