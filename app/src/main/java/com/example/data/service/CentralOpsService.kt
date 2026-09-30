package com.example.data.service

import com.example.data.model.SouthGovernorate
import com.example.data.model.ThreatItem
import com.example.data.model.ThreatLevel
import com.example.data.model.ThreatType
import kotlin.random.Random

object CentralOpsService {

    fun getInitialTacticalThreats(): List<ThreatItem> {
        val now = System.currentTimeMillis()
        return listOf(
            ThreatItem(
                id = "THR-101",
                type = ThreatType.DRONE,
                level = ThreatLevel.CRITICAL,
                designation = "UAV-ADN-409",
                name = "طائرة مسيرة انتحارية هجومية",
                latitude = 12.82,
                longitude = 44.95,
                altitudeMeters = 1950,
                speedKmh = 195,
                frequencyBand = "2.40 GHz FHSS",
                headingDegrees = 115f,
                governorate = SouthGovernorate.ADEN,
                sector = "القطاع الجوي لخورمكسر وميناء عدن",
                detectionSensor = "رادار جبل شمسان - محطة الرصد الكهروبصري",
                detectedAt = now - (15 * 60 * 1000),
                isVerified = true,
                isDecoy = false,
                launchOrigin = "المخاء",
                notes = "مسار مقترب من الميناء والمنشآت الحيوية في العاصمة عدن. تحذير جوي عاجل."
            ),
            ThreatItem(
                id = "THR-102",
                type = ThreatType.JAMMING,
                level = ThreatLevel.HIGH,
                designation = "EW-LHJ-033",
                name = "محطة تشويش إلكتروني بحرية/برية",
                latitude = 12.62,
                longitude = 43.55,
                altitudeMeters = 15,
                speedKmh = 0,
                frequencyBand = "GPS L1 / GLONASS L1 (1.5 GHz)",
                headingDegrees = 0f,
                governorate = SouthGovernorate.LAHIJ,
                sector = "مضيق باب المندب ورأس العارة",
                detectionSensor = "مرصد الاستطلاع الإشاري - رأس العارة",
                detectedAt = now - (42 * 60 * 1000),
                isVerified = true,
                isDecoy = false,
                launchOrigin = "الحديدة",
                notes = "تشويش كهرومغناطيسي مستمر يؤثر على ملاحة السفن التجارية وقوارب الصيد."
            ),
            ThreatItem(
                id = "THR-103",
                type = ThreatType.MISSILE,
                level = ThreatLevel.CRITICAL,
                designation = "BML-SHB-712",
                name = "صاروخ باليستي متوسط المدى",
                latitude = 14.82,
                longitude = 45.88,
                altitudeMeters = 13200,
                speedKmh = 2950,
                frequencyBand = "توجيه باليستي قصوري",
                headingDegrees = 140f,
                governorate = SouthGovernorate.SHABWAH,
                sector = "قطاع بيحان وعسيلان - شمال غرب شبوة",
                detectionSensor = "رادار عتق التكتيكي المشترك",
                detectedAt = now - (8 * 60 * 1000),
                isVerified = true,
                isDecoy = false,
                launchOrigin = "صنعاء",
                notes = "مسار قوسي فائق السرعة متجهاً نحو المنشآت الحيوية. تم تعميم الإنذار المبكر لكافة النقاط."
            ),
            // Example Decoy / Ghost Target for immediate test
            ThreatItem(
                id = "THR-DEC-09",
                type = ThreatType.SURVEILLANCE_RADAR,
                level = ThreatLevel.ELEVATED,
                designation = "GHOST-SIG-88",
                name = "[شبح] إشارة استدراج إلكترونية وهمية",
                latitude = 13.15,
                longitude = 44.40,
                altitudeMeters = 500,
                speedKmh = 60,
                frequencyBand = "بصمة زائفة (Spoofed)",
                headingDegrees = 180f,
                governorate = SouthGovernorate.LAHIJ,
                sector = "قطاع كرش والصبيحة",
                detectionSensor = "مرصد الاستطلاع الإشاري",
                detectedAt = now - (5 * 60 * 1000),
                isVerified = false,
                isDecoy = true,
                launchOrigin = "المخاء",
                notes = "إشارة رادارية وهمية مصطنعة بهدف تشتيت وسائط الاستطلاع والرصد (تم كشفها سيبرانياً كهدف مخادع)."
            )
        )
    }

