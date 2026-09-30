package com.example.data.model

enum class ThreatType(val arabicName: String, val code: String) {
    DRONE("طيران مسير", "UAV"),
    JAMMING("تشويش إلكتروني", "EW-JAM"),
    MISSILE("صواريخ ومقذوفات", "BML-MSL"),
    SURVEILLANCE_RADAR("رادار واستطلاع", "SIGINT")
}

enum class ThreatLevel(val arabicName: String, val militaryCode: String) {
    CRITICAL("حرج جداً - خطر داهم", "DEFCON-1"),
    HIGH("مرتفع - تأهب فوري", "DEFCON-2"),
    MEDIUM("متوسط - قيد الرصد", "DEFCON-3"),
    ELEVATED("تحذيري - استطلاع", "DEFCON-4")
}

enum class SouthGovernorate(
    val arabicName: String,
    val code: String,
    val defaultLat: Double,
    val defaultLng: Double,
    val keySectors: List<String>
) {
    ADEN(
        "العاصمة عدن",
        "ADN",
        12.80,
        45.03,
        listOf("خورمكسر", "كريتر", "المعلا", "التواهي", "المنصورة", "البريقة", "ميناء عدن الدولي", "مرصد جبل شمسان", "رادار مطار عدن الدولي")
    ),
    LAHIJ(
        "لحج",
        "LHJ",
        13.06,
        44.88,
        listOf("الحوطة", "ردفان", "تبن", "طور الباحة", "رأس العارة", "مضيق باب المندب الإستراتيجي")
    ),
    AD_DALI(
        "الضالع",
        "DAL",
        13.70,
        44.73,
        listOf("مدينة الضالع", "قعطبة", "مريس", "سناح", "الأزارق", "جحاف", "قطاع بتار والفاخر")
    ),
    ABYAN(
        "أبين",
        "ABY",
        13.50,
        45.80,
        listOf("زنجبار", "جعار / خنفر", "شقرة الساحلية", "لودر", "مودية", "المحفد", "أحور")
    ),
    SHABWAH(
        "شبوة",
        "SHB",
        14.53,
        46.83,
        listOf("عتق", "ميناء بلحاف للغاز", "بيحان", "عسيلان", "نصاب", "حبان", "ميفعة")
    ),
    HADRAMAWT(
        "حضرموت",
        "HAD",
        15.20,
        49.00,
        listOf("المكلا", "الشحر", "ميناء الضبة", "سيئون", "شبام التاريخية", "تريم", "القطن", "وادي المسيلة")
    ),
    AL_MAHRAH(
        "المهرة",
        "MHR",
        16.21,
        52.18,
        listOf("الغيضة", "ميناء نشطون", "منفذ شحن الحدودي", "منفذ صرفيت", "حوف الطبيعية", "سيحوت", "قشن")
    ),
    SOCOTRA(
        "أرخبيل سقطرى",
        "SCT",
        12.46,
        53.82,
        listOf("حديبو", "قلنسية", "جزيرة عبد الكوري", "جزيرة سمحة", "مطار سقطرى الدولي", "رأس إرسال")
    );

    companion object {
        fun fromName(name: String): SouthGovernorate {
            return entries.find { it.arabicName == name || it.name.equals(name, ignoreCase = true) } ?: ADEN
        }
    }
}

data class ThreatItem(
    val id: String,
    val type: ThreatType,
    val level: ThreatLevel,
    val designation: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Int = 0,
    val speedKmh: Int = 0,
    val frequencyBand: String = "",
    val headingDegrees: Float = 0f,
    val governorate: SouthGovernorate,
    val sector: String,
    val detectionSensor: String,
    val detectedAt: Long = System.currentTimeMillis(),
    val isVerified: Boolean = true,
    val isDecoy: Boolean = false,
    val launchOrigin: String = "",
    val notes: String = ""
)
