package com.example.converter

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class ConverterCategory(val displayName: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temperature"),
    AREA("Area"),
    VOLUME("Volume"),
    SPEED("Speed"),
    STORAGE("Digital")
}

data class UnitItem(val name: String, val symbol: String, val factorToBase: Double = 1.0)

object UnitConverter {

    fun getUnitsForCategory(category: ConverterCategory): List<UnitItem> {
        return when (category) {
            ConverterCategory.LENGTH -> listOf(
                UnitItem("Meter", "m", 1.0),
                UnitItem("Kilometer", "km", 1000.0),
                UnitItem("Centimeter", "cm", 0.01),
                UnitItem("Millimeter", "mm", 0.001),
                UnitItem("Inch", "in", 0.0254),
                UnitItem("Foot", "ft", 0.3048),
                UnitItem("Yard", "yd", 0.9144),
                UnitItem("Mile", "mi", 1609.344)
            )
            ConverterCategory.WEIGHT -> listOf(
                UnitItem("Kilogram", "kg", 1.0),
                UnitItem("Gram", "g", 0.001),
                UnitItem("Milligram", "mg", 0.000001),
                UnitItem("Pound", "lb", 0.45359237),
                UnitItem("Ounce", "oz", 0.02834952),
                UnitItem("Metric Ton", "t", 1000.0)
            )
            ConverterCategory.TEMPERATURE -> listOf(
                UnitItem("Celsius", "°C", 1.0),
                UnitItem("Fahrenheit", "°F", 1.0),
                UnitItem("Kelvin", "K", 1.0)
            )
            ConverterCategory.AREA -> listOf(
                UnitItem("Square Meter", "m²", 1.0),
                UnitItem("Square Kilometer", "km²", 1_000_000.0),
                UnitItem("Square Foot", "ft²", 0.09290304),
                UnitItem("Square Yard", "yd²", 0.83612736),
                UnitItem("Acre", "ac", 4046.85642),
                UnitItem("Hectare", "ha", 10000.0)
            )
            ConverterCategory.VOLUME -> listOf(
                UnitItem("Liter", "L", 1.0),
                UnitItem("Milliliter", "mL", 0.001),
                UnitItem("US Gallon", "gal", 3.78541),
                UnitItem("US Quart", "qt", 0.946353),
                UnitItem("US Pint", "pt", 0.473176),
                UnitItem("US Cup", "cup", 0.24),
                UnitItem("Fluid Ounce", "fl oz", 0.0295735)
            )
            ConverterCategory.SPEED -> listOf(
                UnitItem("Kilometers/Hour", "km/h", 1.0),
                UnitItem("Meters/Second", "m/s", 3.6),
                UnitItem("Miles/Hour", "mph", 1.60934),
                UnitItem("Knot", "kn", 1.852)
            )
            ConverterCategory.STORAGE -> listOf(
                UnitItem("Byte", "B", 1.0),
                UnitItem("Kilobyte", "KB", 1024.0),
                UnitItem("Megabyte", "MB", 1048576.0),
                UnitItem("Gigabyte", "GB", 1073741824.0),
                UnitItem("Terabyte", "TB", 1099511627776.0)
            )
        }
    }

    fun convert(
        category: ConverterCategory,
        fromUnit: UnitItem,
        toUnit: UnitItem,
        value: Double
    ): Double {
        if (category == ConverterCategory.TEMPERATURE) {
            return convertTemperature(fromUnit.symbol, toUnit.symbol, value)
        }
        val inBase = value * fromUnit.factorToBase
        return inBase / toUnit.factorToBase
    }

    private fun convertTemperature(from: String, to: String, value: Double): Double {
        if (from == to) return value
        val celsius = when (from) {
            "°C" -> value
            "°F" -> (value - 32.0) * (5.0 / 9.0)
            "K" -> value - 273.15
            else -> value
        }
        return when (to) {
            "°C" -> celsius
            "°F" -> (celsius * (9.0 / 5.0)) + 32.0
            "K" -> celsius + 273.15
            else -> celsius
        }
    }

    fun format(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "-"
        val df = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.US))
        return df.format(value)
    }
}
