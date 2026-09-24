package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

object MathEvaluator {

    sealed class EvalResult {
        data class Success(val value: Double, val formatted: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    fun evaluate(expression: String, isDegree: Boolean): EvalResult {
        if (expression.isBlank()) return EvalResult.Error("")
        return try {
            val sanitized = sanitize(expression)
            val tokens = tokenize(sanitized)
            val parser = Parser(tokens, isDegree)
            val result = parser.parse()
            if (result.isNaN()) {
                EvalResult.Error("Invalid input")
            } else if (result.isInfinite()) {
                EvalResult.Error("Cannot divide by 0")
            } else {
                EvalResult.Success(result, formatResult(result))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (e: Exception) {
            EvalResult.Error("Format error")
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        if (abs(value) < 1e-12 && value != 0.0) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            return df.format(value)
        }

        // Check if value is very large or very small
        val absVal = abs(value)
        if (absVal >= 1e12 || (absVal > 0 && absVal < 1e-6)) {
            val df = DecimalFormat("0.########E0", DecimalFormatSymbols(Locale.US))
            return df.format(value).replace("E", " × 10^")
        }

        // Clean precision to up to 10 decimal places to eliminate floating point artifacts like 0.30000000000000004
        return try {
            val bd = BigDecimal(value, MathContext(12, RoundingMode.HALF_UP))
            val stripped = bd.stripTrailingZeros()
            val plain = stripped.toPlainString()

            // Format with commas for thousands
            val parts = plain.split(".")
            val intPart = parts[0]
            val decPart = if (parts.size > 1) parts[1] else null

            val isNegative = intPart.startsWith("-")
            val unsignedInt = if (isNegative) intPart.substring(1) else intPart

            val formattedInt = unsignedInt.reversed().chunked(3).joinToString(",").reversed()
            val resultInt = if (isNegative) "-$formattedInt" else formattedInt

            if (decPart != null && decPart.isNotEmpty()) {
                "$resultInt.$decPart"
            } else {
                resultInt
            }
        } catch (e: Exception) {
            val df = DecimalFormat("#,##0.##########", DecimalFormatSymbols(Locale.US))
            df.format(value)
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("π", "PI")
            .replace("²", "^2")
            .replace("√", "sqrt")
            .replace(" ", "")
    }

    private enum class TokenType {
        NUMBER, PLUS, MINUS, MULTIPLY, DIVIDE, POWER, PERCENT, FACTORIAL,
        LPAREN, RPAREN, IDENTIFIER
    }

    private data class Token(val type: TokenType, val text: String, val value: Double = 0.0)

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isDigit() || (c == '.' && i + 1 < input.length && input[i + 1].isDigit()) -> {
                    val start = i
                    var hasDot = false
                    while (i < input.length && (input[i].isDigit() || input[i] == '.')) {
                        if (input[i] == '.') {
                            if (hasDot) break
                            hasDot = true
                        }
                        i++
                    }
                    val numStr = input.substring(start, i)
                    val value = numStr.toDoubleOrNull() ?: 0.0
                    tokens.add(Token(TokenType.NUMBER, numStr, value))
                }
                c == '+' -> {
                    tokens.add(Token(TokenType.PLUS, "+"))
                    i++
                }
                c == '-' -> {
                    tokens.add(Token(TokenType.MINUS, "-"))
                    i++
                }
                c == '*' -> {
                    tokens.add(Token(TokenType.MULTIPLY, "*"))
                    i++
                }
                c == '/' -> {
                    tokens.add(Token(TokenType.DIVIDE, "/"))
                    i++
                }
                c == '^' -> {
                    tokens.add(Token(TokenType.POWER, "^"))
                    i++
                }
                c == '%' -> {
                    tokens.add(Token(TokenType.PERCENT, "%"))
                    i++
                }
                c == '!' -> {
                    tokens.add(Token(TokenType.FACTORIAL, "!"))
                    i++
                }
                c == '(' -> {
                    tokens.add(Token(TokenType.LPAREN, "("))
                    i++
                }
                c == ')' -> {
                    tokens.add(Token(TokenType.RPAREN, ")"))
                    i++
                }
                c.isLetter() -> {
                    val start = i
                    while (i < input.length && (input[i].isLetter() || input[i].isDigit())) {
                        i++
                    }
                    val word = input.substring(start, i)
                    tokens.add(Token(TokenType.IDENTIFIER, word))
                }
                else -> {
                    // Ignore or skip unrecognized
                    i++
                }
            }
        }
        return insertImplicitMultiplication(tokens)
    }

    private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
        if (tokens.isEmpty()) return tokens
        val result = mutableListOf<Token>()
        for (i in tokens.indices) {
            val current = tokens[i]
            result.add(current)
            if (i < tokens.size - 1) {
                val next = tokens[i + 1]
                val currentCanMultiply = current.type in listOf(
                    TokenType.NUMBER,
                    TokenType.RPAREN,
                    TokenType.PERCENT,
                    TokenType.FACTORIAL
                ) || (current.type == TokenType.IDENTIFIER && (current.text.equals("PI", true) || current.text.equals("e", true)))

                val nextCanBeMultiplied = next.type in listOf(
                    TokenType.NUMBER,
                    TokenType.LPAREN,
                    TokenType.IDENTIFIER
                )

                if (currentCanMultiply && nextCanBeMultiplied) {
                    result.add(Token(TokenType.MULTIPLY, "*"))
                }
            }
        }
        return result
    }

    private class Parser(private val tokens: List<Token>, private val isDegree: Boolean) {
        private var pos = 0

