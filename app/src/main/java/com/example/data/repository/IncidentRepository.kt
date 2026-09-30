package com.example.data.repository

import com.example.data.dao.IncidentDao
import com.example.data.entity.IncidentEntity
import com.example.data.model.SouthGovernorate
import com.example.data.model.ThreatItem
import com.example.data.model.ThreatLevel
import com.example.data.model.ThreatType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class IncidentRepository(private val dao: IncidentDao) {

    val allIncidents: Flow<List<IncidentEntity>> = dao.getAllIncidents()

    fun getIncidentsByGovernorate(gov: String): Flow<List<IncidentEntity>> {
        return if (gov.isBlank() || gov == "الكل") dao.getAllIncidents() else dao.getIncidentsByGovernorate(gov)
    }

    fun getIncidentsByType(type: String): Flow<List<IncidentEntity>> {
        return if (type.isBlank() || type == "الكل") dao.getAllIncidents() else dao.getIncidentsByType(type)
    }

    suspend fun insert(incident: IncidentEntity): Long = dao.insertIncident(incident)

    suspend fun insertFromThreat(threat: ThreatItem, operatorNote: String = ""): Long {
        val entity = IncidentEntity(
            targetCode = threat.designation,
            threatType = threat.type.name,
            threatLevel = threat.level.name,
            designation = threat.name,
            governorate = threat.governorate.arabicName,
            sector = threat.sector,
            latitude = threat.latitude,
            longitude = threat.longitude,
            altitudeMeters = threat.altitudeMeters,
            speedKmh = threat.speedKmh,
            frequencyBand = threat.frequencyBand,
            timestamp = threat.detectedAt,
            detectionSensor = threat.detectionSensor,
            operatorNotes = if (operatorNote.isNotBlank()) operatorNote else threat.notes,
            isResolved = false
        )
        return dao.insertIncident(entity)
    }

    suspend fun updateNotes(id: Long, notes: String) = dao.updateNotes(id, notes)

    suspend fun updateStatus(id: Long, isResolved: Boolean) = dao.updateStatus(id, isResolved)

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun seedInitialIncidentsIfEmpty() {
        val existing = dao.getAllIncidents().first()
        if (existing.isEmpty()) {
            val now = System.currentTimeMillis()
            val sampleIncidents = listOf(
                IncidentEntity(
                    targetCode = "UAV-ADN-409",
                    threatType = ThreatType.DRONE.name,
                    threatLevel = ThreatLevel.CRITICAL.name,
                    designation = "طيران مسير انتحاري قاصف مجنح",
                    governorate = SouthGovernorate.ADEN.arabicName,
                    sector = "القطاع الجوي الغربي - البريقة وميناء الزيت",
                    latitude = 12.78,
                    longitude = 44.92,
                    altitudeMeters = 1850,
                    speedKmh = 190,
                    frequencyBand = "2.40 GHz / FHSS",
                    timestamp = now - (12 * 60 * 1000),
                    detectionSensor = "رادار جبل شمسان - محطة الرصد الكهروبصري",
                    operatorNotes = "تم رصد الهدف مقترباً من الممر الملاحي لخليج عدن. مسار متعرج للتمويه الراداري. تم إطلاق صفارات الإنذار المبكر للمنشآت الحيوية.",
                    isResolved = false
                ),
                IncidentEntity(
                    targetCode = "EW-JAM-033",
                    threatType = ThreatType.JAMMING.name,
                    threatLevel = ThreatLevel.HIGH.name,
                    designation = "عملية تشويش إلكتروني مكثف على أنظمة الملاحة GPS",
                    governorate = SouthGovernorate.LAHIJ.arabicName,
                    sector = "رأس العارة - الممر الملاحي لباب المندب",
                    latitude = 12.65,
                    longitude = 43.60,
                    altitudeMeters = 0,
                    speedKmh = 0,
                    frequencyBand = "GPS L1 (1575.42 MHz) + GLONASS L1",
                    timestamp = now - (35 * 60 * 1000),
                    detectionSensor = "محطة استطلاع الحرب الإلكترونية - رأس العارة",
                    operatorNotes = "تشويش واسع النطاق يغطي دائرة نصف قطرها 35 كم. فقدان إشارات الاستغاثة البحرية والملاحة الجوية. تم توجيه البلاغ للغرفة المشتركة.",
                    isResolved = false
                ),
                IncidentEntity(
                    targetCode = "BML-SHB-712",
                    threatType = ThreatType.MISSILE.name,
                    threatLevel = ThreatLevel.CRITICAL.name,
                    designation = "صاروخ باليستي متوسط المدى قيد التتبع",
                    governorate = SouthGovernorate.SHABWAH.arabicName,
                    sector = "قطاع عسيلان وبيحان - شمال غرب شبوة",
                    latitude = 14.88,
                    longitude = 45.75,
                    altitudeMeters = 12400,
                    speedKmh = 2850,
                    frequencyBand = "توجيه قصوري + راداري نشط",
                    timestamp = now - (6 * 60 * 1000),
                    detectionSensor = "رادار الإنذار المبكر - محطة عتق التكتيكية",
                    operatorNotes = "رصد بصمة حرارية ومسار باليستي منطلق بسرعة Mach 2.4. زاوية السمت 135 درجة باتجاه الجنوب الشرقي. جاري إنذار الدفاع المدني والمستشفيات الميدانية.",
                    isResolved = false
                ),
                IncidentEntity(
                    targetCode = "UAV-HAD-118",
                    threatType = ThreatType.DRONE.name,
                    threatLevel = ThreatLevel.HIGH.name,
                    designation = "طائرتان مسيرتان للاستطلاع والتجسس الإلكتروني",
                    governorate = SouthGovernorate.HADRAMAWT.arabicName,
                    sector = "ساحل حضرموت - محيط ميناء الضبة والشحر",
                    latitude = 14.71,
                    longitude = 49.52,
                    altitudeMeters = 3200,
                    speedKmh = 140,
                    frequencyBand = "5.8 GHz C2 Link + SATCOM",
                    timestamp = now - (68 * 60 * 1000),
                    detectionSensor = "رادار الدفاع الساحلي - المكلا والشحر",
                    operatorNotes = "تحليق دائري بنمط مسح استخباري لرصد المنشآت النفطية والموانئ الساحلية. تم توثيق الترددات ورفعها للمديرية.",
                    isResolved = false
                ),
                IncidentEntity(
                    targetCode = "EW-DAL-091",
                    threatType = ThreatType.JAMMING.name,
                    threatLevel = ThreatLevel.MEDIUM.name,
                    designation = "حزم تشويش ترددية متقطعة على الاتصالات اللاسلكية VHF",
                    governorate = SouthGovernorate.AD_DALI.arabicName,
                    sector = "جبهة مريس - قعطبة وشمال الضالع",
                    latitude = 13.84,
                    longitude = 44.72,
                    altitudeMeters = 0,
                    speedKmh = 0,
                    frequencyBand = "136-174 MHz VHF Tactical Net",
                    timestamp = now - (120 * 60 * 1000),
                    detectionSensor = "مرصد الاستطلاع الإشاري - سناح والضالع",
                    operatorNotes = "تشويش ضوضائي متقطع يستهدف شبكات التنسيق العسكرية الميدانية. تم تحويل الاتصالات إلى الترددات المشفرة البديلة.",
                    isResolved = true
                )
            )
            dao.insertAll(sampleIncidents)
        }
    }
}
