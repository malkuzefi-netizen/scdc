package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.IncidentEntity
import com.example.data.entity.TacticalMessageEntity
import com.example.data.entity.UserAccountEntity
import com.example.data.model.SouthGovernorate
import com.example.data.model.ThreatItem
import com.example.data.model.ThreatType
import com.example.data.repository.IncidentRepository
import com.example.data.repository.FirebaseMessagingRepository
import com.example.data.service.CentralOpsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenState {
    object TacticalMap : ScreenState()
    object IncidentLog : ScreenState()
    object CounterJamming : ScreenState()
    object TacticalMessaging : ScreenState()
    object IntelligenceReport : ScreenState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val incidentDao = database.incidentDao()
    private val userAccountDao = database.userAccountDao()
    private val tacticalMessageDao = database.tacticalMessageDao()
    private val repository = IncidentRepository(incidentDao)
    private val messagingRepository = FirebaseMessagingRepository(application, tacticalMessageDao)

    // User authentication state
    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Accounts list (observed by Commander)
    val allAccounts: StateFlow<List<UserAccountEntity>> = userAccountDao.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incidents: StateFlow<List<IncidentEntity>> = repository.allIncidents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _messagingConnectionStatus = MutableStateFlow(if (messagingRepository.isConfigured) "CONNECTING" else "NOT_CONFIGURED")
    val messagingConnectionStatus: StateFlow<String> = _messagingConnectionStatus.asStateFlow()

    // In-app tactical messaging & dispatches
    val messages: StateFlow<List<TacticalMessageEntity>> = tacticalMessageDao.getAllMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Counter-Jamming (ECM) Interactive State ---
    private val _isCounterJammingActive = MutableStateFlow(false)
    val isCounterJammingActive: StateFlow<Boolean> = _isCounterJammingActive.asStateFlow()

    private val _jammingPowerWatts = MutableStateFlow(850)
    val jammingPowerWatts: StateFlow<Int> = _jammingPowerWatts.asStateFlow()

    private val _selectedEcmMode = MutableStateFlow("إخماد روابط التحكم C2 بالمسيرات")
    val selectedEcmMode: StateFlow<String> = _selectedEcmMode.asStateFlow()

    private val _selectedTargetSector = MutableStateFlow("العاصمة عدن ومحيطها")
    val selectedTargetSector: StateFlow<String> = _selectedTargetSector.asStateFlow()

    private val _selectedFrequencyBand = MutableStateFlow("2.4 GHz / 5.8 GHz C2")
    val selectedFrequencyBand: StateFlow<String> = _selectedFrequencyBand.asStateFlow()

    private val _lastEmpPulseTimestamp = MutableStateFlow(0L)
    val lastEmpPulseTimestamp: StateFlow<Long> = _lastEmpPulseTimestamp.asStateFlow()

    private val _activeThreats = MutableStateFlow<List<ThreatItem>>(emptyList())
    val activeThreats: StateFlow<List<ThreatItem>> = _activeThreats.asStateFlow()

    private val _selectedThreat = MutableStateFlow<ThreatItem?>(null)
    val selectedThreat: StateFlow<ThreatItem?> = _selectedThreat.asStateFlow()

    private val _threatFilter = MutableStateFlow<ThreatType?>(null)
    val threatFilter: StateFlow<ThreatType?> = _threatFilter.asStateFlow()

    private val _currentScreen = MutableStateFlow<ScreenState>(ScreenState.TacticalMap)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _isLiveStreamEnabled = MutableStateFlow(true)
    val isLiveStreamEnabled: StateFlow<Boolean> = _isLiveStreamEnabled.asStateFlow()

    private val _systemAlertBanner = MutableStateFlow<String?>(null)
    val systemAlertBanner: StateFlow<String?> = _systemAlertBanner.asStateFlow()

    private var liveStreamJob: Job? = null

    init {
        // Initialize Master Account for Commander Mohsen Al-Quzefi, seed incidents & initial tactical messages
        viewModelScope.launch {
            seedMasterAccountIfNeeded()
            repository.seedInitialIncidentsIfEmpty()
            if (messagingRepository.isConfigured) {
                val connected = messagingRepository.startRealtimeSync(viewModelScope) { error ->
                    _messagingConnectionStatus.value = "ERROR: $error"
                }
                _messagingConnectionStatus.value = if (connected) "CONNECTED" else "ERROR"
            }
        }

        // Initialize active threats
        _activeThreats.value = CentralOpsService.getInitialTacticalThreats()

        // Start routine automated threat stream
        startLiveThreatStream()
    }

    private suspend fun seedInitialTacticalCommsIfEmpty() {
        if (tacticalMessageDao.getMessageCount() == 0) {
            val now = System.currentTimeMillis()
            val initialDispatches = listOf(
                TacticalMessageEntity(
                    senderId = 1L,
                    senderName = "محسن القزيفي",
                    senderRank = "قائد وحدة استطلاع",
                    senderRole = "COMMANDER",
                    receiverName = "غرفة العمليات المشتركة (تعميم عام)",
                    channel = "OPS_ROOM",
                    classification = "سري للغاية",
                    content = "تعميم عملياتي عاجل لكافة القطاعات: تفعيل نظام الرصد الروتيني المستمر 24/7 ورفع الجاهزية القتالية لوحدات الاستطلاع والاستخبارات على طول الشريط الساحلي ومحاور التماس.",
                    timestamp = now - (25 * 60 * 1000),
                    isUrgent = true,
                    cipherCode = "SEC-101"
                ),
                TacticalMessageEntity(
                    senderId = 2L,
                    senderName = "سالم اليافعي",
                    senderRank = "عقيد ركن",
                    senderRole = "OFFICER",
                    receiverName = "قيادة وحدة الاستطلاع - عدن",
                    channel = "UAV_RECON",
                    classification = "عملياتي عاجل",
                    content = "إشارة من محور رأس العارة وباب المندب: تم رصد تحليق مسيرة استطلاع معادية قادمة من اتجاه المخاء على ارتفاع 1900م، طواقم الرصد تتابع المسار بدقة.",
                    timestamp = now - (15 * 60 * 1000),
                    isUrgent = false,
                    cipherCode = "SEC-102"
                ),
                TacticalMessageEntity(
                    senderId = 1L,
                    senderName = "محسن القزيفي",
                    senderRank = "قائد وحدة استطلاع",
                    senderRole = "COMMANDER",
                    receiverName = "محور رأس العارة وباب المندب",
                    channel = "UAV_RECON",
                    classification = "سري للغاية",
                    content = "مستلم. استمروا في التتبع الكهروبصري وشاركوا الإحداثيات فورياً مع قاعدة العند والقطاع الجوي للعاصمة عدن.",
                    timestamp = now - (10 * 60 * 1000),
                    isUrgent = false,
                    cipherCode = "SEC-103"
                ),
                TacticalMessageEntity(
                    senderId = 3L,
                    senderName = "منظومة الاستطلاع الإشاري",
                    senderRank = "نقيب",
                    senderRole = "OFFICER",
                    receiverName = "غرفة العمليات المشتركة (تعميم عام)",
                    channel = "AIR_DEFENSE",
                    classification = "إشارة استخباراتية",
                    content = "رصد طيف تشويش إلكتروني معادٍ بنطاق 1.5 GHz يستهدف حزمة الملاحة البحرية في الممر الدولي. الفرق السيبرانية تعمل على التحييد.",
                    timestamp = now - (4 * 60 * 1000),
                    isUrgent = true,
                    cipherCode = "SEC-104"
                )
            )
            initialDispatches.forEach { tacticalMessageDao.insertMessage(it) }
        }
    }

    private suspend fun seedMasterAccountIfNeeded() {
        val master = userAccountDao.findByUsernameOrName("mohsen")
            ?: userAccountDao.findByUsernameOrName("محسن القزيفي")

        if (master == null) {
            val commander = UserAccountEntity(
                username = "mohsen",
                fullName = "محسن القزيفي",
                title = "قائد وحدة الاستطلاع والمسيرات في عدن",
                password = "123",
                militaryRank = "قائد وحدة استطلاع",
                role = "COMMANDER",
                assignedSector = "العاصمة عدن - مقر قيادة الاستطلاع المشترك",
                isMasterAdmin = true
            )
            userAccountDao.insertAccount(commander)
        } else if (master.militaryRank != "قائد وحدة استطلاع") {
            val updated = master.copy(militaryRank = "قائد وحدة استطلاع")
            userAccountDao.updateAccount(updated)
            if (_currentUser.value?.id == master.id || _currentUser.value?.isMasterAdmin == true) {
                _currentUser.value = updated
            }
        }
    }

    // --- Authentication Actions ---
    fun login(usernameOrName: String, pass: String): Boolean {
        _loginError.value = null
        val trimmedUser = usernameOrName.trim()
        val trimmedPass = pass.trim()

        if (trimmedUser.isBlank() || trimmedPass.isBlank()) {
            _loginError.value = "يرجى إدخال اسم المستخدم أو الاسم وكلمة السر"
            return false
        }

        viewModelScope.launch {
            // Check direct match
            var account = userAccountDao.authenticate(trimmedUser, trimmedPass)
            // Case-insensitive or name match fallback
            if (account == null) {
                val candidate = userAccountDao.findByUsernameOrName(trimmedUser)
                if (candidate != null && candidate.password == trimmedPass) {
                    account = candidate
                }
            }

            if (account != null) {
                _currentUser.value = account
                _loginError.value = null
                _systemAlertBanner.value = "مرحباً بالـ ${account.militaryRank} ${account.fullName} - ${account.title}"
                triggerVibrationAlert()
                delay(3500)
                if (_systemAlertBanner.value?.contains(account.fullName) == true) {
                    _systemAlertBanner.value = null
                }
            } else {
                _loginError.value = "بيانات الدخول غير صحيحة. تحقق من اسم المستخدم أو كلمة السر."
            }
        }
        return true
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
        _selectedThreat.value = null
    }

    fun createPersonnelAccount(
        username: String,
        fullName: String,
        title: String,
        password: String,
        militaryRank: String,
        role: String,
        assignedSector: String
    ): Boolean {
        if (username.isBlank() || fullName.isBlank() || password.isBlank()) {
            return false
        }
        viewModelScope.launch {
            val newAccount = UserAccountEntity(
                username = username.trim().lowercase(),
                fullName = fullName.trim(),
                title = title.trim(),
                password = password.trim(),
                militaryRank = militaryRank.trim(),
                role = role.trim(),
                assignedSector = assignedSector.trim(),
                isMasterAdmin = false
            )
            userAccountDao.insertAccount(newAccount)
            _systemAlertBanner.value = "تم إنشاء حساب عسكري جديد بنجاح: ${newAccount.militaryRank} ${newAccount.fullName}"
            delay(3500)
            _systemAlertBanner.value = null
        }
        return true
    }

    fun deletePersonnelAccount(id: Long) {
        viewModelScope.launch {
            userAccountDao.deleteAccount(id)
            _systemAlertBanner.value = "تم إلغاء صلاحية الحساب وحذفه من السجلات"
            delay(2500)
            _systemAlertBanner.value = null
        }
    }

    fun updatePersonnelAccount(
        id: Long,
        username: String,
        fullName: String,
        title: String,
        password: String,
        militaryRank: String,
        role: String,
        assignedSector: String
    ): Boolean {
        if (username.isBlank() || fullName.isBlank() || password.isBlank()) {
            return false
        }
        viewModelScope.launch {
            val existing = userAccountDao.getAccountById(id) ?: return@launch
            val updated = existing.copy(
                username = username.trim().lowercase(),
                fullName = fullName.trim(),
                title = title.trim(),
                password = password.trim(),
                militaryRank = militaryRank.trim(),
                role = role.trim(),
                assignedSector = assignedSector.trim()
            )
            userAccountDao.updateAccount(updated)
            if (_currentUser.value?.id == id) {
                _currentUser.value = updated
            }
            _systemAlertBanner.value = "تم تحديث بيانات الحساب العسكري: ${updated.militaryRank} ${updated.fullName}"
            delay(3000)
            _systemAlertBanner.value = null
        }
        return true
    }

    // --- Counter-Jamming (ECM) Interactive Controls ---
    fun toggleCounterJamming() {
        val newState = !_isCounterJammingActive.value
        _isCounterJammingActive.value = newState
        triggerVibrationAlert()
        _systemAlertBanner.value = if (newState) {
            "تم تفعيل منظومة التشويش الإلكتروني العكسي بنجاح: بث طاقة الإخماد نشط (${_jammingPowerWatts.value}W)"
        } else {
            "تم إيقاف منظومة التشويش العكسي: النظام في وضع الاستعداد السلبي"
        }
        viewModelScope.launch {
            delay(3500)
            _systemAlertBanner.value = null
        }
    }

    fun setJammingPower(watts: Int) {
        _jammingPowerWatts.value = watts.coerceIn(50, 2000)
    }

    fun setEcmMode(mode: String) {
        _selectedEcmMode.value = mode
    }

    fun setTargetSector(sector: String) {
        _selectedTargetSector.value = sector
    }

    fun setFrequencyBand(band: String) {
        _selectedFrequencyBand.value = band
    }

    fun triggerHighPowerEmpPulse() {
        _lastEmpPulseTimestamp.value = System.currentTimeMillis()
        triggerVibrationAlert()
        _systemAlertBanner.value = "⚡ تم إطلاق نبضة كهرومغناطيسية قصوى بقوة ${_jammingPowerWatts.value * 2}W! إخماد فوري للإشارات المعادية."
        viewModelScope.launch {
            delay(4000)
            _systemAlertBanner.value = null
        }
    }

    // --- Fast One-Click Radar & Threat Actions ---

    // 1. One-click clear all active alerts (حذف الإنذارات بضغطة واحدة)
    fun clearAllActiveThreats() {
        val count = _activeThreats.value.size
        _activeThreats.value = emptyList()
        _selectedThreat.value = null
        _systemAlertBanner.value = "تم مسح وإلغاء كافة الإنذارات النشطة ($count هدف) بضغطة واحدة."
        triggerVibrationAlert()
        viewModelScope.launch {
            delay(3000)
            _systemAlertBanner.value = null
        }
    }

    // 2. One-click clean radar of spoofed / decoy targets (تنظيف الرادارات من الأهداف المخادعة والوهمية)
    fun purgeDecoysAndGhostTargets() {
        val currentList = _activeThreats.value
        val decoys = currentList.filter { it.isDecoy || !it.isVerified || it.designation.contains("DECOY", ignoreCase = true) || it.designation.contains("GHOST", ignoreCase = true) }
        val genuineTargets = currentList.filterNot { it.isDecoy || !it.isVerified || it.designation.contains("DECOY", ignoreCase = true) || it.designation.contains("GHOST", ignoreCase = true) }

        _activeThreats.value = genuineTargets
        if (_selectedThreat.value?.isDecoy == true) {
            _selectedThreat.value = null
        }

        val purgedCount = decoys.size
        _systemAlertBanner.value = if (purgedCount > 0) {
            "تم تنفيذ مسح كهرومغناطيسي سيبراني: تنظيف الرادار من $purgedCount أهداف مخادعة ووهمية بنجاح."
        } else {
            "فحص سيبراني دقيق: الرادار خالٍ من الأهداف المخادعة، كافة الأهداف الحالية مؤكدة."
        }
        triggerVibrationAlert()
        viewModelScope.launch {
            delay(3500)
            _systemAlertBanner.value = null
        }
    }

    // 3. System Reset: wipe all radar tracks and incident database to factory clean state (إعادة تهيئة النظام خالي من البيانات)
    fun resetSystemToCleanState() {
        _activeThreats.value = emptyList()
        _selectedThreat.value = null
        viewModelScope.launch {
            incidentDao.clearAll()
            _systemAlertBanner.value = "تمت إعادة تهيئة النظام بالكامل: النظام نظيف وخالٍ من أي بيانات سابقة."
            triggerVibrationAlert()
            delay(3500)
            _systemAlertBanner.value = null
        }
    }

    // 4. Routine Automated Attack Sweep (الهجمات روتينية ومستمرة تلقائياً)
    fun triggerRoutineRadarSweep() {
        val routineThreat = CentralOpsService.generateRoutineAttack()
        _activeThreats.value = (listOf(routineThreat) + _activeThreats.value).take(15)
        _selectedThreat.value = routineThreat
        _systemAlertBanner.value = "⚠️ رصد روتيني: ${routineThreat.name} في ${routineThreat.sector} (${routineThreat.governorate.arabicName})"

        viewModelScope.launch {
            repository.insertFromThreat(routineThreat, "رصد روتيني ميداني دوري - مستمر")
            triggerVibrationAlert()
            delay(4500)
            if (_systemAlertBanner.value?.contains(routineThreat.designation) == true) {
                _systemAlertBanner.value = null
            }
        }
    }

    // --- Firebase-backed real-time messaging actions ---
    fun sendTacticalMessage(
        channel: String,
        content: String,
        receiverId: Long?,
        receiverName: String,
        classification: String,
        isUrgent: Boolean
    ) {
        val user = _currentUser.value ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            val message = TacticalMessageEntity(
                senderId = user.id,
                senderName = user.fullName,
                senderRank = user.militaryRank,
                senderRole = user.role,
                receiverId = receiverId,
                receiverName = receiverName,
                channel = channel,
                classification = classification,
                content = content.trim(),
                timestamp = System.currentTimeMillis(),
                isUrgent = isUrgent,
                cipherCode = "SEC-${(1000..9999).random()}",
                syncState = "PENDING"
            )
            val result = messagingRepository.send(message)
            triggerVibrationAlert()
            if (result.isSuccess) {
                _messagingConnectionStatus.value = "CONNECTED"
                _systemAlertBanner.value = "تم إرسال البرقية [${message.cipherCode}] إلى $receiverName"
            } else {
                _messagingConnectionStatus.value = "OFFLINE"
                _systemAlertBanner.value = "تم حفظ البرقية محلياً وسيُعاد إرسالها عند عودة الاتصال"
            }
            delay(3000)
            _systemAlertBanner.value = null
        }
    }

    fun deleteTacticalMessage(id: Long) {
        viewModelScope.launch {
            val message = tacticalMessageDao.findById(id) ?: return@launch
            val result = messagingRepository.delete(message)
            _systemAlertBanner.value = if (result.isSuccess) {
                "تم حذف البرقية من جميع الأجهزة"
            } else {
                "تعذر حذف البرقية من الخادم حالياً"
            }
            delay(2000)
            _systemAlertBanner.value = null
        }
    }

    fun clearAllTacticalMessages() {
        viewModelScope.launch {
            val result = messagingRepository.clearAll()
            _systemAlertBanner.value = if (result.isSuccess) {
                "تم إفراغ أرشيف البرقيات المتزامن بالكامل"
            } else {
                "تعذر مسح الأرشيف من الخادم حالياً"
            }
            delay(2000)
            _systemAlertBanner.value = null
        }
    }

    // --- Standard Actions ---
    fun selectThreat(threat: ThreatItem?) {
        _selectedThreat.value = threat
    }

    fun setFilter(filter: ThreatType?) {
        _threatFilter.value = filter
    }

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
    }

    fun triggerVibrationAlert() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 180, 100, 180), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 180, 100, 180), -1)
            }
        } catch (_: Exception) {}
    }

    fun addManualThreat(threat: ThreatItem) {
        _activeThreats.value = listOf(threat) + _activeThreats.value
        _selectedThreat.value = threat
        _systemAlertBanner.value = "تم إدراج هدف رصد يدوي بنجاح: ${threat.designation} (${threat.governorate.arabicName})"

        viewModelScope.launch {
            repository.insertFromThreat(threat, threat.notes)
            triggerVibrationAlert()
            delay(4000)
            if (_systemAlertBanner.value?.contains(threat.designation) == true) {
                _systemAlertBanner.value = null
            }
        }
    }

    fun fetchNewServerAlert() {
        val newThreat = CentralOpsService.generateNewServerThreat()
        _activeThreats.value = listOf(newThreat) + _activeThreats.value
        _systemAlertBanner.value = "إشعار وارد من الخادم المركزي: ${newThreat.designation} في ${newThreat.governorate.arabicName}"

        viewModelScope.launch {
            repository.insertFromThreat(newThreat, "وارد تلقائياً من الخادم المركزي للعمليات والاستطلاع")
            triggerVibrationAlert()
            delay(4000)
            if (_systemAlertBanner.value?.contains(newThreat.designation) == true) {
                _systemAlertBanner.value = null
            }
        }
    }

    fun toggleLiveStream() {
        val newState = !_isLiveStreamEnabled.value
        _isLiveStreamEnabled.value = newState
        if (newState) {
            startLiveThreatStream()
        } else {
            liveStreamJob?.cancel()
            liveStreamJob = null
        }
    }

    private fun startLiveThreatStream() {
        liveStreamJob?.cancel()
        liveStreamJob = viewModelScope.launch {
            while (_isLiveStreamEnabled.value) {
                delay(28000)
                if (_isLiveStreamEnabled.value) {
                    val incoming = CentralOpsService.generateRoutineAttack()
                    _activeThreats.value = (listOf(incoming) + _activeThreats.value).take(15)
                    repository.insertFromThreat(incoming, "رصد روتيني ميداني دوري - تحديث آلي مستمر")
                }
            }
        }
    }

    fun saveThreatToIncidentLog(threat: ThreatItem) {
        viewModelScope.launch {
            repository.insertFromThreat(threat, "تم التوثيق اليدوي بواسطة الضابط المناوب في مركز الرصد والإنذار")
            _systemAlertBanner.value = "تم توثيق الهدف ${threat.designation} في سجل البلاغات"
            delay(3000)
            _systemAlertBanner.value = null
        }
    }

    fun updateIncidentNotes(id: Long, notes: String) {
        viewModelScope.launch {
            repository.updateNotes(id, notes)
            _systemAlertBanner.value = "تم تحديث ملاحظات الضابط بنجاح"
            delay(2500)
            _systemAlertBanner.value = null
        }
    }

    fun toggleIncidentResolved(id: Long, isResolved: Boolean) {
        viewModelScope.launch {
            repository.updateStatus(id, isResolved)
        }
    }

    fun clearBanner() {
        _systemAlertBanner.value = null
    }

    override fun onCleared() {
        messagingRepository.stop()
        super.onCleared()
    }

}
