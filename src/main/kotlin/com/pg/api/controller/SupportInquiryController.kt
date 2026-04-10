package com.pg.api.controller

import com.pg.api.dto.ApiResponse
import com.pg.api.dto.CreateSupportInquiryResponse
import com.pg.api.dto.SupportInquiryDetailResponse
import com.pg.api.dto.SupportInquiryFileResponse
import com.pg.api.dto.SupportInquirySummaryResponse
import com.pg.api.service.SupportInquiryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/support/inquiries")
class SupportInquiryController(
    private val supportInquiryService: SupportInquiryService,
) {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @GetMapping
    fun getMyInquiries(
        @RequestParam username: String,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<ApiResponse<List<SupportInquirySummaryResponse>>> {
        return try {
            val items = supportInquiryService.getMyInquiries(username, size).map {
                SupportInquirySummaryResponse(
                    inquiryId = it.inquiryId.toString(),
                    inquiryNo = it.inquiryNo,
                    categoryCode = it.categoryCode,
                    title = it.title,
                    status = it.status,
                    createdAt = it.createdAt.format(dateTimeFormatter),
                )
            }
            ResponseEntity.ok(ApiResponse("SUCCESS", items))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(ApiResponse("ERROR", null, e.message))
        }
    }

    @GetMapping("/{id:\\d+}")
    fun getInquiryDetail(
        @PathVariable id: Long,
        @RequestParam username: String,
    ): ResponseEntity<ApiResponse<SupportInquiryDetailResponse?>> {
        return try {
            val (inquiry, files) = supportInquiryService.getInquiryDetail(username = username, inquiryId = id)
            val payload = SupportInquiryDetailResponse(
                inquiryId = inquiry.inquiryId.toString(),
                inquiryNo = inquiry.inquiryNo,
                categoryCode = inquiry.categoryCode,
                title = inquiry.title,
                contentText = inquiry.contentText,
                answerContentText = inquiry.answerContentText,
                status = inquiry.status,
                createdAt = inquiry.createdAt.format(dateTimeFormatter),
                updatedAt = inquiry.updatedAt.format(dateTimeFormatter),
                answeredAt = inquiry.answeredAt?.format(dateTimeFormatter),
                files = files.map {
                    SupportInquiryFileResponse(
                        fileId = it.fileId.toString(),
                        inquiryId = it.inquiryId.toString(),
                        ownerType = it.ownerType,
                        fileRole = it.fileRole,
                        originalFileName = it.originalFileName,
                        fileUrl = it.fileUrl,
                        mimeType = it.mimeType,
                        fileSizeBytes = it.fileSizeBytes,
                        createdAt = it.createdAt.format(dateTimeFormatter),
                    )
                },
            )
            ResponseEntity.ok(ApiResponse("SUCCESS", payload))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(ApiResponse("ERROR", null, e.message))
        }
    }

    @PostMapping(consumes = ["multipart/form-data"])
    fun createInquiry(
        @RequestParam username: String,
        @RequestParam categoryCode: String,
        @RequestParam title: String,
        @RequestParam contentText: String,
        @RequestParam(required = false) files: List<MultipartFile>?,
        @RequestParam(required = false) fileKeys: List<String>?,
    ): ResponseEntity<ApiResponse<CreateSupportInquiryResponse>> {
        return try {
            val created = supportInquiryService.createInquiry(
                username = username,
                categoryCode = categoryCode,
                title = title,
                contentText = contentText,
                files = files ?: emptyList(),
                fileKeys = fileKeys ?: emptyList(),
            )
            ResponseEntity.ok(ApiResponse("SUCCESS", created))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(ApiResponse("ERROR", null, e.message))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(500).body(ApiResponse("ERROR", null, e.message))
        }
    }
}