    // Routine Military Threat Feed: Continuous routine incursions from northern origins and maritime borders
    fun generateRoutineAttack(): ThreatItem {
        val now = System.currentTimeMillis()
        val routineTypes = listOf("UAV_ROUTINE", "JAMMING_ROUTINE", "MISSILE_ROUTINE")
        val chosen = routineTypes.random()
        val randomNum = Random.nextInt(100, 999)

        return when (chosen) {
            "UAV_ROUTINE" -> {
                val origin = listOf("المخاء", "الحديدة", "صنعاء").random()
                val targetGov = when (origin) {
                    "المخاء" -> SouthGovernorate.LAHIJ
                    "الحديدة" -> if (Random.nextBoolean()) SouthGovernorate.LAHIJ else SouthGovernorate.ADEN
                    else -> if (Random.nextBoolean()) SouthGovernorate.AD_DALI else SouthGovernorate.SHABWAH
                }
                val sector = when (targetGov) {
                    SouthGovernorate.LAHIJ -> "محور رأس العارة وباب المندب"
                    SouthGovernorate.ADEN -> "القطاع الجوي لميناء وخليج عدن"
                    SouthGovernorate.AD_DALI -> "محور مريس وقعطبة شمال الضالع"
                    else -> "قطاع بيحان وعسيلان"
                }

                ThreatItem(
                    id = "THR-RTN-$randomNum",
                    type = ThreatType.DRONE,
                    level = ThreatLevel.CRITICAL,
                    designation = "UAV-RTN-$randomNum",
                    name = "دورية روتينية: طائرة مسيرة معادية مخترقة للأجواء",
                    latitude = targetGov.defaultLat + (Random.nextDouble() - 0.5) * 0.35,
                    longitude = targetGov.defaultLng + (Random.nextDouble() - 0.5) * 0.35,
                    altitudeMeters = Random.nextInt(1400, 3200),
                    speedKmh = Random.nextInt(180, 240),
                    frequencyBand = "${listOf("2.40 GHz FHSS", "5.8 GHz C2", "915 MHz").random()}",
                    headingDegrees = Random.nextInt(110, 180).toFloat(),
                    governorate = targetGov,
                    sector = sector,
                    detectionSensor = "منظومة الاستطلاع والرصد الروتيني المبكر - $sector",
                    detectedAt = now,
                    isVerified = true,
                    isDecoy = false,
                    launchOrigin = origin,
                    notes = "رصد روتيني مستمر: هدف جوي مسير منطلق من محور ($origin) يخترق قطاع ($sector). تم تفعيل صفارات الإنذار الروتينية."
                )
            }
            "JAMMING_ROUTINE" -> {
                val origin = listOf("الحديدة", "المخاء").random()
                val targetGov = if (origin == "المخاء") SouthGovernorate.LAHIJ else SouthGovernorate.ADEN
                val sector = if (targetGov == SouthGovernorate.LAHIJ) "ممر باب المندب ورأس العارة" else "ساحل البريقة وخليج عدن"

                ThreatItem(
                    id = "THR-RTN-$randomNum",
                    type = ThreatType.JAMMING,
                    level = ThreatLevel.HIGH,
                    designation = "EW-RTN-$randomNum",
                    name = "نشاط تشويش إلكتروني روتيني معادٍ",
                    latitude = targetGov.defaultLat + (Random.nextDouble() - 0.5) * 0.25,
                    longitude = targetGov.defaultLng + (Random.nextDouble() - 0.5) * 0.25,
                    altitudeMeters = 0,
                    speedKmh = 0,
                    frequencyBand = "${listOf("GPS L1/L2 (1575 MHz)", "VHF Tactical Net", "GLONASS L1").random()}",
                    headingDegrees = 0f,
                    governorate = targetGov,
                    sector = sector,
                    detectionSensor = "مرصد الاستطلاع الإشاري الجنوبي - $sector",
                    detectedAt = now,
                    isVerified = true,
                    isDecoy = false,
                    launchOrigin = origin,
                    notes = "تشويش كهرومغناطيسي روتيني مرصود على ترددات الملاحة والاتصالات في قطاع $sector."
                )
            }
            else -> {
                val origin = "صنعاء"
                val targetGov = SouthGovernorate.AD_DALI
                val sector = "قطاع سناح والضالع الأوسط"

                ThreatItem(
                    id = "THR-RTN-$randomNum",
                    type = ThreatType.MISSILE,
                    level = ThreatLevel.CRITICAL,
                    designation = "BML-RTN-$randomNum",
                    name = "إنذار روتيني عاجل: مقذوف باليستي مقترب",
                    latitude = targetGov.defaultLat + (Random.nextDouble() - 0.5) * 0.3,
                    longitude = targetGov.defaultLng + (Random.nextDouble() - 0.5) * 0.3,
                    altitudeMeters = Random.nextInt(9000, 15000),
                    speedKmh = Random.nextInt(2500, 3200),
                    frequencyBand = "توجيه باليستي قصوري",
                    headingDegrees = 175f,
                    governorate = targetGov,
                    sector = sector,
                    detectionSensor = "رادار الإنذار المبكر التكتيكي - شمال الضالع",
                    detectedAt = now,
                    isVerified = true,
                    isDecoy = false,
                    launchOrigin = origin,
                    notes = "مسار باليستي روتيني تم رصده فور انطلاقه من جهة $origin باتجاه قطاع $sector. إنذار فوري لكافة الوحدات."
                )
            }
        }
    }

