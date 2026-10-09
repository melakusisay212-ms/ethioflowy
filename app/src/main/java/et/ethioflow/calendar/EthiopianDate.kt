package et.ethioflow.calendar

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor

/**
 * Pure-Kotlin Ethiopian (Ethiopic) calendar.
 * 13 months: 12 × 30 days + Pagume (5 or 6 days in leap years).
 */
data class EthiopianDate(
    val year: Int,
    val month: Int,   // 1–13
    val day: Int      // 1–30 (or 1–5/6 for Pagume)
) : Comparable<EthiopianDate> {

    init {
        require(month in 1..13) { "Month must be 1–13, got $month" }
        val maxDay = daysInMonth(year, month)
        require(day in 1..maxDay) { "Day $day invalid for month $month year $year (max $maxDay)" }
    }

    fun toGregorian(): LocalDate {
        val jdn = ethiopianToJdn(year, month, day)
        return jdnToGregorian(jdn)
    }

    fun monthName(amharic: Boolean = true): String =
        if (amharic) MONTH_NAMES_AM[month - 1] else MONTH_NAMES_EN[month - 1]

    fun dayOfWeek(): Int = toGregorian().dayOfWeek.value % 7

    fun dayOfWeekName(amharic: Boolean = true): String {
        val idx = toGregorian().dayOfWeek.value % 7
        return if (amharic) DAY_NAMES_AM[idx] else DAY_NAMES_EN[idx]
    }

    fun plusDays(days: Long): EthiopianDate {
        val jdn = ethiopianToJdn(year, month, day) + days
        return fromJdn(jdn)
    }

    fun minusDays(days: Long): EthiopianDate = plusDays(-days)

    override fun compareTo(other: EthiopianDate): Int = when {
        year != other.year -> year - other.year
        month != other.month -> month - other.month
        else -> day - other.day
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

        /** Ethiopic epoch in JDN (Meskerem 1, year 1 ≈ day before first day offset). */
        private const val ETH_EPOCH = 1723856L

        fun now(): EthiopianDate = fromGregorian(LocalDate.now())

        fun fromGregorian(date: LocalDate): EthiopianDate {
            val jdn = gregorianToJdn(date.year, date.monthValue, date.dayOfMonth)
            return fromJdn(jdn)
        }

        fun isLeapYear(year: Int): Boolean = year % 4 == 3

        fun daysInMonth(year: Int, month: Int): Int =
            if (month == 13) if (isLeapYear(year)) 6 else 5 else 30

        /**
         * Convert JDN → Ethiopian date (Abushakir / standard algorithm).
         */
        fun fromJdn(jdn: Long): EthiopianDate {
            val r = ((jdn - ETH_EPOCH) % 1461 + 1461) % 1461  // safe positive mod
            val n = (r % 365) + 365 * (r / 1460)
            val year = (4 * ((jdn - ETH_EPOCH) / 1461) + (r / 365) - (r / 1460)).toInt()
            val month = (n / 30 + 1).toInt()
            val day = (n % 30 + 1).toInt()
            // Clamp defensively in case of edge arithmetic
            val safeMonth = month.coerceIn(1, 13)
            val safeDay = day.coerceIn(1, daysInMonth(year, safeMonth))
            return EthiopianDate(year, safeMonth, safeDay)
        }

        fun ethiopianToJdn(year: Int, month: Int, day: Int): Long {
            return ETH_EPOCH - 1 + 365L * (year - 1) + (year / 4) + 30L * (month - 1) + day
        }

        fun gregorianToJdn(y: Int, m: Int, d: Int): Long {
            val a = (14 - m) / 12
            val y2 = y + 4800 - a
            val m2 = m + 12 * a - 3
            return d + (153 * m2 + 2) / 5 + 365L * y2 + y2 / 4 - y2 / 100 + y2 / 400 - 32045L
        }

        fun jdnToGregorian(jdn: Long): LocalDate {
            val a = jdn + 32044
            val b = (4 * a + 3) / 146097
            val c = a - (146097 * b) / 4
            val d = (4 * c + 3) / 1461
            val e = c - (1461 * d) / 4
            val m = (5 * e + 2) / 153
            val day = (e - (153 * m + 2) / 5 + 1).toInt()
            val month = (m + 3 - 12 * (m / 10)).toInt()
            val year = (100 * b + d - 4800 + m / 10).toInt()
            return LocalDate.of(year, month, day)
        }
    }
}
