package com.example.kipacalculator.ui.main



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavKey
import java.util.Locale

fun String.toCleanDoubleOrNull(): Double? = this.replace(',', '.').trim().toDoubleOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    // Выбранные пресеты
    var sigType by remember { mutableStateOf("4-20 мА") }
    var physPreset by remember { mutableStateOf("0-10 бар") }
    var freqPreset by remember { mutableStateOf("0-50 Гц") }

    // Ручные пределы шкал (редактируемые текстовые поля)
    var textSigMin by remember { mutableStateOf("4.0") }
    var textSigMax by remember { mutableStateOf("20.0") }
    var sigUnit by remember { mutableStateOf("мА") }

    var textPhysMin by remember { mutableStateOf("0.0") }
    var textPhysMax by remember { mutableStateOf("10.0") }
    var physUnit by remember { mutableStateOf("бар") }

    var textFreqMin by remember { mutableStateOf("0.0") }
    var textFreqMax by remember { mutableStateOf("50.0") }

    // Рабочая точка в процентах (0..100)
    var currentPercent by remember { mutableDoubleStateOf(50.0) }

    // Четыре основных отображаемых поля
    var textSignal by remember { mutableStateOf("12.000") }
    var textPercent by remember { mutableStateOf("50.00") }
    var textPhys by remember { mutableStateOf("5.000") }
    var textFreq by remember { mutableStateOf("25.00") }

    // Состояние диалогов настройки частотников
    var showNvf2gDialog by remember { mutableStateOf(false) }
    var showDeltaDialog by remember { mutableStateOf(false) }
    var showDanfossDialog by remember { mutableStateOf(false) }
    var showSeries900Dialog by remember { mutableStateOf(false) }

    // Вспомогательные функции пересчета
    fun getLimits(): Pair<DoubleArray, Array<String>> {
        val sMin = textSigMin.toCleanDoubleOrNull() ?: 4.0
        val sMax = textSigMax.toCleanDoubleOrNull() ?: 20.0
        val pMin = textPhysMin.toCleanDoubleOrNull() ?: 0.0
        val pMax = textPhysMax.toCleanDoubleOrNull() ?: 10.0
        val fMin = textFreqMin.toCleanDoubleOrNull() ?: 0.0
        val fMax = textFreqMax.toCleanDoubleOrNull() ?: 50.0
        return Pair(doubleArrayOf(sMin, sMax, pMin, pMax, fMin, fMax), arrayOf(sigUnit, physUnit))
    }

    fun updateAllFromPercent(pct: Double, updatePctField: Boolean = true) {
        currentPercent = pct
        val (limits, _) = getLimits()
        val r = pct / 100.0

        val sig = limits[0] + r * (limits[1] - limits[0])
        val phys = limits[2] + r * (limits[3] - limits[2])
        val freq = limits[4] + r * (limits[5] - limits[4])

        textSignal = String.format(Locale.US, "%.3f", sig)
        textPhys = String.format(Locale.US, "%.3f", phys)
        textFreq = String.format(Locale.US, "%.2f", freq)
        if (updatePctField) {
            textPercent = String.format(Locale.US, "%.2f", pct)
        }
    }

    fun updateAllFromSignal(sig: Double) {
        val (limits, _) = getLimits()
        val span = limits[1] - limits[0]
        val pct = if (span != 0.0) ((sig - limits[0]) / span) * 100.0 else 0.0
        currentPercent = pct
        val r = pct / 100.0

        val phys = limits[2] + r * (limits[3] - limits[2])
        val freq = limits[4] + r * (limits[5] - limits[4])

        textPercent = String.format(Locale.US, "%.2f", pct)
        textPhys = String.format(Locale.US, "%.3f", phys)
        textFreq = String.format(Locale.US, "%.2f", freq)
    }

    fun updateAllFromPhys(phys: Double) {
        val (limits, _) = getLimits()
        val span = limits[3] - limits[2]
        val pct = if (span != 0.0) ((phys - limits[2]) / span) * 100.0 else 0.0
        currentPercent = pct
        val r = pct / 100.0

        val sig = limits[0] + r * (limits[1] - limits[0])
        val freq = limits[4] + r * (limits[5] - limits[4])

        textSignal = String.format(Locale.US, "%.3f", sig)
        textPercent = String.format(Locale.US, "%.2f", pct)
        textFreq = String.format(Locale.US, "%.2f", freq)
    }

    fun updateAllFromFreq(freq: Double) {
        val (limits, _) = getLimits()
        val span = limits[5] - limits[4]
        val pct = if (span != 0.0) ((freq - limits[4]) / span) * 100.0 else 0.0
        currentPercent = pct
        val r = pct / 100.0

        val sig = limits[0] + r * (limits[1] - limits[0])
        val phys = limits[2] + r * (limits[3] - limits[2])

        textSignal = String.format(Locale.US, "%.3f", sig)
        textPercent = String.format(Locale.US, "%.2f", pct)
        textPhys = String.format(Locale.US, "%.3f", phys)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "⚡ КИПиА 4-20 мА & ЧРП",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // КНОПКИ ПОМОЩНИКОВ НАСТРОЙКИ ЧРП (2 ряда по 2 кнопки)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { showNvf2gDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Chint NVF2G",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showDeltaDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Delta VFD-B",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { showDanfossDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Danfoss FC302",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { showSeries900Dialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Серия 900 / X9",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // ================= 1. НАСТРОЙКА ДИАПАЗОНОВ =================
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "⚙️ Параметры шкалы (настраиваемые)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 1.1. ДИАПАЗОН СИГНАЛА
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("1. Сигнал датчика:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("4-20 мА", "0-20 мА", "0-10 В", "2-10 В").forEach { p ->
                                FilterChip(
                                    selected = sigType == p,
                                    onClick = {
                                        sigType = p
                                        when (p) {
                                            "4-20 мА" -> { textSigMin = "4.0"; textSigMax = "20.0"; sigUnit = "мА" }
                                            "0-20 мА" -> { textSigMin = "0.0"; textSigMax = "20.0"; sigUnit = "мА" }
                                            "0-10 В" -> { textSigMin = "0.0"; textSigMax = "10.0"; sigUnit = "В" }
                                            "2-10 В" -> { textSigMin = "2.0"; textSigMax = "10.0"; sigUnit = "В" }
                                        }
                                        updateAllFromPercent(currentPercent)
                                    },
                                    label = { Text(p, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Поля ручного ввода Мин / Макс
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Мин:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            CompactNumberInput(
                                value = textSigMin,
                                onValueChange = {
                                    textSigMin = it
                                    sigType = "Свой"
                                    updateAllFromPercent(currentPercent)
                                },
                                modifier = Modifier.width(72.dp)
                            )
                            Text("—", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text("Макс:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            CompactNumberInput(
                                value = textSigMax,
                                onValueChange = {
                                    textSigMax = it
                                    sigType = "Свой"
                                    updateAllFromPercent(currentPercent)
                                },
                                modifier = Modifier.width(72.dp)
                            )
                            Text(sigUnit, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // 1.2. ДИАПАЗОН ФИЗИЧЕСКОЙ ВЕЛИЧИНЫ
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("2. Физическая величина:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("0-10 бар", "0-16 бар", "0-6 бар", "0-25 бар", "0-100 °C", "-50..150 °C", "0-5 м").forEach { p ->
                                FilterChip(
                                    selected = physPreset == p,
                                    onClick = {
                                        physPreset = p
                                        when (p) {
                                            "0-10 бар" -> { textPhysMin = "0.0"; textPhysMax = "10.0"; physUnit = "бар" }
                                            "0-16 бар" -> { textPhysMin = "0.0"; textPhysMax = "16.0"; physUnit = "бар" }
                                            "0-6 бар" -> { textPhysMin = "0.0"; textPhysMax = "6.0"; physUnit = "бар" }
                                            "0-25 бар" -> { textPhysMin = "0.0"; textPhysMax = "25.0"; physUnit = "бар" }
                                            "0-100 °C" -> { textPhysMin = "0.0"; textPhysMax = "100.0"; physUnit = "°C" }
                                            "-50..150 °C" -> { textPhysMin = "-50.0"; textPhysMax = "150.0"; physUnit = "°C" }
                                            "0-5 м" -> { textPhysMin = "0.0"; textPhysMax = "5.0"; physUnit = "м" }
                                        }
                                        updateAllFromPercent(currentPercent)
                                    },
                                    label = { Text(p, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Поля ручного ввода Мин / Макс
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Мин:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            CompactNumberInput(
                                value = textPhysMin,
                                onValueChange = {
                                    textPhysMin = it
                                    physPreset = "Свой"
                                    updateAllFromPercent(currentPercent)
                                },
                                modifier = Modifier.width(72.dp)
                            )
                            Text("—", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text("Макс:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            CompactNumberInput(
                                value = textPhysMax,
                                onValueChange = {
                                    textPhysMax = it
                                    physPreset = "Свой"
                                    updateAllFromPercent(currentPercent)
                                },
                                modifier = Modifier.width(72.dp)
                            )
                            Text(physUnit, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // 1.3. ДИАПАЗОН ЧАСТОТНИКА (ЧРП)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("3. Диапазон частоты ЧРП:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("0-50 Гц", "0-60 Гц", "20-50 Гц").forEach { p ->
                                FilterChip(
                                    selected = freqPreset == p,
                                    onClick = {
                                        freqPreset = p
                                        when (p) {
                                            "0-50 Гц" -> { textFreqMin = "0.0"; textFreqMax = "50.0" }
                                            "0-60 Гц" -> { textFreqMin = "0.0"; textFreqMax = "60.0" }
                                            "20-50 Гц" -> { textFreqMin = "20.0"; textFreqMax = "50.0" }
                                        }
                                        updateAllFromPercent(currentPercent)
                                    },
                                    label = { Text(p, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Поля ручного ввода Fмин / Fмакс
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fмин:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            CompactNumberInput(
                                value = textFreqMin,
                                onValueChange = {
                                    textFreqMin = it
                                    freqPreset = "Свой"
                                    updateAllFromPercent(currentPercent)
                                },
                                modifier = Modifier.width(72.dp)
                            )
                            Text("—", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text("Fмакс:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            CompactNumberInput(
                                value = textFreqMax,
                                onValueChange = {
                                    textFreqMax = it
                                    freqPreset = "Свой"
                                    updateAllFromPercent(currentPercent)
                                },
                                modifier = Modifier.width(72.dp)
                            )
                            Text("Гц", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // ================= 2. КАРТОЧКИ ЗНАЧЕНИЙ (4 ШТУКИ) =================
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueInputCard(
                    title = "СИГНАЛ",
                    value = textSignal,
                    unit = sigUnit,
                    modifier = Modifier.weight(1f),
                    onValueChange = { str ->
                        textSignal = str
                        str.toCleanDoubleOrNull()?.let { v -> updateAllFromSignal(v) }
                    }
                )

                ValueInputCard(
                    title = "ЗАДАНИЕ %",
                    value = textPercent,
                    unit = "%",
                    modifier = Modifier.weight(1f),
                    onValueChange = { str ->
                        textPercent = str
                        str.toCleanDoubleOrNull()?.let { v -> updateAllFromPercent(v, updatePctField = false) }
                    }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueInputCard(
                    title = "ВЕЛИЧИНА",
                    value = textPhys,
                    unit = physUnit,
                    modifier = Modifier.weight(1f),
                    onValueChange = { str ->
                        textPhys = str
                        str.toCleanDoubleOrNull()?.let { v -> updateAllFromPhys(v) }
                    }
                )

                ValueInputCard(
                    title = "ВЫХОД ЧРП",
                    value = textFreq,
                    unit = "Гц",
                    modifier = Modifier.weight(1f),
                    onValueChange = { str ->
                        textFreq = str
                        str.toCleanDoubleOrNull()?.let { v -> updateAllFromFreq(v) }
                    }
                )
            }

            // ================= 3. ПОЛЗУНОК =================
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = currentPercent.toFloat().coerceIn(0f, 100f),
                    onValueChange = { updateAllFromPercent(it.toDouble()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("25%", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("50%", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("75%", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("100%", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            // ================= 4. КНОПКИ БЫСТРОГО ВЫБОРА =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(0.0, 25.0, 50.0, 75.0, 100.0).forEach { p ->
                    Button(
                        onClick = { updateAllFromPercent(p) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                    ) {
                        Text("${p.toInt()}%", fontSize = 11.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val s = 0.0
                        textSignal = String.format(Locale.US, "%.3f", s)
                        updateAllFromSignal(s)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("0 мА (Обрыв)", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
                OutlinedButton(
                    onClick = {
                        val s = 21.0
                        textSignal = String.format(Locale.US, "%.3f", s)
                        updateAllFromSignal(s)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("21 мА (Авария)", fontSize = 11.sp, color = Color(0xFFE65100))
                }
            }

            // ================= 5. ДИАГНОСТИКА NAMUR NE43 =================
            val sigVal = textSignal.toCleanDoubleOrNull() ?: 12.0
            val (limits, _) = getLimits()
            val sMin = limits[0]
            val sMax = limits[1]

            val (statusText, statusColor, statusBg) = when {
                sMin == 4.0 && sMax == 20.0 && sigUnit == "мА" && sigVal < 3.6 ->
                    Triple("⚠️ ОБРЫВ ЛИНИИ / ОТКАЗ ДАТЧИКА (I < 3.6 мА)", Color(0xFFB71C1C), Color(0xFFFFEBEE))
                sMin == 4.0 && sMax == 20.0 && sigUnit == "мА" && sigVal > 21.0 ->
                    Triple("🚨 НЕИСПРАВНОСТЬ / КЗ / ЗАШКАЛ (I > 21.0 мА)", Color(0xFFB71C1C), Color(0xFFFFEBEE))
                currentPercent < 0.0 || currentPercent > 100.0 ->
                    Triple("⚠️ Вне калиброванного диапазона (0..100%)", Color(0xFFE65100), Color(0xFFFFF3E0))
                else ->
                    Triple("✅ Сигнал в норме (нормальная работа)", Color(0xFF1B5E20), Color(0xFFE8F5E9))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusBg, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // ================= 6. ТАБЛИЦА КОНТРОЛЬНЫХ ТОЧЕК =================
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📊 Контрольные точки (нажмите для выбора):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))

                    listOf(0, 25, 50, 75, 100).forEach { p ->
                        val r = p / 100.0
                        val s = limits[0] + r * (limits[1] - limits[0])
                        val ph = limits[2] + r * (limits[3] - limits[2])
                        val f = limits[4] + r * (limits[5] - limits[4])

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { updateAllFromPercent(p.toDouble()) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$p%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text(String.format(Locale.US, "%.2f %s", s, sigUnit), fontSize = 13.sp)
                            Text(String.format(Locale.US, "%.2f %s", ph, physUnit), fontSize = 13.sp)
                            Text(String.format(Locale.US, "%.1f Гц", f), fontSize = 13.sp)
                        }
                        if (p < 100) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }

val cleanInitialTarget = textPhys.toCleanDoubleOrNull()?.let { String.format(Locale.US, "%.1f", it) } ?: textPhys

    // ДИАЛОГ ПОШАГОВОЙ НАСТРОЙКИ CHINT NVF2G
    if (showNvf2gDialog) {
        Nvf2gWizardDialog(
            initialMin = textPhysMin,
            initialMax = textPhysMax,
            initialTarget = cleanInitialTarget,
            onDismiss = { showNvf2gDialog = false }
        )
    }

    // ДИАЛОГ ПОШАГОВОЙ НАСТРОЙКИ DELTA VFD-B
    if (showDeltaDialog) {
        DeltaWizardDialog(
            initialMin = textPhysMin,
            initialMax = textPhysMax,
            initialTarget = cleanInitialTarget,
            initialFmax = textFreqMax,
            onDismiss = { showDeltaDialog = false }
        )
    }

    // ДИАЛОГ ПОШАГОВОЙ НАСТРОЙКИ DANFOSS VLT FC 302
    if (showDanfossDialog) {
        DanfossWizardDialog(
            initialMin = textPhysMin,
            initialMax = textPhysMax,
            initialTarget = cleanInitialTarget,
            onDismiss = { showDanfossDialog = false }
        )
    }

    // ДИАЛОГ ПОШАГОВОЙ НАСТРОЙКИ СЕРИИ 900 / X9
    if (showSeries900Dialog) {
        Series900WizardDialog(
            initialMin = textPhysMin,
            initialMax = textPhysMax,
            initialTarget = cleanInitialTarget,
            onDismiss = { showSeries900Dialog = false }
        )
    }
}

// Компактное поле ручного ввода для диапазонов Мин / Макс
@Composable
fun CompactNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Карточка интерактивного значения
@Composable
fun ValueInputCard(
    title: String,
    value: String,
    unit: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(unit, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

// Полноэкранные мастера и каталоги параметров частотных преобразователей

@Composable
fun GroupHeaderCard(title: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
    ) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun FullParamItem(
    code: String,
    name: String,
    defaultVal: String,
    recVal: String,
    desc: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        code,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text("Завод: $defaultVal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Row {
                Text("Рекомендовано: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(recVal, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }
            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f))
        }
    }
}

@Composable
fun AppExampleCard(
    title: String,
    wiring: String,
    params: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Схема подключения:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(wiring, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("Параметры:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(params, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StepSection(
    stepNum: String,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(stepNum, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            content()
        }
    }
}

@Composable
fun ParamRowItem(code: String, value: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text(code, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        }
        Text("=", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981))
        Text("— $desc", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
    }
}

// -------------------------------------------------------------
// 1. ДИАЛОГ CHINT NVF2G С ВКЛАДКАМИ
// -------------------------------------------------------------
@Composable
fun Nvf2gWizardDialog(
    initialMin: String,
    initialMax: String,
    initialTarget: String,
    onDismiss: () -> Unit
) {
    var sensorMin by remember { mutableStateOf(initialMin) }
    var sensorMax by remember { mutableStateOf(initialMax) }
    var targetPress by remember { mutableStateOf(initialTarget) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val minVal = sensorMin.toCleanDoubleOrNull() ?: 0.0
    val maxVal = sensorMax.toCleanDoubleOrNull() ?: 10.0
    val targetVal = targetPress.toCleanDoubleOrNull() ?: 4.0
    val span = maxVal - minVal
    val targetPct = if (span != 0.0) ((targetVal - minVal) / span) * 100.0 else 0.0
    val targetPctStr = String.format(Locale.US, "%.1f", targetPct)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Шапка
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🔧 CHINT NVF2G",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0284C7)
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Вкладки
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("⚡ Быстрый ПИД", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("📚 Все параметры и примеры", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (selectedTab == 0) {
                    // ВКЛАДКА 1: БЫСТРЫЙ ПИД
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🎯 Расчет уставки для параметра F9.02", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Мин:", fontSize = 10.sp)
                                        CompactNumberInput(value = sensorMin, onValueChange = { sensorMin = it })
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Макс:", fontSize = 10.sp)
                                        CompactNumberInput(value = sensorMax, onValueChange = { sensorMax = it })
                                    }
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text("Нужно (бар):", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        CompactNumberInput(value = targetPress, onValueChange = { targetPress = it })
                                    }
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("F9.02 (уставка %):", fontSize = 11.sp)
                                        Text("$targetPctStr %", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                                    }
                                }
                            }
                        }

                        StepSection(stepNum = "1", title = "Подключение клемм и схема", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("• Датчик 4-20 мА: +24V -> Датчик (+) -> Датчик (-) -> клемма AI2", fontSize = 11.sp)
                                Text("• Перемычка COM - 0V: ОБЯЗАТЕЛЬНА!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                                Text("• Джампер J1: перевести в положение 'I' (ток)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("• Пуск двигателя: контакт (тумблер) между DI1 и DCM", fontSize = 11.sp)
                            }
                        })

                        StepSection(stepNum = "2", title = "Базовые параметры ПЧ (F0)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("F0.01", "1", "Пуск с внешних клемм DI1-DCM")
                                ParamRowItem("F0.03", "5", "Источник частоты: ПИД-регулятор")
                                ParamRowItem("F0.04", "50.00", "Максимальная выходная частота (Гц)")
                                ParamRowItem("F0.06", "20.00", "Нижний предел частоты (защита насоса)")
                                ParamRowItem("F0.07", "10.0 с", "Время разгона (плавный пуск)")
                                ParamRowItem("F0.08", "10.0 с", "Время торможения (без гидроудара)")
                                ParamRowItem("F0.09", "1", "Запрет реверса (только вперед!)")
                            }
                        })

                        StepSection(stepNum = "3", title = "Калибровка входа AI2 (F7)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("F7.01", "2.00 В", "Нижний порог 4 мА (при J1 в 'I' это 2 В)")
                                ParamRowItem("F7.03", "10.00 В", "Верхний порог 20 мА (10.00 В)")
                            }
                        })

                        StepSection(stepNum = "4", title = "ПИД-регулятор (F9)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("F9.00", "0", "Источник уставки: параметр F9.02")
                                ParamRowItem("F9.01", "1", "Обратная связь: токовый вход AI2")
                                ParamRowItem("F9.02", "$targetPctStr %", "Уставка давления по расчету")
                                ParamRowItem("F9.03", "1.50", "Коэффициент усиления Kp (1.0-2.0)")
                                ParamRowItem("F9.04", "2.50 с", "Время интегрирования Ti (2-4 с)")
                                ParamRowItem("F9.05", "0.00 с", "Дифференцирование Td (выключено)")
                            }
                        })

                        StepSection(stepNum = "5", title = "Спящий режим (F9)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("F9.28", "1", "Спящий режим: ВКЛЮЧЕН")
                                ParamRowItem("F9.30", "1", "Условие засыпания: по частоте")
                                ParamRowItem("F9.31", "25.00", "Частота перехода в сон (Гц)")
                                ParamRowItem("F9.32", "10.0 с", "Задержка засыпания (сек)")
                                ParamRowItem("F9.33", "10.0 %", "Порог пробуждения (% падения)")
                                ParamRowItem("F9.34", "1.0 с", "Задержка пробуждения")
                            }
                        })
                    }
                } else {
                    // ВКЛАДКА 2: ВСЕ ПАРАМЕТРЫ И ПРИМЕРЫ
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GroupHeaderCard("⚡ Группа F0: Базовые параметры работы", Color(0xFF0284C7))
                        FullParamItem("F0.01", "Режим пуска/останова", "0", "1 (Клеммы)", "0: Пульт, 1: Клеммы DI1..DI5, 2: RS485 Modbus", Color(0xFF0284C7))
                        FullParamItem("F0.03", "Источник опорной частоты", "0", "5 (ПИД) / 1 (AI1)", "0: Кнопки, 1: AI1 (0-10В), 2: AI2 (4-20мА), 5: ПИД", Color(0xFF0284C7))
                        FullParamItem("F0.04", "Максимальная частота", "50.00 Гц", "50.00 Гц", "Предел выходной частоты привода", Color(0xFF0284C7))
                        FullParamItem("F0.06", "Нижний предел частоты", "0.00 Гц", "20.00 Гц", "Защита насоса от перегрева на закрытую задвижку", Color(0xFF0284C7))
                        FullParamItem("F0.07", "Время разгона 1", "10.0 с", "8.0..12.0 с", "Плавный разгон мотора от 0 до Fmax", Color(0xFF0284C7))
                        FullParamItem("F0.08", "Время торможения 1", "10.0 с", "8.0..12.0 с", "Плавный останов без гидроудара", Color(0xFF0284C7))
                        FullParamItem("F0.09", "Запрет реверса", "0", "1 (Запрещен)", "Строго для насосов и вентиляторов!", Color(0xFF0284C7))
                        FullParamItem("F0.10", "Несущая частота ШИМ", "4.0 кГц", "4.0..8.0 кГц", "Выше частота — тише свист мотора", Color(0xFF0284C7))
                        FullParamItem("F0.16", "Сброс на заводские", "0", "0", "1: Полный сброс параметров к заводским", Color(0xFF0284C7))

                        GroupHeaderCard("⚙️ Группа F1: Двигатель и автонастройка", Color(0xFF0284C7))
                        FullParamItem("F1.00", "Мощность двигателя", "По ПЧ", "кВт шильдика", "Паспортная мощность электродвигателя", Color(0xFF0284C7))
                        FullParamItem("F1.01", "Номинальное напряжение", "380 В", "380 В", "Номинальное линейное напряжение", Color(0xFF0284C7))
                        FullParamItem("F1.02", "Номинальный ток", "По ПЧ", "Амперы (А)", "Паспортный ток мотора для термозащиты", Color(0xFF0284C7))
                        FullParamItem("F1.04", "Номинальные обороты", "1440", "об/мин", "Скорость вращения вала с шильдика", Color(0xFF0284C7))
                        FullParamItem("F1.11", "Автонастройка (Tuning)", "0", "1 (статическая)", "0: Нет, 1: Статическая (вал неподвижен)", Color(0xFF0284C7))

                        GroupHeaderCard("🔌 Группа F2 & F5: Режим U/f и клеммы DI", Color(0xFF0284C7))
                        FullParamItem("F2.00", "Режим управления", "1 (V/F)", "1 (V/F)", "0: Векторный SVC, 1: Скалярный V/F", Color(0xFF0284C7))
                        FullParamItem("F5.00", "Клемма DI1", "1", "1 (Пуск Вперед)", "Пуск двигателя при замыкании на клемму DCM", Color(0xFF0284C7))
                        FullParamItem("F5.01", "Клемма DI2", "2", "2 (Реверс) / 9", "2: Реверс, 9: Внешний сброс аварии Reset", Color(0xFF0284C7))
                        FullParamItem("F6.00", "Выходное реле TA-TB-TC", "2", "2 (Авария) / 1", "1: Привод в работе RUN, 2: Авария ПЧ Fault", Color(0xFF0284C7))

                        GroupHeaderCard("🎯 Группа F7 & F9: ПИД и спящий режим", Color(0xFF0284C7))
                        FullParamItem("F7.01", "Нижний порог AI2 (4 мА)", "0.00 В", "2.00 В", "Ток 4 мА на шунте 500 Ом дает 2.00 В (J1 в 'I')", Color(0xFF0284C7))
                        FullParamItem("F9.00", "Источник уставки ПИД", "0", "0 (цифровой)", "0: Уставка в F9.02, 1: Вход AI1", Color(0xFF0284C7))
                        FullParamItem("F9.01", "Источник ОС ПИД", "0", "1 (AI2 4-20мА)", "0: AI1 (0-10В), 1: AI2 (4-20 мА)", Color(0xFF0284C7))
                        FullParamItem("F9.03", "Коэффициент Kp", "1.00", "1.20..2.00", "Пропорциональный коэффициент контура", Color(0xFF0284C7))
                        FullParamItem("F9.04", "Время интегрирования Ti", "1.00 с", "2.00..4.00 с", "Устранение статической погрешности", Color(0xFF0284C7))
                        FullParamItem("F9.28", "Спящий режим", "0", "1 (Включен)", "Автоотключение насоса при нулевом расходе", Color(0xFF0284C7))
                        FullParamItem("F9.31", "Частота сна", "20.00 Гц", "22.0..25.0 Гц", "Частота перехода в спящий режим", Color(0xFF0284C7))
                        FullParamItem("F9.32", "Задержка сна", "5.0 с", "10.0..15.0 с", "Фильтр кратковременных падений расхода", Color(0xFF0284C7))

                        GroupHeaderCard("💡 Практические схемы применения NVF2G", Color(0xFF0284C7))
                        AppExampleCard(
                            title = "🚰 Пример 1: Насос водоснабжения со спящим режимом",
                            wiring = "Датчик 4-20 мА: +24V -> Датчик (+) -> Датчик (-) -> AI2. Перемычка COM-0V. Джампер J1 в 'I'. Тумблер пуска: DI1-DCM.",
                            params = "F0.01=1, F0.03=5, F0.06=20.00, F0.09=1, F7.01=2.00, F9.00=0, F9.01=1, F9.02=40%, F9.03=1.5, F9.04=3.0, F9.28=1, F9.31=23.0, F9.32=12.0 с, F9.33=8.0%",
                            color = Color(0xFF0284C7)
                        )
                        AppExampleCard(
                            title = "🌀 Пример 2: Вентилятор вытяжки с потенциометром 0-10 В",
                            wiring = "Потенциометр 10 кОм: край 1 -> +10V, движок -> AI1, край 2 -> GND. Разрешение: контакт DI1-DCM.",
                            params = "F0.01=1, F0.03=1, F0.07=15.0, F0.08=15.0, F0.09=1, F2.00=1",
                            color = Color(0xFF0284C7)
                        )
                        AppExampleCard(
                            title = "📦 Пример 3: Двухскоростной конвейер (15 Гц / 50 Гц)",
                            wiring = "Кнопка Пуск: DI1-DCM. Концевик быстрого хода: DI4-DCM.",
                            params = "F0.01=1, F0.03=0, F5.00=1, F5.03=16, FC.00=15.00 Гц, FC.01=50.00 Гц",
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Закрыть", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. ДИАЛОГ DELTA VFD-B С ВКЛАДКАМИ
// -------------------------------------------------------------
@Composable
fun DeltaWizardDialog(
    initialMin: String,
    initialMax: String,
    initialTarget: String,
    initialFmax: String,
    onDismiss: () -> Unit
) {
    var sensorMin by remember { mutableStateOf(initialMin) }
    var sensorMax by remember { mutableStateOf(initialMax) }
    var targetPress by remember { mutableStateOf(initialTarget) }
    var freqMax by remember { mutableStateOf(initialFmax) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val minVal = sensorMin.toCleanDoubleOrNull() ?: 0.0
    val maxVal = sensorMax.toCleanDoubleOrNull() ?: 10.0
    val targetVal = targetPress.toCleanDoubleOrNull() ?: 4.0
    val fmaxVal = freqMax.toCleanDoubleOrNull() ?: 50.0
    val span = maxVal - minVal
    val targetPct = if (span != 0.0) (targetVal - minVal) / span else 0.0
    val targetHz = targetPct * fmaxVal
    val targetHzStr = String.format(Locale.US, "%.2f", targetHz)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Шапка
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🔧 DELTA VFD-B",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF059669)
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Вкладки
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("⚡ Быстрый ПИД", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("📚 Все параметры и примеры", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (selectedTab == 0) {
                    // ВКЛАДКА 1: БЫСТРЫЙ ПИД
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF059669).copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🎯 Расчет задания для пульта DELTA VFD-B", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF059669))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Мин:", fontSize = 10.sp)
                                        CompactNumberInput(value = sensorMin, onValueChange = { sensorMin = it })
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Макс:", fontSize = 10.sp)
                                        CompactNumberInput(value = sensorMax, onValueChange = { sensorMax = it })
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Давление:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        CompactNumberInput(value = targetPress, onValueChange = { targetPress = it })
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Fmax:", fontSize = 10.sp)
                                        CompactNumberInput(value = freqMax, onValueChange = { freqMax = it })
                                    }
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Задание стрелками пульта:", fontSize = 11.sp)
                                        Text("$targetHzStr Гц", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                                    }
                                }
                            }
                        }

                        StepSection(stepNum = "1", title = "Подключение клемм и схема", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("• Датчик 4-20 мА: +24V -> Датчик (+) -> Датчик (-) -> клемма ACI", fontSize = 11.sp)
                                Text("• Перемычка ACM - 0V: ОБЯЗАТЕЛЬНА!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                                Text("• Пуск двигателя: сухой контакт (тумблер) между FWD и DCM", fontSize = 11.sp)
                            }
                        })

                        StepSection(stepNum = "2", title = "Базовые параметры (00, 01, 02)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("00-02", "09", "Сброс к заводским установкам 50 Гц")
                                ParamRowItem("01-00", "50.00 Гц", "Максимальная частота Fmax")
                                ParamRowItem("01-07", "20.00 Гц", "Нижний предел частоты (защита насоса)")
                                ParamRowItem("01-09", "10.0 с", "Время разгона 1 (Ramp up)")
                                ParamRowItem("01-10", "10.0 с", "Время замедления 1 (Ramp down)")
                                ParamRowItem("02-00", "00", "Источник задания: кнопки на пульте")
                                ParamRowItem("02-01", "01", "Источник ПУСК: клеммы FWD-DCM")
                                ParamRowItem("02-04", "01", "Запрет реверса двигателя")
                            }
                        })

                        StepSection(stepNum = "3", title = "Настройка ПИД-регулятора (Группа 10)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("10-00", "02", "ПИД вкл, прямая ОС с входа ACI (4-20 мА)")
                                ParamRowItem("10-01", "1.00", "Множитель обратной связи")
                                ParamRowItem("10-02", "2.0", "Коэффициент усиления P (1.0 - 2.5)")
                                ParamRowItem("10-03", "2.50 с", "Время интегрирования I (2 - 4 с)")
                                ParamRowItem("10-04", "0.00 с", "Дифференцирование D (выключено)")
                                ParamRowItem("10-08", "01", "Контроль обрыва датчика ОС (< 4 мА)")
                                ParamRowItem("10-09", "01", "Реакция на обрыв: останов и авария")
                            }
                        })

                        StepSection(stepNum = "4", title = "Спящий режим (Группа 11: Fan & Pump)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("11-05", "10.0 с", "Задержка перехода в сон и пробуждения")
                                ParamRowItem("11-06", "25.00 Гц", "Частота перехода в сон при нуле расхода")
                                ParamRowItem("11-07", "28.00 Гц", "Частота пробуждения (строго > 11-06)")
                            }
                        })
                    }
                } else {
                    // ВКЛАДКА 2: ВСЕ ПАРАМЕТРЫ И ПРИМЕРЫ
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GroupHeaderCard("⚡ Группы 00 и 01: Базовые параметры DELTA", Color(0xFF059669))
                        FullParamItem("00-02", "Сброс параметров", "00", "09 (50 Гц)", "09: Сброс всех настроек к заводским 50 Гц", Color(0xFF059669))
                        FullParamItem("00-03", "Отображение на дисплее", "00", "00 (Гц) / 02 (K)", "00: Частота F, 01: Ток A, 02: K*F (давление)", Color(0xFF059669))
                        FullParamItem("01-00", "Максимальная частота Fmax", "50.00 Гц", "50.00 Гц", "Верхний предел частоты вращения", Color(0xFF059669))
                        FullParamItem("01-02", "Максимальное напряжение", "380 В", "380 В", "Выходное номинальное напряжение мотора", Color(0xFF059669))
                        FullParamItem("01-07", "Нижний предел частоты", "0.10 Гц", "20.00 Гц", "Ограничение скорости насоса от перегрева", Color(0xFF059669))
                        FullParamItem("01-09", "Время разгона 1", "10.0 с", "8.0..12.0 с", "Плавный разгон до номинала", Color(0xFF059669))
                        FullParamItem("01-10", "Время замедления 1", "10.0 с", "8.0..12.0 с", "Плавный останов без гидроудара", Color(0xFF059669))

                        GroupHeaderCard("🔌 Группы 02, 04, 05: Управление и клеммы", Color(0xFF059669))
                        FullParamItem("02-00", "Источник задания частоты", "00", "00 (Пульт) / 02", "00: Панель, 01: AVI (0-10В), 02: ACI (4-20мА)", Color(0xFF059669))
                        FullParamItem("02-01", "Источник команды ПУСК", "00", "01 (Клеммы)", "00: Клавиша RUN, 01: Клеммы FWD-DCM", Color(0xFF059669))
                        FullParamItem("02-02", "Способ остановки", "00", "00 (Рампа)", "00: Замедление по рампе, 01: Самовыбег", Color(0xFF059669))
                        FullParamItem("02-04", "Запрет реверса", "00", "01 (Запрещен)", "Блокировка обратного вращения", Color(0xFF059669))
                        FullParamItem("04-07", "Режим клеммного управления", "00", "00 (2-пр) / 02", "00: 2-проводной, 02: 3-проводной импульсный", Color(0xFF059669))
                        FullParamItem("05-00", "Реле RA-RB-RC", "01", "01 (Авария) / 02", "01: Индикация Аварии, 02: Привод в работе", Color(0xFF059669))

                        GroupHeaderCard("🎯 Группы 10 и 11: ПИД и режим сна", Color(0xFF059669))
                        FullParamItem("10-00", "Режим работы ПИД", "00", "02 (ACI 4-20мА)", "01: ОС с AVI (0-10В), 02: ОС с ACI (4-20мА)", Color(0xFF059669))
                        FullParamItem("10-02", "Коэффициент усиления (P)", "1.0", "1.5..2.5", "Жесткость поддержания давления", Color(0xFF059669))
                        FullParamItem("10-03", "Время интегрирования (I)", "1.00 с", "2.00..4.00 с", "Устранение статической ошибки давления", Color(0xFF059669))
                        FullParamItem("10-08", "Контроль обрыва датчика", "00", "01 (Включен)", "Защита при падении тока ниже 4 мА", Color(0xFF059669))
                        FullParamItem("11-05", "Задержка засыпания", "0.0 с", "10.0..15.0 с", "Время работы на частоте сна до останова", Color(0xFF059669))
                        FullParamItem("11-06", "Частота засыпания", "0.00 Гц", "22.0..25.0 Гц", "Порог ухода в спящий режим", Color(0xFF059669))
                        FullParamItem("11-07", "Частота пробуждения", "0.00 Гц", "26.0..30.0 Гц", "Частота пробуждения (строго > 11-06)", Color(0xFF059669))

                        GroupHeaderCard("💡 Практические схемы применения DELTA VFD-B", Color(0xFF059669))
                        AppExampleCard(
                            title = "🚰 Пример 1: Насос ХВС с датчиком 4-20 мА и спящим режимом",
                            wiring = "Датчик 4-20 мА: +24V -> Датчик (+) -> Датчик (-) -> клемма ACI. Перемычка ACM-0V. Тумблер пуска: FWD-DCM.",
                            params = "00-02=09, 01-00=50.00, 01-07=20.00, 02-00=00, 02-01=01, 02-04=01, 10-00=02, 10-02=2.0, 10-03=3.0, 11-05=12.0 с, 11-06=22.0 Гц, 11-07=27.0 Гц",
                            color = Color(0xFF059669)
                        )
                        AppExampleCard(
                            title = "🔘 Пример 2: 3-проводное управление кнопками Пуск/Стоп",
                            wiring = "Кнопка 'Пуск' (НО) между M0 (FWD) и DCM. Кнопка 'Стоп' (НЗ) между M2 и DCM.",
                            params = "02-01=01 (Управление с клемм), 04-07=02 (3-проводной импульсный режим)",
                            color = Color(0xFF059669)
                        )
                        AppExampleCard(
                            title = "🌪️ Пример 3: Вентилятор вытяжки по сигналу 0-10 В",
                            wiring = "Сигнал 0-10 В: (+) на клемму AVI, (-) на клемму ACM. Разрешение пуска: контакт FWD-DCM.",
                            params = "02-00=01, 02-01=01, 01-09=20.0 с, 01-10=20.0 с",
                            color = Color(0xFF059669)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Закрыть", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. ДИАЛОГ DANFOSS VLT FC 302 С ВКЛАДКАМИ
// -------------------------------------------------------------
@Composable
fun DanfossWizardDialog(
    initialMin: String,
    initialMax: String,
    initialTarget: String,
    onDismiss: () -> Unit
) {
    var sensorMin by remember { mutableStateOf(initialMin) }
    var sensorMax by remember { mutableStateOf(initialMax) }
    var targetPress by remember { mutableStateOf(initialTarget) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val targetVal = targetPress.toCleanDoubleOrNull() ?: 4.0
    val targetValStr = String.format(Locale.US, "%.3f", targetVal)
    val maxSensorStr = sensorMax.toCleanDoubleOrNull()?.let { String.format(Locale.US, "%.3f", it) } ?: "10.000"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Шапка
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🔧 Danfoss VLT® FC 302",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFDC2626)
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Вкладки
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("⚡ Быстрый ПИД", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("📚 Все параметры и примеры", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (selectedTab == 0) {
                    // ВКЛАДКА 1: БЫСТРЫЙ ПИД
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDC2626).copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🎯 Уставка давления для Danfoss FC 302", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Мин (бар):", fontSize = 10.sp)
                                        CompactNumberInput(value = sensorMin, onValueChange = { sensorMin = it })
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Макс (бар):", fontSize = 10.sp)
                                        CompactNumberInput(value = sensorMax, onValueChange = { sensorMax = it })
                                    }
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text("Нужно (бар):", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        CompactNumberInput(value = targetPress, onValueChange = { targetPress = it })
                                    }
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Уставка в параметр 3-10[0]:", fontSize = 11.sp)
                                        Text("$targetValStr бар", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFDC2626))
                                    }
                                }
                            }
                        }

                        StepSection(stepNum = "1", title = "Подключение клемм и схема", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("• Датчик давления 4-20 мА: Клемма 12 (+24V) -> Датчик (+) -> Датчик (-) -> Клемма 54", fontSize = 11.sp)
                                Text("• Переключатель A54 (под дисплеем): перевести в положение 'I' (ток)!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                                Text("• Клемма 37 (Safe Stop): ОБЯЗАТЕЛЬНО соединить с клеммой 12 (+24V)! Иначе Alarm 68.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                                Text("• Пуск: сухой контакт (тумблер) между клеммой 12 (+24V) и клеммой 18 (Start)", fontSize = 11.sp)
                            }
                        })

                        StepSection(stepNum = "2", title = "Базовые параметры (Группы 1, 3, 4, 5)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("1-00", "[3] Closed Loop", "Замкнутый контур процесса (ПИД)")
                                ParamRowItem("1-03", "[1] Var Torque", "Переменный момент для насосов")
                                ParamRowItem("4-10", "[0] Clockwise", "Вращение только по часовой стрелке")
                                ParamRowItem("4-12", "20.0 Hz", "Минимальная скорость двигателя (Гц)")
                                ParamRowItem("4-14", "50.0 Hz", "Максимальная скорость двигателя (Гц)")
                                ParamRowItem("3-41", "10.00 s", "Время разгона (Ramp 1 Up)")
                                ParamRowItem("3-42", "10.00 s", "Время торможения (Ramp 1 Down)")
                                ParamRowItem("5-10", "[8] Start", "Назначение клеммы 18: Пуск")
                            }
                        })

                        StepSection(stepNum = "3", title = "Калибровка датчика (Вход 54, Группа 6)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("6-22", "4.00 mA", "Минимальный ток клеммы 54")
                                ParamRowItem("6-23", "20.00 mA", "Максимальный ток клеммы 54")
                                ParamRowItem("6-24", "0.000", "Минимальная физ. величина (бар)")
                                ParamRowItem("6-25", maxSensorStr, "Максимальная физ. величина датчика")
                            }
                        })

                        StepSection(stepNum = "4", title = "ПИД-регулятор процесса (Группа 7)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("7-20", "[2] AI 54", "Источник обратной связи = клемма 54")
                                ParamRowItem("7-30", "[0] Normal", "Нормальное прямое регулирование")
                                ParamRowItem("7-31", "[1] On", "Защита от насыщения интегратора")
                                ParamRowItem("7-33", "1.00", "Пропорциональный коэффициент Kp")
                                ParamRowItem("7-34", "3.00 s", "Интегральное время Ti (2.5 - 5 с)")
                                ParamRowItem("7-35", "0.00 s", "Дифференцирование Td (выключено)")
                            }
                        })

                        StepSection(stepNum = "5", title = "Спящий режим (Группа 22: Application)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("22-22", "[1] Enabled", "Контроль низкой скорости активен")
                                ParamRowItem("22-23", "[1] Sleep", "Действие при нулевом расходе: сон")
                                ParamRowItem("22-40", "10 s", "Минимальное время работы до сна")
                                ParamRowItem("22-41", "10 s", "Минимальное время в спящем режиме")
                                ParamRowItem("22-44", "10.0 %", "Порог пробуждения (% падения)")
                            }
                        })

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("💡 Важно:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                                Text("• Обязательно нажмите клавишу [Auto On] на пульте LCP! В режиме [Hand On] частотник работает только вручную.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    // ВКЛАДКА 2: ВСЕ ПАРАМЕТРЫ И ПРИМЕРЫ
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GroupHeaderCard("⚡ Группы 0 и 1: Управление и автоадаптация (AMA)", Color(0xFFDC2626))
                        FullParamItem("0-01", "Язык меню LCP", "[0] English", "[28] Русский", "[0] English, [28] Русский язык", Color(0xFFDC2626))
                        FullParamItem("0-02", "Единицы скорости", "[1] Hz", "[1] Гц (Hz)", "[0] RPM, [1] Гц", Color(0xFFDC2626))
                        FullParamItem("0-51", "Сброс к заводским", "[0] Выкл", "[1] Сброс", "Инициализация параметров при перезагрузке питания", Color(0xFFDC2626))
                        FullParamItem("1-00", "Режим конфигурации", "[0] Open Loop", "[3] Closed Loop", "[0] Скорость, [3] ПИД замкнутый контур процесса", Color(0xFFDC2626))
                        FullParamItem("1-03", "Характеристика момента", "[0] Постоянный", "[1] Квадратичный", "[0] Станки, [1] Переменный момент для насосов", Color(0xFFDC2626))
                        FullParamItem("1-20..1-25", "Паспорт двигателя", "По ПЧ", "кВт, В, А, Гц, RPM", "Паспортные данные электродвигателя", Color(0xFFDC2626))
                        FullParamItem("1-29", "Автоадаптация AMA", "[0] Выкл", "[1] Полная AMA", "Замер сопротивления кабеля и статора без вращения вала", Color(0xFFDC2626))

                        GroupHeaderCard("🔌 Группы 3, 4, 5: Задания, рампы и клеммы", Color(0xFFDC2626))
                        FullParamItem("3-02", "Минимальное задание", "0.000", "0.000 бар", "Нижняя граница диапазона процесса", Color(0xFFDC2626))
                        FullParamItem("3-03", "Максимальное задание", "50.000", "10.000 бар", "Верхняя граница шкалы датчика", Color(0xFFDC2626))
                        FullParamItem("3-10[0]", "Предустановленное задание", "0.00 %", "4.000 бар", "Уставка давления в реальных единицах", Color(0xFFDC2626))
                        FullParamItem("3-41", "Время разгона 1", "3.00 с", "8.00..15.00 с", "Время разгона от 0 до номинала", Color(0xFFDC2626))
                        FullParamItem("3-42", "Время торможения 1", "3.00 с", "8.00..15.00 с", "Плавное торможение без гидроудара", Color(0xFFDC2626))
                        FullParamItem("4-10", "Направление вращения", "[0] По часовой", "[0] По часовой", "Строгий запрет реверса насоса", Color(0xFFDC2626))
                        FullParamItem("4-12", "Нижний предел скорости", "0.0 Гц", "20.0..25.0 Гц", "Защита подшипников погружного насоса", Color(0xFFDC2626))
                        FullParamItem("5-10", "Клемма 18 (Пуск)", "[8] Start", "[8] Start", "Пуск замыканием с клеммой 12 (+24V)", Color(0xFFDC2626))
                        FullParamItem("5-40[0]", "Реле 1 (01-02-03)", "[9] Alarm", "[9] Alarm", "[9] Авария ПЧ, [5] Привод в работе", Color(0xFFDC2626))

                        GroupHeaderCard("🎯 Группы 6, 7, 22: Датчик 54, ПИД и режим сна", Color(0xFFDC2626))
                        FullParamItem("6-22", "Клемма 54, Мин. ток", "4.00 мА", "4.00 мА", "Живой ноль датчика (переключатель A54 в 'I')", Color(0xFFDC2626))
                        FullParamItem("6-23", "Клемма 54, Макс. ток", "20.00 мА", "20.00 мА", "Верхний предел токового сигнала", Color(0xFFDC2626))
                        FullParamItem("6-24", "Клемма 54, Мин. физ.", "0.000", "0.000 бар", "Давление при 4 мА", Color(0xFFDC2626))
                        FullParamItem("6-25", "Клемма 54, Макс. физ.", "100.000", "10.000 бар", "Давление при 20 мА (шкала датчика)", Color(0xFFDC2626))
                        FullParamItem("7-20", "Источник ОС ПИД", "[0] Нет", "[2] Вход 54", "Обратная связь от токового входа 54", Color(0xFFDC2626))
                        FullParamItem("7-30", "Тип регулирования", "[0] Normal", "[0] Normal", "Прямое (падение давления -> разгон)", Color(0xFFDC2626))
                        FullParamItem("7-33", "Коэффициент Kp", "0.01", "1.00..1.50", "Усиление контура ПИД", Color(0xFFDC2626))
                        FullParamItem("7-34", "Время интегрирования Ti", "9999.0 с", "3.00..5.00 с", "Заводское 9999 выключает интеграл!", Color(0xFFDC2626))
                        FullParamItem("14-20", "Автоперезапуск", "[0] Ручной", "[10] 10 попыток", "Авторестарт при сетевых сбоях", Color(0xFFDC2626))
                        FullParamItem("22-22", "Контроль низкой скорости", "[0] Выкл", "[1] Включен", "Активация обнаружения нулевого расхода", Color(0xFFDC2626))
                        FullParamItem("22-44", "Порог пробуждения", "10 %", "5..10 %", "Падение давления от уставки для старта", Color(0xFFDC2626))

                        GroupHeaderCard("💡 Практические схемы применения Danfoss FC 302", Color(0xFFDC2626))
                        AppExampleCard(
                            title = "🚰 Пример 1: Погружной артезианский насос с AMA и сном",
                            wiring = "Датчик 4-20 мА: Клемма 12 (+24V) -> (+) датчика; (-) датчика -> Клемма 54. Переключатель A54 в 'I'. Перемычка 12-37 (Safe Stop). Пуск: 12-18.",
                            params = "1-00=[3], 1-03=[1], 1-29=[1] (AMA), 4-10=[0], 4-12=25.0 Гц, 6-22=4.00 мА, 6-23=20.00 мА, 6-24=0.0, 6-25=10.0, 7-20=[2], 7-33=1.20, 7-34=4.00 с, 3-10[0]=4.000 бар, 14-20=[10]. Нажать [Auto On]!",
                            color = Color(0xFFDC2626)
                        )
                        AppExampleCard(
                            title = "🌀 Пример 2: Вентиляция по перепаду давления 0-10 В",
                            wiring = "Сигнал 0-10 В на клеммы 53 (+) и 55 (GND). Переключатель A53 в 'U'. Пуск: клеммы 12-18.",
                            params = "1-00=[3], 1-03=[1], 3-03=500.0 Па, 3-10[0]=250.0 Па, 6-10=0В, 6-11=10В, 6-14=0, 6-15=500, 7-20=[1], 7-33=0.80, 7-34=5.00 с",
                            color = Color(0xFFDC2626)
                        )
                        AppExampleCard(
                            title = "⚙️ Пример 3: Шпиндель станка с потенциометром и реверсом",
                            wiring = "Потенциометр: кл. 50 (+10V), кл. 53 (сигнал), кл. 55 (GND). Тумблер Вперед: кл. 12-18. Тумблер Реверс: кл. 12-19.",
                            params = "1-00=[0], 1-03=[0], 3-15=[1], 4-10=[2], 5-10=[8], 5-11=[10]",
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Закрыть", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}


// -------------------------------------------------------------
// 4. ДИАЛОГ СЕРИИ 900 / X9 С ВКЛАДКАМИ
// -------------------------------------------------------------
@Composable
fun Series900WizardDialog(
    initialMin: String,
    initialMax: String,
    initialTarget: String,
    onDismiss: () -> Unit
) {
    var sensorMin by remember { mutableStateOf(initialMin) }
    var sensorMax by remember { mutableStateOf(initialMax) }
    var targetPress by remember { mutableStateOf(initialTarget) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val minVal = sensorMin.toCleanDoubleOrNull() ?: 0.0
    val maxVal = sensorMax.toCleanDoubleOrNull() ?: 10.0
    val targetVal = targetPress.toCleanDoubleOrNull() ?: 4.0
    val span = maxVal - minVal
    val targetPct = if (span != 0.0) ((targetVal - minVal) / span) * 100.0 else 0.0
    val targetPctStr = String.format(Locale.US, "%.1f", targetPct)
    val targetValStr = String.format(Locale.US, "%.1f", targetVal)
    val maxSensorStr = String.format(Locale.US, "%.1f", maxVal)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Шапка
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🔧 900 Series / X9 (900M / 900G)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFD97706)
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Вкладки
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("⚡ Быстрый ПИД", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("📚 Все параметры и примеры", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (selectedTab == 0) {
                    // ВКЛАДКА 1: БЫСТРЫЙ ПИД
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD97706).copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🎯 Уставка давления F5-01 и шкала F5-09", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFD97706))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Мин:", fontSize = 10.sp)
                                        CompactNumberInput(value = sensorMin, onValueChange = { sensorMin = it })
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Шкала F5-09:", fontSize = 10.sp)
                                        CompactNumberInput(value = sensorMax, onValueChange = { sensorMax = it })
                                    }
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text("Давление F5-01:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        CompactNumberInput(value = targetPress, onValueChange = { targetPress = it })
                                    }
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Уставка F5-01 (Bar):", fontSize = 11.sp)
                                        Text("$targetValStr Bar ($targetPctStr %)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFD97706))
                                    }
                                }
                            }
                        }

                        StepSection(stepNum = "1", title = "Подключение клемм управления", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("• Для 900M (Mini): клемма DC (+24В при F0-06=2) -> (+) датчика; AI -> (-) датчика; GND - общий.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("• Для 900G: клемма 24V -> (+) датчика; AI1 -> (-) датчика; перемычка COM-GND обязательна!", fontSize = 11.sp)
                                Text("• Пуск: сухой контакт (тумблер) между DI1 и GND (или COM).", fontSize = 11.sp)
                            }
                        })

                        StepSection(stepNum = "2", title = "Базовые параметры привода (Группа F0)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("F0-00", "1", "Пуск с внешних клемм DI1")
                                ParamRowItem("F0-01", "6", "Режим водоснабжения постоянного давления")
                                ParamRowItem("F0-04", "10.0 с", "Время разгона (плавный пуск насоса)")
                                ParamRowItem("F0-05", "10.0 с", "Время торможения (без гидроудара)")
                                ParamRowItem("F0-06", "2", "Выход DC: 24В для питания датчика на 900M")
                                ParamRowItem("F0-07", "0002", "Формат входа AI1: токовый сигнал 4-20 мА")
                                ParamRowItem("F0-09", "50.0 Гц", "Верхний предел рабочей частоты")
                                ParamRowItem("F0-10", "20.0 Гц", "Нижний предел частоты (защита насоса)")
                                ParamRowItem("F0-14", "0", "Чередование фаз (1 - реверс без перекидки)")
                            }
                        })

                        StepSection(stepNum = "3", title = "Настройка датчика и ПИД (Группа F5)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("F5-00", "0", "Источник уставки: параметр F5-01")
                                ParamRowItem("F5-01", "$targetValStr Bar", "Целевое давление в барах!")
                                ParamRowItem("F5-02", "0", "Источник обратной связи: вход AI1 / AI")
                                ParamRowItem("F5-03", "0", "Прямое регулирование (давление падает -> разгон)")
                                ParamRowItem("F5-04", "20.0", "Коэффициент усиления Kp")
                                ParamRowItem("F5-05", "0.80 с", "Постоянная интегрирования Ki")
                                ParamRowItem("F5-08", "1", "Тип датчика: токовый 4~20 мА")
                                ParamRowItem("F5-09", "$maxSensorStr Bar", "Шкала датчика давления в барах")
                            }
                        })

                        StepSection(stepNum = "4", title = "Спящий режим и сухой ход (Группа F5)", content = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParamRowItem("F5-12", "22.0 Гц", "Частота перехода в сон при нуле расхода")
                                ParamRowItem("F5-13", "10.0 с", "Задержка засыпания насоса")
                                ParamRowItem("F5-17", "80 %", "Давление пробуждения (80% от уставки)")
                                ParamRowItem("F5-18", "200 %", "Защита от избыточного давления (Err53)")
                                ParamRowItem("F5-19", "15.0 с", "Время детекции сухого хода (Water shortage)")
                                ParamRowItem("F5-20", "48.0 Гц", "Частота контроля сухого хода")
                                ParamRowItem("F5-22", "20 %", "Давление сухого хода (авария A52)")
                                ParamRowItem("F5-23", "20 мин", "Автоперезапуск после сухого хода")
                            }
                        })
                    }
                } else {
                    // ВКЛАДКА 2: ВСЕ ПАРАМЕТРЫ И ПРИМЕРЫ
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GroupHeaderCard("⚡ Группа F0: Базовые параметры управления", Color(0xFFD97706))
                        FullParamItem("F0-00", "Источник команды", "0", "1 (Клеммы)", "0: Панель, 1: Клеммы DI1..DI5, 2: RS485 Modbus", Color(0xFFD97706))
                        FullParamItem("F0-01", "Источник частоты", "1", "6 (Водоснабжение)", "0: Код, 1: Потенциометр, 2: AI1, 6: Пост. давление, 7: ПИД", Color(0xFFD97706))
                        FullParamItem("F0-04", "Время разгона", "10.0 с", "10.0 с", "Время разгона мотора от 0 до Fmax", Color(0xFFD97706))
                        FullParamItem("F0-05", "Время замедления", "10.0 с", "10.0 с", "Плавный останов без гидроудара", Color(0xFFD97706))
                        FullParamItem("F0-06", "Выход клеммы DC", "1 (10V)", "2 (24V)", "24В для питания датчиков 4-20 мА на 900M", Color(0xFFD97706))
                        FullParamItem("F0-07", "Формат аналог. сигналов", "0000", "0002 (4-20мА)", "2: 4-20 мА на младшем разряде AI1", Color(0xFFD97706))
                        FullParamItem("F0-10", "Нижний предел частоты", "0.0 Гц", "20.0 Гц", "Защита насоса от перегрева", Color(0xFFD97706))
                        FullParamItem("F0-14", "Чередование фаз", "0", "0 (U-V-W)", "1: Смена направления без перекидки кабеля", Color(0xFFD97706))
                        FullParamItem("F0-24", "Сброс на заводские", "0", "0", "1: Полный сброс параметров к заводским", Color(0xFFD97706))

                        GroupHeaderCard("🔌 Группа F1: Входы DI, реле и выходы", Color(0xFFD97706))
                        FullParamItem("F1-00", "Клемма DI1", "1", "1 (Пуск Вперед)", "Пуск при замыкании на GND (или COM)", Color(0xFFD97706))
                        FullParamItem("F1-01", "Клемма DI2", "2", "2 (Реверс) / 7", "2: Реверс, 7: Сброс аварии Fault reset", Color(0xFFD97706))
                        FullParamItem("F1-06", "Режим клемм", "0", "0 (2-пров) / 2", "0: 2-проводной, 2: 3-проводной импульсный", Color(0xFFD97706))
                        FullParamItem("F1-08", "Реле TA/TB", "1", "2 (Авария) / 1", "1: Привод в работе RUN, 2: Авария привода", Color(0xFFD97706))

                        GroupHeaderCard("🎯 Группа F5: ПИД, водоснабжение и защита", Color(0xFFD97706))
                        FullParamItem("F5-00", "Источник уставки ПИД", "0", "0 (параметр F5-01)", "0: Уставка в F5-01, 1: AI1, 3: Потенциометр", Color(0xFFD97706))
                        FullParamItem("F5-01", "Уставка давления", "3.5 Bar", "По расчету Bar", "Целевое давление в реальных барах (0.1..1000 Bar)", Color(0xFFD97706))
                        FullParamItem("F5-02", "Обратная связь ПИД", "0", "0 (AI1 / AI)", "0: Клемма AI/AI1, 1: AI2, 2: Modbus", Color(0xFFD97706))
                        FullParamItem("F5-04", "Усиление Kp", "20.0", "15.0..25.0", "Быстродействие поддержания давления", Color(0xFFD97706))
                        FullParamItem("F5-05", "Интеграл Ki", "0.80 с", "0.80..2.00 с", "Устранение статической погрешности", Color(0xFFD97706))
                        FullParamItem("F5-08", "Тип датчика", "0", "1 (4~20мА)", "0: 0-10В, 1: 4-20мА, 2: 0-5В, 3: 0.5-4.5В", Color(0xFFD97706))
                        FullParamItem("F5-09", "Шкала датчика", "16.0 Bar", "Шкала (напр. 10 Bar)", "Верхний предел датчика давления в барах", Color(0xFFD97706))
                        FullParamItem("F5-12", "Частота сна (Dormant)", "20.0 Гц", "22.0..25.0 Гц", "Порог перехода в спящий режим", Color(0xFFD97706))
                        FullParamItem("F5-13", "Задержка сна", "0.0 с", "10.0..15.0 с", "Время работы на частоте сна до останова", Color(0xFFD97706))
                        FullParamItem("F5-17", "Порог пробуждения", "80 %", "80..85 %", "Давление включения насоса (% от уставки)", Color(0xFFD97706))
                        FullParamItem("F5-19..22", "Сухой ход (A52)", "-", "15 с, 48 Гц, 20%", "Защита насоса от работы без воды", Color(0xFFD97706))

                        GroupHeaderCard("💡 Практические схемы применения Серии 900", Color(0xFFD97706))
                        AppExampleCard(
                            title = "🚰 Пример 1: Насос водоснабжения со спящим режимом и защитой A52",
                            wiring = "Датчик 4-20 мА: DC (+24В при F0-06=2) -> (+) датчика; AI -> (-) датчика; GND - общий. Пуск: тумблер DI1-GND.",
                            params = "F0-00=1, F0-01=6, F0-06=2, F0-07=0002, F0-10=20.0, F5-00=0, F5-01=4.0 Bar, F5-08=1, F5-09=10.0, F5-12=23.0, F5-13=12.0 с, F5-17=80%, F5-19=15.0 с, F5-20=48.0 Гц, F5-22=20%, F5-23=20 мин",
                            color = Color(0xFFD97706)
                        )
                        AppExampleCard(
                            title = "🌀 Пример 2: Вентилятор с внешним потенциометром 0-10 В",
                            wiring = "Потенциометр 1-5 кОм: край 1 -> +10V, движок -> AI1, край 2 -> GND. Разрешение: контакт DI1-COM.",
                            params = "F0-00=1, F0-01=2, F0-04=15.0 с, F0-05=15.0 с, F0-09=50.0 Гц",
                            color = Color(0xFFD97706)
                        )
                        AppExampleCard(
                            title = "🔘 Пример 3: 3-проводная схема Пуск/Стоп",
                            wiring = "Кнопка Пуск (НО) между DI1 и GND/COM. Кнопка Стоп (НЗ) между DI3 и GND/COM. Реверс на DI2.",
                            params = "F0-00=1, F1-06=2 (Three-wire type 1), F1-00=1, F1-01=2, F1-02=3",
                            color = Color(0xFFD97706)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Закрыть", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
