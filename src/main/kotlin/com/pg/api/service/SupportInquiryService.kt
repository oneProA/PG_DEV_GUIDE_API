package com.pg.api.service

import com.pg.api.domain.SupportInquiryCreateCommand
import com.pg.api.domain.SupportInquiryDetail
import com.pg.api.domain.SupportInquiryFileCreateCommand
import com.pg.api.domain.SupportInquiryFileSummary
import com.pg.api.domain.SupportInquirySummary
import com.pg.api.dto.CreateSupportInquiryResponse
import com.pg.api.repository.SupportInquiryMapper
import com.pg.api.repository.UserMapper
import org.jsoup.Jsoup
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.HtmlUtils
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class SupportInquiryService(
    private val supportInquiryMapper: SupportInquiryMapper,
    private val userMapper: UserMapper,
    private val cloudinaryUploadService: CloudinaryUploadService,
    @Value("\${support.inquiry.max-file-size-bytes:10485760}") private val maxFileSizeBytes: Long,
    @Value("\${support.inquiry.allowed-extensions:jpg,png}") allowedExtensionsRaw: String,
) {

    private val allowedExtensions = allowedExtensionsRaw
        .split(",")
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .toSet()

    fun getMyInquiries(username: String, limit: Int = 20): List<SupportInquirySummary> {
        val userId = resolveUserId(username)
        return supportInquiryMapper.findRecentByUserId(userId = userId, limit = limit.coerceIn(1, 100))
    }

    fun getInquiryDetail(username: String, inquiryId: Long): Pair<SupportInquiryDetail, List<SupportInquiryFileSummary>> {
        val userId = resolveUserId(username)
        val inquiry = supportInquiryMapper.findDetailByIdAndUserId(inquiryId = inquiryId, userId = userId)
            ?: throw IllegalArgumentException("Inquiry not found: $inquiryId")
        val files = supportInquiryMapper.findFilesByInquiryId(inquiryId)
        return inquiry to files
    }

    @Transactional
    fun createInquiry(
        username: String,
        categoryCode: String,
        title: String,
        contentText: String,
        files: List<MultipartFile>,
        fileKeys: List<String>,
    ): CreateSupportInquiryResponse {
        val userId = resolveUserId(username)
        val normalizedCategory = normalizeCategoryCode(categoryCode)
        val normalizedTitle = title.trim().takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Please enter a title.")
        val normalizedRawContent = contentText.trim().takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Please enter inquiry content.")
        val validFiles = files.filter { !it.isEmpty }
        val normalizedFileKeys = normalizeFileKeys(fileKeys, validFiles.size)

        validFiles.forEach { validateFile(it) }

        val now = LocalDateTime.now()
        val inquiryNo = generateInquiryNo()

        val uploadedFiles = validFiles.mapIndexed { index, file ->
            val uploaded = cloudinaryUploadService.uploadImage(file)
            UploadedSupportFile(
                key = normalizedFileKeys.getOrNull(index),
                originalFileName = uploaded.originalFileName,
                storedFileName = uploaded.storedFileName,
                url = uploaded.url,
                mimeType = uploaded.mimeType,
                sizeBytes = uploaded.sizeBytes,
            )
        }

        val normalizedContent = sanitizeAndResolveContent(
            rawContent = normalizedRawContent,
            uploadedFiles = uploadedFiles,
        )

        val inlineKeySet = extractInlineKeys(normalizedRawContent)

        val inquiryCommand = SupportInquiryCreateCommand(
            inquiryNo = inquiryNo,
            userId = userId,
            categoryCode = normalizedCategory,
            title = normalizedTitle,
            contentText = normalizedContent,
            status = "RECEIVED",
            priority = "NORMAL",
            hasAttachments = if (validFiles.isEmpty()) "N" else "Y",
            viewCount = 0,
            createdAt = now,
            updatedAt = now,
        )
        supportInquiryMapper.insertInquiry(inquiryCommand)
        val inquiryId = inquiryCommand.inquiryId
            ?: throw IllegalStateException("Failed to resolve inquiry id after insert.")

        uploadedFiles.forEachIndexed { index, uploaded ->
            val fileKey = normalizedFileKeys.getOrNull(index)
            supportInquiryMapper.insertInquiryFile(
                SupportInquiryFileCreateCommand(
                    inquiryId = inquiryId,
                    ownerType = "INQUIRY",
                    fileRole = if (fileKey != null && inlineKeySet.contains(fileKey)) "INLINE_IMAGE" else "ATTACHMENT",
                    originalFileName = uploaded.originalFileName,
                    storedFileName = uploaded.storedFileName,
                    fileUrl = uploaded.url,
                    mimeType = uploaded.mimeType,
                    fileSizeBytes = uploaded.sizeBytes,
                    inlineKey = fileKey,
                    sortOrder = index + 1,
                    uploadedByUserId = userId,
                    createdAt = now,
                ),
            )
        }

        return CreateSupportInquiryResponse(
            inquiryId = inquiryId.toString(),
            inquiryNo = inquiryNo,
            uploadedFileCount = validFiles.size,
        )
    }

    private fun resolveUserId(username: String): Long {
        val normalizedUsername = username.trim().takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("username is required.")
        return userMapper.findUserIdByUsername(normalizedUsername)
            ?: throw IllegalArgumentException("User not found: $normalizedUsername")
    }

    private fun normalizeCategoryCode(categoryCode: String): String {
        val normalized = categoryCode.trim().uppercase()
        return when (normalized) {
            "PAYMENT_ERROR", "API_INTEGRATION", "ACCOUNT_PERMISSION", "ETC" -> normalized
            else -> throw IllegalArgumentException("Invalid category code: $categoryCode")
        }
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

    private fun generateInquiryNo(): String {
        val millis = System.currentTimeMillis().toString().takeLast(10)
        val random = Random.nextInt(1000, 10000)
        return "INQ-U-$millis$random"
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

    private fun sanitizeAndResolveContent(
        rawContent: String,
        uploadedFiles: List<UploadedSupportFile>,
    ): String {
        val contentHtml = if (containsHtmlTag(rawContent)) {
            rawContent
        } else {
            rawContent
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
            ?: throw IllegalArgumentException("Please enter inquiry content.")
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

    private data class UploadedSupportFile(
        val key: String?,
        val originalFileName: String,
        val storedFileName: String,
        val url: String,
        val mimeType: String,
        val sizeBytes: Long,
    )
}
