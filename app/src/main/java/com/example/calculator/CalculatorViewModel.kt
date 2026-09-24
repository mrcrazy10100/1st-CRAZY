package com.example.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.converter.ConverterCategory
import com.example.converter.UnitConverter
import com.example.converter.UnitItem
import com.example.data.CalculatorDatabase
import com.example.data.HistoryItem
import com.example.data.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AppTab {
    CALCULATOR, CONVERTER, TIP
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository

    init {
        val db = CalculatorDatabase.getDatabase(application)
        repository = HistoryRepository(db.historyDao())
    }

    val history: StateFlow<List<HistoryItem>> = repository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI state for Calculator
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _previewResult = MutableStateFlow("")
    val previewResult: StateFlow<String> = _previewResult.asStateFlow()

    private val _isDegree = MutableStateFlow(true)
    val isDegree: StateFlow<Boolean> = _isDegree.asStateFlow()

    private val _isScientificExpanded = MutableStateFlow(false)
    val isScientificExpanded: StateFlow<Boolean> = _isScientificExpanded.asStateFlow()

    private val _isInverse = MutableStateFlow(false)
    val isInverse: StateFlow<Boolean> = _isInverse.asStateFlow()

    private val _memoryValue = MutableStateFlow<Double?>(null)
    val memoryValue: StateFlow<Double?> = _memoryValue.asStateFlow()

    // App Navigation & Settings
    private val _currentTab = MutableStateFlow(AppTab.CALCULATOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _showHistorySheet = MutableStateFlow(false)
    val showHistorySheet: StateFlow<Boolean> = _showHistorySheet.asStateFlow()

    // Converter State
    private val _converterCategory = MutableStateFlow(ConverterCategory.LENGTH)
    val converterCategory: StateFlow<ConverterCategory> = _converterCategory.asStateFlow()

    private val _fromUnit = MutableStateFlow(UnitConverter.getUnitsForCategory(ConverterCategory.LENGTH)[0])
    val fromUnit: StateFlow<UnitItem> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow(UnitConverter.getUnitsForCategory(ConverterCategory.LENGTH)[1])
    val toUnit: StateFlow<UnitItem> = _toUnit.asStateFlow()

    private val _converterInput = MutableStateFlow("1")
    val converterInput: StateFlow<String> = _converterInput.asStateFlow()

    private val _converterOutput = MutableStateFlow("")
    val converterOutput: StateFlow<String> = _converterOutput.asStateFlow()

    // Tip Calculator State
    private val _billAmount = MutableStateFlow("")
    val billAmount: StateFlow<String> = _billAmount.asStateFlow()

    private val _tipPercentage = MutableStateFlow(15.0)
    val tipPercentage: StateFlow<Double> = _tipPercentage.asStateFlow()

    private val _splitCount = MutableStateFlow(1)
    val splitCount: StateFlow<Int> = _splitCount.asStateFlow()

    private val _roundUpTip = MutableStateFlow(false)
    val roundUpTip: StateFlow<Boolean> = _roundUpTip.asStateFlow()

    private var justCalculated = false

    init {
        updateConverter()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun toggleHistorySheet(show: Boolean? = null) {
        _showHistorySheet.value = show ?: !_showHistorySheet.value
    }

    fun toggleScientific() {
        _isScientificExpanded.value = !_isScientificExpanded.value
    }

    fun toggleAngleUnit() {
        _isDegree.value = !_isDegree.value
        updatePreview()
    }

    fun toggleInverse() {
        _isInverse.value = !_isInverse.value
    }

    // Calculator inputs
    fun onDigit(digit: String) {
        if (justCalculated) {
            _expression.value = digit
            justCalculated = false
        } else {
            _expression.value += digit
        }
        updatePreview()
    }

    fun onOperator(op: String) {
        justCalculated = false
        val current = _expression.value
        if (current.isEmpty()) {
            if (op == "−" || op == "-") {
                _expression.value = "−"
            }
            return
        }

        val lastChar = current.last()
        if (isOperatorChar(lastChar)) {
            // Replace previous operator
            _expression.value = current.dropLast(1) + op
        } else {
            _expression.value += op
        }
        updatePreview()
    }

    fun onDecimal() {
        if (justCalculated) {
            _expression.value = "0."
            justCalculated = false
            updatePreview()
            return
        }

        val current = _expression.value
        if (current.isEmpty() || isOperatorChar(current.last()) || current.last() == '(') {
            _expression.value += "0."
            updatePreview()
            return
        }

        // Find the last number token to check if it already has a dot
        var lastTokenStart = current.length - 1
        while (lastTokenStart >= 0 && (current[lastTokenStart].isDigit() || current[lastTokenStart] == '.')) {
            lastTokenStart--
        }
        val lastNumber = current.substring(lastTokenStart + 1)
        if (!lastNumber.contains('.')) {
            _expression.value += "."
            updatePreview()
        }
    }

    fun onClear() {
        _expression.value = ""
        _previewResult.value = ""
        justCalculated = false
    }

    fun onDelete() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            // Check if removing a multi-character function name like "sin(", "sqrt(", etc.
            val funcNames = listOf("sin(", "cos(", "tan(", "asin(", "acos(", "atan(", "ln(", "log(", "sqrt(")
            var removed = false
            for (fn in funcNames) {
                if (current.endsWith(fn)) {
                    _expression.value = current.dropLast(fn.length)
                    removed = true
                    break
                }
            }
            if (!removed) {
                _expression.value = current.dropLast(1)
            }
            justCalculated = false
            updatePreview()
        }
    }

    fun onParenthesis() {
        if (justCalculated) {
            _expression.value = "("
            justCalculated = false
            updatePreview()
            return
        }

        val current = _expression.value
        val openCount = current.count { it == '(' }
        val closeCount = current.count { it == ')' }

        if (current.isNotEmpty()) {
            val last = current.last()
            if (openCount > closeCount && (last.isDigit() || last == ')' || last == '%' || last == '!' || last == 'π' || last == 'e')) {
                _expression.value += ")"
            } else if (last.isDigit() || last == ')' || last == 'π' || last == 'e') {
                _expression.value += "×("
            } else {
                _expression.value += "("
            }
        } else {
            _expression.value = "("
        }
        updatePreview()
    }

    fun onExplicitParenthesis(char: Char) {
        if (justCalculated && char == '(') {
            _expression.value = "("
            justCalculated = false
            updatePreview()
            return
        }
        _expression.value += char
        justCalculated = false
        updatePreview()
    }

    fun onPercent() {
        val current = _expression.value
        if (current.isNotEmpty() && (current.last().isDigit() || current.last() == ')' || current.last() == 'π' || current.last() == 'e')) {
            _expression.value += "%"
            justCalculated = false
            updatePreview()
        }
    }

    fun onToggleSign() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "−"
            return
        }

        // Try to toggle sign of the entire expression if it's evaluated or simple
        when (val eval = MathEvaluator.evaluate(current, _isDegree.value)) {
            is MathEvaluator.EvalResult.Success -> {
                val toggled = -eval.value
                _expression.value = MathEvaluator.formatResult(toggled)
                updatePreview()
            }
            else -> {
                // Otherwise prepend or wrap
                if (current.startsWith("−")) {
                    _expression.value = current.removePrefix("−")
                } else if (current.startsWith("-")) {
                    _expression.value = current.removePrefix("-")
                } else {
                    _expression.value = "−$current"
                }
                updatePreview()
            }
        }
    }

    fun onFunction(funcName: String) {
        if (justCalculated) {
            _expression.value = ""
            justCalculated = false
        }
        when (funcName) {
            "sqrt" -> _expression.value += "√("
            "square" -> _expression.value += "²"
            "power" -> _expression.value += "^"
            "factorial" -> _expression.value += "!"
            "reciprocal" -> {
                val current = _expression.value
                if (current.isNotEmpty()) {
                    _expression.value = "1/($current)"
                } else {
                    _expression.value = "1/("
                }
            }
            "sin" -> _expression.value += if (_isInverse.value) "asin(" else "sin("
            "cos" -> _expression.value += if (_isInverse.value) "acos(" else "cos("
            "tan" -> _expression.value += if (_isInverse.value) "atan(" else "tan("
            "ln" -> _expression.value += "ln("
            "log" -> _expression.value += "log("
            else -> _expression.value += "$funcName("
        }
        updatePreview()
    }

    fun onConstant(c: String) {
        if (justCalculated) {
            _expression.value = c
            justCalculated = false
        } else {
            val current = _expression.value
            if (current.isNotEmpty() && (current.last().isDigit() || current.last() == ')')) {
                _expression.value += "×$c"
            } else {
                _expression.value += c
            }
        }
        updatePreview()
    }

    fun onEquals() {
        val current = _expression.value.trim()
        if (current.isEmpty()) return

        when (val result = MathEvaluator.evaluate(current, _isDegree.value)) {
            is MathEvaluator.EvalResult.Success -> {
                val formatted = result.formatted
                viewModelScope.launch {
                    repository.insert(current, formatted)
                }
                _expression.value = formatted
                _previewResult.value = ""
                justCalculated = true
            }
            is MathEvaluator.EvalResult.Error -> {
                _previewResult.value = result.message.ifEmpty { "Error" }
            }
        }
    }

    // Memory operations
    fun onMemoryClear() {
        _memoryValue.value = null
    }

    fun onMemoryRecall() {
        val mem = _memoryValue.value ?: return
        val formatted = MathEvaluator.formatResult(mem)
        if (justCalculated) {
            _expression.value = formatted
            justCalculated = false
        } else {
            _expression.value += formatted
        }
        updatePreview()
    }

    fun onMemoryAdd() {
        val eval = getCurrentValue() ?: return
        val currentMem = _memoryValue.value ?: 0.0
        _memoryValue.value = currentMem + eval
    }

    fun onMemorySubtract() {
        val eval = getCurrentValue() ?: return
        val currentMem = _memoryValue.value ?: 0.0
        _memoryValue.value = currentMem - eval
    }

    fun onMemoryStore() {
        val eval = getCurrentValue() ?: return
        _memoryValue.value = eval
    }

    private fun getCurrentValue(): Double? {
        val current = _expression.value.trim()
        if (current.isEmpty()) return null
        return when (val eval = MathEvaluator.evaluate(current, _isDegree.value)) {
            is MathEvaluator.EvalResult.Success -> eval.value
            else -> null
        }
    }

    private fun updatePreview() {
        val current = _expression.value.trim()
        if (current.isEmpty()) {
            _previewResult.value = ""
            return
        }

        // Only compute preview if it has at least one operator or function or is different
        val hasOp = current.any { isOperatorChar(it) || it == '%' || it == '!' || it == '^' || it == '√' || it.isLetter() }
        if (!hasOp) {
            _previewResult.value = ""
            return
        }

        when (val result = MathEvaluator.evaluate(current, _isDegree.value)) {
            is MathEvaluator.EvalResult.Success -> {
                _previewResult.value = "= ${result.formatted}"
            }
            is MathEvaluator.EvalResult.Error -> {
                _previewResult.value = ""
            }
        }
    }

    private fun isOperatorChar(c: Char): Boolean {
        return c == '+' || c == '−' || c == '-' || c == '×' || c == '*' || c == '÷' || c == '/' || c == '^'
    }

    // History interaction
    fun onSelectHistory(item: HistoryItem, useResultOnly: Boolean = false) {
        if (useResultOnly) {
            _expression.value = item.result
        } else {
            _expression.value = item.expression
        }
        justCalculated = false
        updatePreview()
        _showHistorySheet.value = false
    }

    fun onDeleteHistory(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun onClearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // Unit Converter methods
    fun setConverterCategory(cat: ConverterCategory) {
        _converterCategory.value = cat
        val units = UnitConverter.getUnitsForCategory(cat)
        _fromUnit.value = units[0]
        _toUnit.value = if (units.size > 1) units[1] else units[0]
        updateConverter()
    }

    fun setFromUnit(unit: UnitItem) {
        _fromUnit.value = unit
        updateConverter()
    }

    fun setToUnit(unit: UnitItem) {
        _toUnit.value = unit
        updateConverter()
    }

    fun swapUnits() {
        val temp = _fromUnit.value
        _fromUnit.value = _toUnit.value
        _toUnit.value = temp
        updateConverter()
    }

    fun setConverterInput(input: String) {
        _converterInput.value = input
        updateConverter()
    }

    private fun updateConverter() {
        val value = _converterInput.value.toDoubleOrNull() ?: 0.0
        val converted = UnitConverter.convert(_converterCategory.value, _fromUnit.value, _toUnit.value, value)
        _converterOutput.value = UnitConverter.format(converted)
    }

    // Tip Calculator methods
    fun setBillAmount(amount: String) {
        _billAmount.value = amount
    }

    fun setTipPercentage(percentage: Double) {
        _tipPercentage.value = percentage
    }

    fun setSplitCount(count: Int) {
        if (count >= 1) {
            _splitCount.value = count
        }
    }

    fun toggleRoundUp() {
        _roundUpTip.value = !_roundUpTip.value
    }
}
