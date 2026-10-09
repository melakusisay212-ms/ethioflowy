package et.ethioflow.calendar

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor

/**
 * Pure-Kotlin Ethiopian (Ethiopic) calendar.
 * 13 months: 12 × 30 days + Pagume (5 or 6 days in leap years).
 * Ethiopian year ≈ Gregorian year − 7/8.
 */
data class EthiopianDate(
    val year: Int,
    val month: Int,   // 1–13
    val day: Int      // 1–30 (or 1–5/6 for Pagume)
) : Comparable<EthiopianDate> {

    init {
        require(month in 1..13) { "Month must be 1–13" }
        val maxDay = if (month == 13) if (isLeapYear(year)) 6 else 5 else 30
        require(day in 1..maxDay) { "Day $day invalid for month $month year $year" }
    }

    fun toGregorian(): LocalDate {
        // JDN of Ethiopian epoch (Meskerem 1, 1 EC ≈ Aug 29, 8 CE)
        val jdn = ethiopianToJdn(year, month, day)
        return jdnToGregorian(jdn)
    }

    fun monthName(amharic: Boolean = true): String =
        if (amharic) MONTH_NAMES_AM[month - 1] else MONTH_NAMES_EN[month - 1]

    fun dayOfWeek(): Int {
        // 0 = Sunday … 6 = Saturday (Gregorian-aligned)
        return toGregorian().dayOfWeek.value % 7
    }

    fun dayOfWeekName(amharic: Boolean = true): String {
        val idx = toGregorian().dayOfWeek.value % 7
        return if (amharic) DAY_NAMES_AM[idx] else DAY_NAMES_EN[idx]
    }

    fun plusDays(days: Long): EthiopianDate {
        val jdn = ethiopianToJdn(year, month, day) + days
        return fromJdn(jdn)
    }

    fun minusDays(days: Long): EthiopianDate = plusDays(-days)

    override fun compareTo(other: EthiopianDate): Int {
        return when {
            year != other.year -> year - other.year
            month != other.month -> month - other.month
            else -> day - other.day
        }
    }

    override fun toString(): String = "%04d-%02d-%02d".format(year, month, day)

    companion object {
        val MONTH_NAMES_AM = listOf(
            "መስከረም", "ጥቅምት", "ኅዳር", "ታኅሣሥ",
            "ጥር", "የካቲት", "መጋቢት", "ሚያዝያ",
            "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጉሜ"
        )
        val MONTH_NAMES_EN = listOf(
            "Meskerem", "Tikimt", "Hidar", "Tahsas",
            "Tir", "Yekatit", "Megabit", "Miazia",
            "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
        )
        val DAY_NAMES_AM = listOf("እሁድ", "ሰኞ", "ማክሰኞ", "ረቡዕ", "ሐሙስ", "አርብ", "ቅዳሜ")
        val DAY_NAMES_EN = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        fun now(): EthiopianDate = fromGregorian(LocalDate.now())

        fun fromGregorian(date: LocalDate): EthiopianDate {
            val jdn = gregorianToJdn(date.year, date.monthValue, date.dayOfMonth)
            return fromJdn(jdn)
        }

        fun isLeapYear(year: Int): Boolean = year % 4 == 3

        fun daysInMonth(year: Int, month: Int): Int =
            if (month == 13) if (isLeapYear(year)) 6 else 5 else 30

        // ---- JDN conversion (standard algorithm) ----

        private fun ethiopianToJdn(year: Int, month: Int, day: Int): Long {
            return (1723856L + 365L * (year - 1) +
                    floor((year / 4.0)).toLong() +
                    30L * (month - 1) + day - 1)
        }

        private fun fromJdn(jdn: Long): EthiopianDate {
            val r = (jdn - 1723856).toDouble()
            val year = floor((r - floor((r + 366) / 1461.0) + 3) / 365.0).toInt() + 1
            val t = floor((r + 366 - 365.0 * (year - 1) - floor((year - 1) / 4.0)) / 30.0).toInt()
            val month = t + 1
            val day = (r + 1 - 365.0 * (year - 1) - floor((year - 1) / 4.0) - 30.0 * t).toInt()
            return EthiopianDate(year, month, day)
        }

        private fun gregorianToJdn(y: Int, m: Int, d: Int): Long {
            val a = (14 - m) / 12
            val y2 = y + 4800 - a
            val m2 = m + 12 * a - 3
            return d + (153 * m2 + 2) / 5 + 365L * y2 + y2 / 4 - y2 / 100 + y2 / 400 - 32045L
        }

        private fun jdnToGregorian(jdn: Long): LocalDate {
            val a = jdn + 32044
            val b = (4 * a + 3) / 146097
            val c = a - (146097 * b) / 4
            val d = (4 * c + 3) / 1461
            val e = c - (1461 * d) / 4
            val m = (5 * e + 2) / 153
            val day = e - (153 * m + 2) / 5 + 1
            val month = m + 3 - 12 * (m / 10)
            val year = 100 * b + d - 4800 + m / 10
            return LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        }
    }
}
