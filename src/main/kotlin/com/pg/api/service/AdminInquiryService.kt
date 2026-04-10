package com.pg.api.service

import com.pg.api.domain.AdminInquiry
import com.pg.api.domain.AdminInquiryFile
import com.pg.api.domain.SupportInquiryFileCreateCommand
import com.pg.api.repository.AdminInquiryMapper
import org.jsoup.Jsoup
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.HtmlUtils
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.round

@Service
class AdminInquiryService(
    private val adminInquiryMapper: AdminInquiryMapper,
    private val cloudinaryUploadService: CloudinaryUploadService,
    @Value("\${support.inquiry.max-file-size-bytes:10485760}") private val maxFileSizeBytes: Long,
    @Value("\${support.inquiry.allowed-extensions:jpg,png}") allowedExtensionsRaw: String,
) {
    private val businessStartTime: LocalTime = LocalTime.of(9, 0)
    private val businessEndTime: LocalTime = LocalTime.of(18, 0)
    private val allowedExtensions = allowedExtensionsRaw
        .split(",")
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .toSet()

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
            round(
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
            ?: throw IllegalArgumentException("Unsupported status: $status")

        val updatedCount = adminInquiryMapper.updateStatus(
            id = id,
            status = normalizedStatus,
            updatedAt = LocalDateTime.now(),
        )
        if (updatedCount == 0) {
            throw IllegalArgumentException("Inquiry not found: $id")
        }

        return adminInquiryMapper.findById(id)
            ?: throw IllegalArgumentException("Inquiry not found: $id")
    }

    @Transactional
    fun updateInquiryAnswer(
        id: Long,
        answerContentText: String,
        status: String,
        files: List<MultipartFile> = emptyList(),
        fileKeys: List<String> = emptyList(),
    ): AdminInquiry {
        val normalizedStatus = normalizeStatus(status)
            ?: throw IllegalArgumentException("Unsupported status: $status")

        val validFiles = files.filter { !it.isEmpty }
        val normalizedFileKeys = normalizeFileKeys(fileKeys, validFiles.size)
        validFiles.forEach { validateFile(it) }

        val uploadedFiles = validFiles.mapIndexed { index, file ->
            val uploaded = cloudinaryUploadService.uploadImage(file)
            UploadedAnswerFile(
                key = normalizedFileKeys.getOrNull(index),
                originalFileName = uploaded.originalFileName,
                storedFileName = uploaded.storedFileName,
                url = uploaded.url,
                mimeType = uploaded.mimeType,
                sizeBytes = uploaded.sizeBytes,
            )
        }

        val normalizedAnswer = sanitizeAndResolveAnswerContent(
            rawContent = answerContentText,
            uploadedFiles = uploadedFiles,
        )
        val inlineKeySet = extractInlineKeys(answerContentText)

        val updatedAt = LocalDateTime.now()
        val updatedCount = adminInquiryMapper.updateAnswerAndStatus(
            id = id,
            answerContentText = normalizedAnswer,
            status = normalizedStatus,
            updatedAt = updatedAt,
        )
        if (updatedCount == 0) {
            throw IllegalArgumentException("Inquiry not found: $id")
        }

        val inquiry = adminInquiryMapper.findById(id)
            ?: throw IllegalArgumentException("Inquiry not found: $id")

        uploadedFiles.forEachIndexed { index, uploaded ->
            val fileKey = normalizedFileKeys.getOrNull(index)
            adminInquiryMapper.insertInquiryFile(
                SupportInquiryFileCreateCommand(
                    inquiryId = id,
                    ownerType = "ANSWER",
                    fileRole = if (fileKey != null && inlineKeySet.contains(fileKey)) "INLINE_IMAGE" else "ATTACHMENT",
                    originalFileName = uploaded.originalFileName,
                    storedFileName = uploaded.storedFileName,
                    fileUrl = uploaded.url,
                    mimeType = uploaded.mimeType,
                    fileSizeBytes = uploaded.sizeBytes,
                    inlineKey = fileKey,
                    sortOrder = index + 1,
                    uploadedByUserId = inquiry.userId,
                    createdAt = updatedAt,
                ),
            )
        }

        return inquiry
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
            throw IllegalArgumentException("Query period must be 31 days or less.")
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

    private fun validateFile(file: MultipartFile) {
        val fileName = file.originalFilename ?: throw IllegalArgumentException("File name is empty.")
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension !in allowedExtensions) {
            throw IllegalArgumentException("Unsupported file extension: $fileName")
        }
        if (file.size <= 0) {
            throw IllegalArgumentException("Empty file cannot be uploaded: $fileName")
        }
        if (file.size > maxFileSizeBytes) {
            throw IllegalArgumentException("File is larger than ${maxFileSizeBytes / (1024 * 1024)}MB: $fileName")
        }
    }

    private fun normalizeFileKeys(rawFileKeys: List<String>, fileCount: Int): List<String> {
        if (rawFileKeys.isEmpty()) {
            return emptyList()
        }
        if (rawFileKeys.size != fileCount) {
            throw IllegalArgumentException("files and fileKeys size must match.")
        }
        return rawFileKeys.mapIndexed { index, key ->
            val normalized = key.trim()
            if (normalized.isEmpty()) {
                throw IllegalArgumentException("fileKeys[$index] is empty.")
            }
            normalized
        }
    }

    private fun sanitizeAndResolveAnswerContent(rawContent: String, uploadedFiles: List<UploadedAnswerFile>): String {
        val normalizedRaw = rawContent.trim().takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Please enter answer content.")

        val contentHtml = if (containsHtmlTag(normalizedRaw)) {
            normalizedRaw
        } else {
            normalizedRaw
                .split("\n")
                .joinToString("<br/>") { HtmlUtils.htmlEscape(it) }
        }

        val safelist = Safelist.none()
            .addTags("div", "p", "br", "strong", "em", "u", "s", "ul", "ol", "li", "blockquote", "code", "pre", "img")
            .addAttributes("img", "data-inline-key", "alt")
        val sanitizedDoc = Cleaner(safelist).clean(Jsoup.parseBodyFragment(contentHtml))

        val uploadByKey = uploadedFiles.mapNotNull { uploaded ->
            val key = uploaded.key?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            key to uploaded
        }.toMap()

        sanitizedDoc.select("img").forEach { image ->
            val key = image.attr("data-inline-key").trim()
            val uploaded = uploadByKey[key]
            if (key.isEmpty() || uploaded == null) {
                image.remove()
            } else {
                image.attr("src", uploaded.url)
                image.attr("alt", uploaded.originalFileName)
                image.attr("loading", "lazy")
                image.removeAttr("data-inline-key")
            }
        }

        return sanitizedDoc.body().html().trim()
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Please enter answer content.")
    }

    private fun extractInlineKeys(contentHtml: String): Set<String> {
        return Jsoup.parseBodyFragment(contentHtml)
            .select("img")
            .mapNotNull { image -> image.attr("data-inline-key").takeIf { it.isNotBlank() } }
            .toSet()
    }

    private fun containsHtmlTag(text: String): Boolean {
        return Regex("<\\s*[a-zA-Z][^>]*>").containsMatchIn(text)
    }

    private data class UploadedAnswerFile(
        val key: String?,
        val originalFileName: String,
        val storedFileName: String,
        val url: String,
        val mimeType: String,
        val sizeBytes: Long,
    )
}
