package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.calculator.CalculatorViewModel
import com.example.calculator.ThemeMode
import com.example.ui.components.ButtonType
import com.example.ui.components.CalculatorButton

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsState()
    val previewResult by viewModel.previewResult.collectAsState()
    val isDegree by viewModel.isDegree.collectAsState()
    val isScientificExpanded by viewModel.isScientificExpanded.collectAsState()
    val isInverse by viewModel.isInverse.collectAsState()
    val memoryValue by viewModel.memoryValue.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Auto scroll expression to end when it changes
    LaunchedEffect(expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Angle unit badge button (DEG / RAD)
            FilterChip(
                selected = !isDegree,
                onClick = { viewModel.toggleAngleUnit() },
                label = {
                    Text(
                        text = if (isDegree) "DEG" else "RAD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("deg_rad_toggle"),
                shape = CircleShape
            )

            // Memory Active Badge
            if (memoryValue != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "M",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Scientific keypad expand button
            FilledTonalIconButton(
                onClick = { viewModel.toggleScientific() },
                modifier = Modifier.testTag("scientific_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = "Scientific keyboard",
                    tint = if (isScientificExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // History button
            IconButton(
                onClick = { viewModel.toggleHistorySheet(true) },
                modifier = Modifier.testTag("history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History"
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Theme toggle
            IconButton(
                onClick = {
                    val nextMode = when (themeMode) {
                        ThemeMode.LIGHT -> ThemeMode.DARK
                        ThemeMode.DARK -> ThemeMode.SYSTEM
                        ThemeMode.SYSTEM -> ThemeMode.LIGHT
                    }
                    viewModel.setThemeMode(nextMode)
                },
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (themeMode == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle theme"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Display Area (Expression and Live Preview)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Main Expression
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = if (expression.length > 12) 36.sp else 46.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (expression.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.testTag("expression_display")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live Preview Result
            AnimatedVisibility(
                visible = previewResult.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = previewResult,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.testTag("preview_display")
                )
            }
        }

        // Memory Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val memoryButtons = listOf(
                "MC" to { viewModel.onMemoryClear() },
                "MR" to { viewModel.onMemoryRecall() },
                "M+" to { viewModel.onMemoryAdd() },
                "M-" to { viewModel.onMemorySubtract() },
                "MS" to { viewModel.onMemoryStore() }
            )

            for ((label, action) in memoryButtons) {
                val isEnabled = if (label == "MC" || label == "MR") memoryValue != null else true
                TextButton(
                    onClick = action,
                    enabled = isEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .testTag("btn_mem_$label"),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (isEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Scientific Keyboard (Expandable)
        AnimatedVisibility(
            visible = isScientificExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(bottom = 6.dp)
            ) {
                // Scientific Row 1: 2nd, sin, cos, tan, ln
                Row(modifier = Modifier.fillMaxWidth()) {
                    CalculatorButton(
                        text = "2nd",
                        onClick = { viewModel.toggleInverse() },
                        type = ButtonType.SCIENTIFIC,
                        isSelected = isInverse,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = if (isInverse) "sin⁻¹" else "sin",
                        onClick = { viewModel.onFunction("sin") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = if (isInverse) "cos⁻¹" else "cos",
                        onClick = { viewModel.onFunction("cos") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = if (isInverse) "tan⁻¹" else "tan",
                        onClick = { viewModel.onFunction("tan") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "ln",
                        onClick = { viewModel.onFunction("ln") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Scientific Row 2: π, e, √, x², xʸ
                Row(modifier = Modifier.fillMaxWidth()) {
                    CalculatorButton(
                        text = "π",
                        onClick = { viewModel.onConstant("π") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "e",
                        onClick = { viewModel.onConstant("e") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "√",
                        onClick = { viewModel.onFunction("sqrt") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "x²",
                        onClick = { viewModel.onFunction("square") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "xʸ",
                        onClick = { viewModel.onFunction("power") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Scientific Row 3: (, ), !, 1/x, log
                Row(modifier = Modifier.fillMaxWidth()) {
                    CalculatorButton(
                        text = "(",
                        onClick = { viewModel.onExplicitParenthesis('(') },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = ")",
                        onClick = { viewModel.onExplicitParenthesis(')') },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "x!",
                        onClick = { viewModel.onFunction("factorial") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "1/x",
                        onClick = { viewModel.onFunction("reciprocal") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "log",
                        onClick = { viewModel.onFunction("log") },
                        type = ButtonType.SCIENTIFIC,
                        height = 46.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Standard Keypad Rows
        // Row 1: AC, DEL, %, ÷
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(
                text = stringResource(R.string.clear),
                onClick = { viewModel.onClear() },
                type = ButtonType.ACTION,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "DEL",
                onClick = { viewModel.onDelete() },
                type = ButtonType.ACTION,
                modifier = Modifier.weight(1f),
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            CalculatorButton(
                text = stringResource(R.string.percent),
                onClick = { viewModel.onPercent() },
                type = ButtonType.OPERATOR,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = stringResource(R.string.divide),
                onClick = { viewModel.onOperator("÷") },
                type = ButtonType.OPERATOR,
                fontSize = 26.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(
                text = "7",
                onClick = { viewModel.onDigit("7") },
                type = ButtonType.NUMBER,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "8",
                onClick = { viewModel.onDigit("8") },
                type = ButtonType.NUMBER,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "9",
                onClick = { viewModel.onDigit("9") },
                type = ButtonType.NUMBER,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = stringResource(R.string.multiply),
                onClick = { viewModel.onOperator("×") },
                type = ButtonType.OPERATOR,
                fontSize = 26.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: 4, 5, 6, −
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(
                text = "4",
                onClick = { viewModel.onDigit("4") },
                type = ButtonType.NUMBER,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "5",
                onClick = { viewModel.onDigit("5") },
                type = ButtonType.NUMBER,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "6",
                onClick = { viewModel.onDigit("6") },
                type = ButtonType.NUMBER,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = stringResource(R.string.subtract),
                onClick = { viewModel.onOperator("−") },
                type = ButtonType.OPERATOR,
                fontSize = 28.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: 1, 2, 3, +
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(
                text = "1",
                onClick = { viewModel.onDigit("1") },
                type = ButtonType.NUMBER,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "2",
                onClick = { viewModel.onDigit("2") },
                type = ButtonType.NUMBER,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "3",
                onClick = { viewModel.onDigit("3") },
                type = ButtonType.NUMBER,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = stringResource(R.string.add),
                onClick = { viewModel.onOperator("+") },
                type = ButtonType.OPERATOR,
                fontSize = 26.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5: ±, 0, ., =
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(
                text = stringResource(R.string.plus_minus),
                onClick = { viewModel.onToggleSign() },
                type = ButtonType.NUMBER,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "0",
                onClick = { viewModel.onDigit("0") },
                type = ButtonType.NUMBER,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = stringResource(R.string.decimal_point),
                onClick = { viewModel.onDecimal() },
                type = ButtonType.NUMBER,
                fontSize = 28.sp,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = stringResource(R.string.equals),
                onClick = { viewModel.onEquals() },
                type = ButtonType.EQUALS,
                fontSize = 28.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
