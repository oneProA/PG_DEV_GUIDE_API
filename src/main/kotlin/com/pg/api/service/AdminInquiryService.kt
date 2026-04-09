package com.pg.api.service

import com.pg.api.domain.AdminInquiry
import com.pg.api.domain.AdminInquiryFile
import com.pg.api.repository.AdminInquiryMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Service
class AdminInquiryService(private val adminInquiryMapper: AdminInquiryMapper) {
    private val businessStartTime: LocalTime = LocalTime.of(9, 0)
    private val businessEndTime: LocalTime = LocalTime.of(18, 0)

    fun getInquiries(
        page: Int,
        size: Int,
        keyword: String?,
        status: String?,
        categoryCode: String?,
        fromDate: String?,
        toDate: String?,
    ): Pair<List<AdminInquiry>, Long> {
        val sanitizedPage = page.coerceAtLeast(1)
        val sanitizedSize = size.coerceIn(1, 100)
        val normalizedKeyword = keyword?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedStatus = normalizeStatus(status)
        val normalizedCategoryCode = normalizeCategoryCode(categoryCode)
        val sortDirection = resolveSortDirection(normalizedStatus)
        val ignoreDateFilter = normalizedStatus == "RECEIVED" || normalizedStatus == "IN_PROGRESS"
        val (fromDateTime, toDateTimeExclusive) = if (ignoreDateFilter) {
            LocalDate.of(1970, 1, 1).atStartOfDay() to LocalDate.of(2999, 12, 31).plusDays(1).atStartOfDay()
        } else {
            normalizeDateRange(fromDate, toDate)
        }
        val offset = (sanitizedPage - 1) * sanitizedSize

        val items = adminInquiryMapper.findPage(
            offset = offset,
            limit = sanitizedSize,
            keyword = normalizedKeyword,
            status = normalizedStatus,
            categoryCode = normalizedCategoryCode,
            sortDirection = sortDirection,
            fromDateTime = fromDateTime,
            toDateTimeExclusive = toDateTimeExclusive,
        )
        val totalCount = adminInquiryMapper.countAll(
            keyword = normalizedKeyword,
            status = normalizedStatus,
            categoryCode = normalizedCategoryCode,
            fromDateTime = fromDateTime,
            toDateTimeExclusive = toDateTimeExclusive,
        )
        return items to totalCount
    }

    fun getInquiryById(id: Long): AdminInquiry? = adminInquiryMapper.findById(id)

    fun getInquiryFiles(inquiryId: Long): List<AdminInquiryFile> = adminInquiryMapper.findFilesByInquiryId(inquiryId)

    fun getRecentUnhandled(limit: Int): List<AdminInquiry> {
        val sanitizedLimit = limit.coerceIn(1, 20)
        return adminInquiryMapper.findRecentUnhandled(sanitizedLimit)
    }

    fun getDashboardSummary(): Triple<Long, Long, Int> {
        val today = LocalDate.now()
        val todayStart = today.atStartOfDay()
        val tomorrowStart = today.plusDays(1).atStartOfDay()
        val monthStart = today.withDayOfMonth(1).atStartOfDay()
        val nextMonthStart = today.withDayOfMonth(1).plusMonths(1).atStartOfDay()

        val todayReceived = adminInquiryMapper.countTodayReceived(todayStart, tomorrowStart)
        val unhandled = adminInquiryMapper.countUnhandled()
        val responseTimes = adminInquiryMapper.findAnsweredResponseTimePairsInRange(
            fromDateTime = monthStart,
            toDateTimeExclusive = nextMonthStart,
        )
        val avgMinutes = if (responseTimes.isEmpty()) {
            0
        } else {
            kotlin.math.round(
                responseTimes
                    .map { calculateBusinessMinutes(it.createdAt, it.answeredAt).toDouble() }
                    .average(),
            ).toInt()
        }

        return Triple(todayReceived, unhandled, avgMinutes)
    }

