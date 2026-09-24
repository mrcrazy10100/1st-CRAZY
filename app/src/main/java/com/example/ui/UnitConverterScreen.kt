package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.CalculatorViewModel
import com.example.converter.ConverterCategory
import com.example.converter.UnitConverter
import com.example.converter.UnitItem
import com.example.ui.components.ButtonType
import com.example.ui.components.CalculatorButton

@Composable
fun UnitConverterScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val category by viewModel.converterCategory.collectAsState()
    val fromUnit by viewModel.fromUnit.collectAsState()
    val toUnit by viewModel.toUnit.collectAsState()
    val inputVal by viewModel.converterInput.collectAsState()
    val outputVal by viewModel.converterOutput.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val categories = remember { ConverterCategory.values() }
    val currentUnits = remember(category) { UnitConverter.getUnitsForCategory(category) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Category Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = cat == category,
                    onClick = { viewModel.setConverterCategory(cat) },
                    label = { Text(cat.displayName) },
                    shape = CircleShape,
                    modifier = Modifier.testTag("cat_chip_${cat.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // From Unit Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { showFromPicker = true }
                .testTag("from_unit_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "From",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    SuggestionChip(
                        onClick = { showFromPicker = true },
                        label = { Text("${fromUnit.name} (${fromUnit.symbol})") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (inputVal.isEmpty()) "0" else inputVal,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("converter_input_text")
                )
            }
        }

        // Swap Button
        IconButton(
            onClick = { viewModel.swapUnits() },
            modifier = Modifier
                .padding(vertical = 4.dp)
                .testTag("swap_units_button")
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap units",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // To Unit Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { showToPicker = true }
                .testTag("to_unit_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "To",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SuggestionChip(
                            onClick = { showToPicker = true },
                            label = { Text("${toUnit.name} (${toUnit.symbol})") }
                        )
                        IconButton(
                            onClick = { clipboardManager.setText(AnnotatedString(outputVal)) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy result",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = outputVal.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("converter_output_text")
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Compact Numeric Keypad for direct input
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            val onKeyTap: (String) -> Unit = { key ->
                when (key) {
                    "C" -> viewModel.setConverterInput("0")
                    "DEL" -> {
                        if (inputVal.length > 1) {
                            viewModel.setConverterInput(inputVal.dropLast(1))
                        } else {
                            viewModel.setConverterInput("0")
                        }
                    }
                    "." -> {
                        if (!inputVal.contains('.')) {
                            viewModel.setConverterInput(inputVal + ".")
                        }
                    }
                    else -> {
                        if (inputVal == "0") {
                            viewModel.setConverterInput(key)
                        } else if (inputVal.length < 12) {
                            viewModel.setConverterInput(inputVal + key)
                        }
                    }
                }
            }

            val rows = listOf(
                listOf("7", "8", "9", "DEL"),
                listOf("4", "5", "6", "C"),
                listOf("1", "2", "3", "."),
                listOf("00", "0")
            )

            // Row 1
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("7", "8", "9").forEach { k ->
                    CalculatorButton(
                        text = k,
                        onClick = { onKeyTap(k) },
                        height = 52.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
                CalculatorButton(
                    text = "DEL",
                    onClick = { onKeyTap("DEL") },
                    type = ButtonType.ACTION,
                    height = 52.dp,
                    modifier = Modifier.weight(1f),
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            // Row 2
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("4", "5", "6").forEach { k ->
                    CalculatorButton(
                        text = k,
                        onClick = { onKeyTap(k) },
                        height = 52.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
                CalculatorButton(
                    text = "C",
                    onClick = { onKeyTap("C") },
                    type = ButtonType.ACTION,
                    height = 52.dp,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("1", "2", "3", ".").forEach { k ->
                    CalculatorButton(
                        text = k,
                        onClick = { onKeyTap(k) },
                        height = 52.dp,
                        fontSize = if (k == ".") 24.sp else 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 4
            Row(modifier = Modifier.fillMaxWidth()) {
                CalculatorButton(
                    text = "0",
                    onClick = { onKeyTap("0") },
                    height = 52.dp,
                    modifier = Modifier.weight(2f)
                )
                CalculatorButton(
                    text = "00",
                    onClick = { onKeyTap("00") },
                    height = 52.dp,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                CalculatorButton(
                    text = "±",
                    onClick = {
                        if (inputVal.startsWith("-")) {
                            viewModel.setConverterInput(inputVal.removePrefix("-"))
                        } else if (inputVal != "0") {
                            viewModel.setConverterInput("-$inputVal")
                        }
                    },
                    height = 52.dp,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // From Unit Selection Dialog
    if (showFromPicker) {
        UnitSelectionDialog(
            title = "Select Source Unit",
            units = currentUnits,
            selectedUnit = fromUnit,
            onSelect = {
                viewModel.setFromUnit(it)
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false }
        )
    }

    // To Unit Selection Dialog
    if (showToPicker) {
        UnitSelectionDialog(
            title = "Select Target Unit",
            units = currentUnits,
            selectedUnit = toUnit,
            onSelect = {
                viewModel.setToUnit(it)
                showToPicker = false
            },
            onDismiss = { showToPicker = false }
        )
    }
}

@Composable
private fun UnitSelectionDialog(
    title: String,
    units: List<UnitItem>,
    selectedUnit: UnitItem,
    onSelect: (UnitItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                units.forEach { unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(unit) }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = unit.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (unit == selectedUnit) FontWeight.Bold else FontWeight.Normal,
                            color = if (unit == selectedUnit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = unit.symbol,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