    fun generateNewServerThreat(): ThreatItem {
        val governorates = SouthGovernorate.entries.toTypedArray()
        val gov = governorates[Random.nextInt(governorates.size)]
        val types = listOf(ThreatType.DRONE, ThreatType.JAMMING, ThreatType.MISSILE)
        val selectedType = types[Random.nextInt(types.size)]

        val (latOffset, lngOffset) = Pair(
            (Random.nextDouble() - 0.5) * 0.4,
            (Random.nextDouble() - 0.5) * 0.4
        )

        val idNum = Random.nextInt(100, 999)
        val sector = gov.keySectors[Random.nextInt(gov.keySectors.size)]
        val origins = listOf("المخاء", "الحديدة", "صنعاء")
        val origin = origins.random()

        return when (selectedType) {
            ThreatType.DRONE -> ThreatItem(
                id = "THR-${System.currentTimeMillis() % 10000}",
                type = ThreatType.DRONE,
                level = if (Random.nextBoolean()) ThreatLevel.CRITICAL else ThreatLevel.HIGH,
                designation = "UAV-${gov.code}-$idNum",
                name = if (Random.nextBoolean()) "مسيرة انتحارية محلقة" else "مسيرة استطلاع وتجسس إلكتروني",
                latitude = gov.defaultLat + latOffset,
                longitude = gov.defaultLng + lngOffset,
                altitudeMeters = Random.nextInt(1200, 4500),
                speedKmh = Random.nextInt(120, 260),
                frequencyBand = "${listOf("2.4 GHz", "5.8 GHz", "915 MHz FHSS").random()}",
                headingDegrees = Random.nextInt(0, 360).toFloat(),
                governorate = gov,
                sector = sector,
                detectionSensor = "منظومة الرصد المبكر - قطاع ${gov.arabicName}",
                detectedAt = System.currentTimeMillis(),
                isVerified = true,
                isDecoy = false,
                launchOrigin = origin,
                notes = "تم استقبال البلاغ فورياً من الخادم المركزي للعمليات. تم تعميم الإنذار على كافة النقاط الميدانية."
            )
            ThreatType.JAMMING -> ThreatItem(
                id = "THR-${System.currentTimeMillis() % 10000}",
                type = ThreatType.JAMMING,
                level = ThreatLevel.HIGH,
                designation = "EW-${gov.code}-$idNum",
                name = "تشويش سيبراني وكهرومغناطيسي معادٍ",
                latitude = gov.defaultLat + latOffset,
                longitude = gov.defaultLng + lngOffset,
                altitudeMeters = 0,
                speedKmh = 0,
                frequencyBand = "${listOf("GPS L1/L2", "VHF/UHF Net", "SATCOM Uplink").random()}",
                headingDegrees = 0f,
                governorate = gov,
                sector = sector,
                detectionSensor = "محطة استطلاع الحرب الإلكترونية - ${gov.arabicName}",
                detectedAt = System.currentTimeMillis(),
                isVerified = true,
                isDecoy = false,
                launchOrigin = origin,
                notes = "رصد طاقة تشويش عالية النطاق تشوش على أنظمة الاتصال والملاحة الميدانية."
            )
            ThreatType.MISSILE -> ThreatItem(
                id = "THR-${System.currentTimeMillis() % 10000}",
                type = ThreatType.MISSILE,
                level = ThreatLevel.CRITICAL,
                designation = "BML-${gov.code}-$idNum",
                name = "مقذوف باليستي / صاروخ موجه",
                latitude = gov.defaultLat + latOffset,
                longitude = gov.defaultLng + lngOffset,
                altitudeMeters = Random.nextInt(8000, 16000),
                speedKmh = Random.nextInt(2200, 3400),
                frequencyBand = "توجيه راداري ورقمي",
                headingDegrees = Random.nextInt(0, 360).toFloat(),
                governorate = gov,
                sector = sector,
                detectionSensor = "رادار الإنذار المبكر المركزي - قطاع ${gov.arabicName}",
                detectedAt = System.currentTimeMillis(),
                isVerified = true,
                isDecoy = false,
                launchOrigin = origin,
                notes = "إنذار طوارئ قصوى: هدف باليستي مقترب بسرعة عالية. صفارات الإنذار تعمل تلقائياً."
            )
            else -> ThreatItem(
                id = "THR-${System.currentTimeMillis() % 10000}",
                type = ThreatType.SURVEILLANCE_RADAR,
                level = ThreatLevel.MEDIUM,
                designation = "SIG-${gov.code}-$idNum",
                name = "إشعاع راداري تكتيكي غير معروف",
                latitude = gov.defaultLat + latOffset,
                longitude = gov.defaultLng + lngOffset,
                altitudeMeters = 50,
                speedKmh = 0,
                frequencyBand = "8.5 - 10 GHz",
                headingDegrees = 0f,
                governorate = gov,
                sector = sector,
                detectionSensor = "مرصد الاستخبارات الإلكترونية",
                detectedAt = System.currentTimeMillis(),
                isVerified = false,
                isDecoy = false,
                launchOrigin = origin,
                notes = "إشارات رادارية كهرومغناطيسية مشبوهة قيد الفحص والتحليل السيبراني."
            )
        }
    }
}