    @Transactional
    fun updateInquiryStatus(id: Long, status: String): AdminInquiry {
        val normalizedStatus = normalizeStatus(status)
            ?: throw IllegalArgumentException("지원하지 않는 상태값입니다: $status")

        val updatedCount = adminInquiryMapper.updateStatus(
            id = id,
            status = normalizedStatus,
            updatedAt = LocalDateTime.now(),
        )
        if (updatedCount == 0) {
            throw IllegalArgumentException("문의를 찾을 수 없습니다: $id")
        }

        return adminInquiryMapper.findById(id)
            ?: throw IllegalArgumentException("문의를 찾을 수 없습니다: $id")
    }

    @Transactional
    fun updateInquiryAnswer(id: Long, answerContentText: String, status: String): AdminInquiry {
        val normalizedStatus = normalizeStatus(status)
            ?: throw IllegalArgumentException("지원하지 않는 상태값입니다: $status")

        val updatedCount = adminInquiryMapper.updateAnswerAndStatus(
            id = id,
            answerContentText = answerContentText,
            status = normalizedStatus,
            updatedAt = LocalDateTime.now(),
        )
        if (updatedCount == 0) {
            throw IllegalArgumentException("문의를 찾을 수 없습니다: $id")
        }

        return adminInquiryMapper.findById(id)
            ?: throw IllegalArgumentException("문의를 찾을 수 없습니다: $id")
    }

    private fun normalizeStatus(status: String?): String? {
        val normalized = status?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: return null
        return when (normalized) {
            "RECEIVED", "IN_PROGRESS", "ANSWERED" -> normalized
            else -> null
        }
    }

    private fun normalizeCategoryCode(categoryCode: String?): String? {
        val normalized = categoryCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: return null
        return when (normalized) {
            "PAYMENT_ERROR", "API_INTEGRATION", "ACCOUNT_PERMISSION", "ETC" -> normalized
            else -> null
        }
    }

    private fun resolveSortDirection(status: String?): String {
        return when (status) {
            "RECEIVED", "IN_PROGRESS" -> "ASC"
            else -> "DESC"
        }
    }

    private fun normalizeDateRange(fromDate: String?, toDate: String?): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now()
        val parsedFrom = fromDate?.trim()?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) }
        val parsedTo = toDate?.trim()?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) }

        val effectiveTo = parsedTo ?: today
        val effectiveFrom = parsedFrom ?: effectiveTo.minusDays(6)
        val normalizedFrom = if (effectiveFrom.isAfter(effectiveTo)) effectiveTo else effectiveFrom
        val days = ChronoUnit.DAYS.between(normalizedFrom, effectiveTo) + 1
        if (days > 31) {
            throw IllegalArgumentException("조회 기간은 최대 31일까지 가능합니다.")
        }

        return normalizedFrom.atStartOfDay() to effectiveTo.plusDays(1).atStartOfDay()
    }

    private fun calculateBusinessMinutes(start: LocalDateTime, end: LocalDateTime): Long {
        if (!end.isAfter(start)) return 0L

        var totalMinutes = 0L
        var cursorDate = start.toLocalDate()
        val lastDate = end.toLocalDate()

        while (!cursorDate.isAfter(lastDate)) {
            if (isBusinessDay(cursorDate.dayOfWeek)) {
                val dayStart = cursorDate.atTime(businessStartTime)
                val dayEnd = cursorDate.atTime(businessEndTime)
                val effectiveStart = maxOf(start, dayStart)
                val effectiveEnd = minOf(end, dayEnd)
                if (effectiveEnd.isAfter(effectiveStart)) {
                    totalMinutes += Duration.between(effectiveStart, effectiveEnd).toMinutes()
                }
            }
            cursorDate = cursorDate.plusDays(1)
        }

        return totalMinutes
    }

    private fun isBusinessDay(dayOfWeek: DayOfWeek): Boolean {
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
    }
}
