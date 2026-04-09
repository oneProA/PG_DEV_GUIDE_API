package com.pg.api.controller

import com.pg.api.domain.AdminInquiry
import com.pg.api.domain.AdminInquiryFile
import com.pg.api.dto.AdminInquiryDetailResponse
import com.pg.api.dto.AdminInquiryEntryResponse
import com.pg.api.dto.AdminInquiryFileResponse
import com.pg.api.dto.AdminInquiryListResponse
import com.pg.api.dto.AdminInquiryDashboardSummaryResponse
import com.pg.api.dto.ApiResponse
import com.pg.api.dto.UpdateInquiryAnswerRequest
import com.pg.api.dto.UpdateInquiryStatusRequest
import com.pg.api.service.AdminInquiryService
import org.jsoup.Jsoup
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

@RestController
@RequestMapping("/admin/inquiries")
class AdminInquiryController(private val adminInquiryService: AdminInquiryService) {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @GetMapping("/dashboard-summary")
    fun getDashboardSummary(): ResponseEntity<ApiResponse<AdminInquiryDashboardSummaryResponse>> {
        val (todayReceived, unhandled, avgResponseMinutes) = adminInquiryService.getDashboardSummary()
        val payload = AdminInquiryDashboardSummaryResponse(
            todayReceivedCount = todayReceived,
            unhandledCount = unhandled,
            avgResponseMinutes = avgResponseMinutes,
        )
        return ResponseEntity.ok(ApiResponse("SUCCESS", payload))
    }

    @GetMapping("/dashboard-unhandled")
    fun getDashboardUnhandled(
        @RequestParam(defaultValue = "5") limit: Int,
    ): ResponseEntity<ApiResponse<List<AdminInquiryEntryResponse>>> {
        val items = adminInquiryService.getRecentUnhandled(limit).map { it.toEntryResponse() }
        return ResponseEntity.ok(ApiResponse("SUCCESS", items))
    }

    @GetMapping
    fun getInquiries(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) categoryCode: String?,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?,
    ): ResponseEntity<ApiResponse<AdminInquiryListResponse>> {
        return try {
            val sanitizedPage = page.coerceAtLeast(1)
            val sanitizedSize = size.coerceIn(1, 100)
            val (items, totalCount) = adminInquiryService.getInquiries(
                page = sanitizedPage,
                size = sanitizedSize,
                keyword = keyword,
                status = status,
                categoryCode = categoryCode,
                fromDate = fromDate,
                toDate = toDate,
            )
            val totalPages = if (totalCount == 0L) 1 else ceil(totalCount.toDouble() / sanitizedSize.toDouble()).toInt()
            val payload = AdminInquiryListResponse(
                items = items.map { it.toEntryResponse() },
                page = sanitizedPage,
                size = sanitizedSize,
                totalCount = totalCount,
                totalPages = totalPages,
            )
            ResponseEntity.ok(ApiResponse("SUCCESS", payload))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(ApiResponse("ERROR", null, e.message))
        }
    }

    @GetMapping("/{id:\\d+}")
    fun getInquiryDetail(@PathVariable id: Long): ResponseEntity<ApiResponse<AdminInquiryDetailResponse?>> {
        val inquiry = adminInquiryService.getInquiryById(id)
            ?: return ResponseEntity.status(404).body(ApiResponse("ERROR", null, "문의를 찾을 수 없습니다: $id"))

        val files = adminInquiryService.getInquiryFiles(id)
        return ResponseEntity.ok(ApiResponse("SUCCESS", inquiry.toDetailResponse(files)))
    }

    @PatchMapping("/{id}/status")
    fun updateInquiryStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateInquiryStatusRequest,
    ): ResponseEntity<ApiResponse<AdminInquiryEntryResponse?>> {
        return try {
            val updated = adminInquiryService.updateInquiryStatus(id, request.status)
            ResponseEntity.ok(ApiResponse("SUCCESS", updated.toEntryResponse()))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(ApiResponse("ERROR", null, e.message))
        }
    }

    @PatchMapping("/{id}/answer")
    fun updateInquiryAnswer(
        @PathVariable id: Long,
        @RequestBody request: UpdateInquiryAnswerRequest,
    ): ResponseEntity<ApiResponse<AdminInquiryEntryResponse?>> {
        return try {
            val updated = adminInquiryService.updateInquiryAnswer(
                id = id,
                answerContentText = request.answerContentText,
                status = request.status,
            )
            ResponseEntity.ok(ApiResponse("SUCCESS", updated.toEntryResponse()))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(ApiResponse("ERROR", null, e.message))
        }
    }

    private fun AdminInquiry.toEntryResponse() = AdminInquiryEntryResponse(
        id = inquiryId.toString(),
        inquiryNo = inquiryNo,
        userId = userId.toString(),
        authorName = authorName,
        authorUsername = authorUsername,
        categoryCode = categoryCode,
        title = title,
        preview = toPreviewText(contentText),
        status = status,
        hasAttachments = hasAttachments == "Y",
        createdAt = createdAt.format(dateTimeFormatter),
        updatedAt = updatedAt.format(dateTimeFormatter),
        answeredAt = answeredAt?.format(dateTimeFormatter),
    )

    private fun AdminInquiry.toDetailResponse(files: List<AdminInquiryFile>) = AdminInquiryDetailResponse(
        id = inquiryId.toString(),
        inquiryNo = inquiryNo,
        userId = userId.toString(),
        authorName = authorName,
        authorUsername = authorUsername,
        categoryCode = categoryCode,
        title = title,
        contentText = contentText,
        answerContentText = answerContentText,
        status = status,
        hasAttachments = hasAttachments == "Y",
        createdAt = createdAt.format(dateTimeFormatter),
        updatedAt = updatedAt.format(dateTimeFormatter),
        answeredAt = answeredAt?.format(dateTimeFormatter),
        files = files.map { it.toResponse() },
    )

    private fun AdminInquiryFile.toResponse() = AdminInquiryFileResponse(
        id = fileId.toString(),
        inquiryId = inquiryId.toString(),
        ownerType = ownerType,
        fileRole = fileRole,
        originalFileName = originalFileName,
        fileUrl = fileUrl,
        mimeType = mimeType,
        fileSizeBytes = fileSizeBytes,
        createdAt = createdAt.format(dateTimeFormatter),
    )

    private fun toPreviewText(content: String): String {
        return Jsoup.parse(content).text().replace('\n', ' ').trim().take(120)
    }
}