        private fun peek(): Token? = if (pos < tokens.size) tokens[pos] else null
        private fun consume(): Token = tokens[pos++]
        private fun match(type: TokenType): Boolean {
            if (peek()?.type == type) {
                pos++
                return true
            }
            return false
        }

        fun parse(): Double {
            if (tokens.isEmpty()) return 0.0
            val result = parseExpression()
            if (pos < tokens.size) {
                throw IllegalArgumentException("Unexpected token: ${peek()?.text}")
            }
            return result
        }

        // Expression = Term (('+' | '-') Term)*
        private fun parseExpression(): Double {
            var left = parseTerm()
            while (pos < tokens.size) {
                val token = peek() ?: break
                if (token.type == TokenType.PLUS) {
                    consume()
                    val right = parseTerm()
                    left += right
                } else if (token.type == TokenType.MINUS) {
                    consume()
                    val right = parseTerm()
                    left -= right
                } else {
                    break
                }
            }
            return left
        }

        // Term = Factor (('*' | '/') Factor)*
        private fun parseTerm(): Double {
            var left = parseFactor()
            while (pos < tokens.size) {
                val token = peek() ?: break
                if (token.type == TokenType.MULTIPLY) {
                    consume()
                    val right = parseFactor()
                    left *= right
                } else if (token.type == TokenType.DIVIDE) {
                    consume()
                    val right = parseFactor()
                    if (right == 0.0) throw ArithmeticException("Cannot divide by 0")
                    left /= right
                } else {
                    break
                }
            }
            return left
        }

        // Factor = Unary ('^' Factor)?
        private fun parseFactor(): Double {
            val base = parseUnary()
            if (match(TokenType.POWER)) {
                val exponent = parseFactor() // Right-associative
                return base.pow(exponent)
            }
            return base
        }

        // Unary = ('+' | '-')? Postfix
        private fun parseUnary(): Double {
            if (match(TokenType.PLUS)) {
                return parseUnary()
            }
            if (match(TokenType.MINUS)) {
                return -parseUnary()
            }
            return parsePostfix()
        }

        // Postfix = Primary ('%' | '!')*
        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (pos < tokens.size) {
                val token = peek() ?: break
                if (token.type == TokenType.PERCENT) {
                    consume()
                    value /= 100.0
                } else if (token.type == TokenType.FACTORIAL) {
                    consume()
                    value = factorial(value)
                } else {
                    break
                }
            }
            return value
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != floor(n)) throw ArithmeticException("Invalid factorial")
            if (n > 170) return Double.POSITIVE_INFINITY
            var res = 1.0
            val intN = n.toInt()
            for (i in 2..intN) {
                res *= i
            }
            return res
        }

        // Primary = NUMBER | IDENTIFIER | '(' Expression ')'
        private fun parsePrimary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")
            when (token.type) {
                TokenType.NUMBER -> {
                    consume()
                    return token.value
                }
                TokenType.LPAREN -> {
                    consume()
                    val expr = parseExpression()
                    if (!match(TokenType.RPAREN)) {
                        // Allow auto-closing of parentheses if at end of expression
                        if (pos < tokens.size) {
                            throw IllegalArgumentException("Missing closing parenthesis")
                        }
                    }
                    return expr
                }
                TokenType.IDENTIFIER -> {
                    consume()
                    val name = token.text.lowercase()
                    when (name) {
                        "pi" -> return Math.PI
                        "e" -> return Math.E
                        "sqrt" -> {
                            val arg = parseFunctionArg()
                            if (arg < 0) throw ArithmeticException("Invalid square root")
                            return sqrt(arg)
                        }
                        "cbrt" -> {
                            val arg = parseFunctionArg()
                            return cbrt(arg)
                        }
                        "sin" -> {
                            val arg = parseFunctionArg()
                            val rad = if (isDegree) Math.toRadians(arg) else arg
                            return sin(rad)
                        }
                        "cos" -> {
                            val arg = parseFunctionArg()
                            val rad = if (isDegree) Math.toRadians(arg) else arg
                            return cos(rad)
                        }
                        "tan" -> {
                            val arg = parseFunctionArg()
                            val rad = if (isDegree) Math.toRadians(arg) else arg
                            return tan(rad)
                        }
                        "asin" -> {
                            val arg = parseFunctionArg()
                            if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Out of domain")
                            val rad = asin(arg)
                            return if (isDegree) Math.toDegrees(rad) else rad
                        }
                        "acos" -> {
                            val arg = parseFunctionArg()
                            if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Out of domain")
                            val rad = acos(arg)
                            return if (isDegree) Math.toDegrees(rad) else rad
                        }
                        "atan" -> {
                            val arg = parseFunctionArg()
                            val rad = atan(arg)
                            return if (isDegree) Math.toDegrees(rad) else rad
                        }
                        "ln" -> {
                            val arg = parseFunctionArg()
                            if (arg <= 0) throw ArithmeticException("Invalid logarithm")
                            return ln(arg)
                        }
                        "log" -> {
                            val arg = parseFunctionArg()
                            if (arg <= 0) throw ArithmeticException("Invalid logarithm")
                            return log10(arg)
                        }
                        "abs" -> {
                            val arg = parseFunctionArg()
                            return abs(arg)
                        }
                        else -> throw IllegalArgumentException("Unknown function: $name")
                    }
                }
                else -> throw IllegalArgumentException("Unexpected token: ${token.text}")
            }
        }

        private fun parseFunctionArg(): Double {
            if (match(TokenType.LPAREN)) {
                val arg = parseExpression()
                match(TokenType.RPAREN)
                return arg
            }
            return parseFactor()
        }
    }
}
