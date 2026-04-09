package com.pg.api.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.security.MessageDigest
import java.time.Instant

data class UploadedFileInfo(
    val url: String,
    val storedFileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val originalFileName: String,
)

@Service
class CloudinaryUploadService(
    @Value("\${integration.cloudinary.cloud-name:}") private val cloudName: String,
    @Value("\${integration.cloudinary.api-key:}") private val apiKey: String,
    @Value("\${integration.cloudinary.api-secret:}") private val apiSecret: String,
    @Value("\${integration.cloudinary.folder:support/inquiries}") private val defaultFolder: String,
) {

    private val restTemplate = RestTemplate(
        SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(10000)
            setReadTimeout(20000)
        },
    )

    fun uploadImage(file: MultipartFile): UploadedFileInfo {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw IllegalStateException("Cloudinary 환경설정이 누락되었습니다.")
        }

        val timestamp = Instant.now().epochSecond
        val paramsToSign = "folder=$defaultFolder&timestamp=$timestamp"
        val signature = sha1Hex("$paramsToSign$apiSecret")
        val endpoint = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

        val body = LinkedMultiValueMap<String, Any>()
        body.add("api_key", apiKey)
        body.add("timestamp", timestamp.toString())
        body.add("folder", defaultFolder)
        body.add("signature", signature)
        body.add("file", object : ByteArrayResource(file.bytes) {
            override fun getFilename(): String = file.originalFilename ?: "upload.jpg"
        })

        val headers = org.springframework.http.HttpHeaders().apply {
            contentType = MediaType.MULTIPART_FORM_DATA
        }

        val response = restTemplate.postForEntity(
            endpoint,
            org.springframework.http.HttpEntity(body, headers),
            MutableMap::class.java,
        )

        if (!response.statusCode.is2xxSuccessful || response.body == null) {
            throw IllegalStateException("Cloudinary 업로드에 실패했습니다.")
        }

        val secureUrl = response.body?.get("secure_url")?.toString()
        val publicId = response.body?.get("public_id")?.toString()
        if (secureUrl.isNullOrBlank() || publicId.isNullOrBlank()) {
            throw IllegalStateException("Cloudinary 응답이 올바르지 않습니다.")
        }

        return UploadedFileInfo(
            url = secureUrl,
            storedFileName = publicId,
            mimeType = file.contentType ?: "application/octet-stream",
            sizeBytes = file.size,
            originalFileName = file.originalFilename ?: "image",
        )
    }

    private fun sha1Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
