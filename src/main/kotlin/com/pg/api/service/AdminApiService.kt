package com.pg.api.service

import com.pg.api.domain.ApiDefinition
import com.pg.api.domain.ApiEndpoint
import com.pg.api.domain.ApiEndpointField
import com.pg.api.domain.ApiEndpointVersion
import com.pg.api.dto.AdminApiFieldRequest
import com.pg.api.repository.AdminApiMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminApiService(private val adminApiMapper: AdminApiMapper) {

    fun getAllApiEndpoints(): List<ApiEndpoint> = adminApiMapper.findAll()

    fun getApiEndpointById(id: Long): ApiEndpoint? = adminApiMapper.findById(id)

    fun getApiFields(apiName: String, endpoint: String, version: String): List<ApiEndpointField> {
        val fieldsByEndpoint = adminApiMapper.findFieldsByEndpointAndVersion(endpoint, version)
        if (fieldsByEndpoint.isNotEmpty()) {
            return fieldsByEndpoint
        }

        return adminApiMapper.findFieldsByApiNameAndVersion(apiName, version)
    }

    @Transactional
    fun createApiEndpoint(apiEndpoint: ApiEndpoint, fields: List<AdminApiFieldRequest>): ApiEndpoint {
        val existing = adminApiMapper.findByEndpoint(apiEndpoint.endpoint)
        if (existing != null) {
            throw IllegalArgumentException("이미 등록된 엔드포인트입니다: ${apiEndpoint.endpoint}")
        }

        val timestamp = LocalDateTime.now()
        val newEndpoint = apiEndpoint.copy(
            version = resolveUniqueVersion(apiEndpoint.name, apiEndpoint.version),
            createdAt = timestamp,
            updatedAt = timestamp
        )
        adminApiMapper.insert(newEndpoint)
        syncApiMetadata(newEndpoint, fields, null)
        return newEndpoint
    }

    @Transactional
    fun updateApiEndpoint(id: Long, apiEndpoint: ApiEndpoint, fields: List<AdminApiFieldRequest>): ApiEndpoint {
        val existing = adminApiMapper.findById(id)
            ?: throw IllegalArgumentException("존재하지 않는 API입니다: $id")

        val duplicatedEndpoint = adminApiMapper.findByEndpoint(apiEndpoint.endpoint)
        if (duplicatedEndpoint != null && duplicatedEndpoint.id != id) {
            throw IllegalArgumentException("이미 등록된 엔드포인트입니다: ${apiEndpoint.endpoint}")
        }

        val updated = apiEndpoint.copy(
            id = id,
            version = resolveUniqueVersion(apiEndpoint.name, apiEndpoint.version, id),
            createdAt = existing.createdAt,
            updatedAt = LocalDateTime.now(),
            status = apiEndpoint.status.ifBlank { existing.status }
        )

        adminApiMapper.update(updated)
        syncApiMetadata(updated, fields, existing)
        return updated
    }

    @Transactional
    fun deleteApiEndpoint(id: Long) {
        val existing = adminApiMapper.findById(id)
            ?: throw IllegalArgumentException("존재하지 않는 API입니다: $id")

        val metadataVersion = adminApiMapper.findApiVersionByEndpointAndVersion(existing.endpoint, existing.version)
        if (metadataVersion?.id != null) {
            adminApiMapper.deleteFieldsByApiVersionId(metadataVersion.id!!)
        }

        adminApiMapper.deleteById(id)
    }

    @Transactional
    fun updateApiStatus(id: Long, status: String): ApiEndpoint {
        val existing = adminApiMapper.findById(id)
            ?: throw IllegalArgumentException("존재하지 않는 API입니다: $id")

        val updated = existing.copy(status = status, updatedAt = LocalDateTime.now())
        adminApiMapper.update(updated)

        val metadataVersion = adminApiMapper.findApiVersionByEndpointAndVersion(existing.endpoint, existing.version)
        if (metadataVersion?.id != null) {
            adminApiMapper.updateApiVersion(
                metadataVersion.copy(
                    status = status,
                    updatedAt = LocalDateTime.now()
                )
            )
        }

        return updated
    }

    private fun syncApiMetadata(
        apiEndpoint: ApiEndpoint,
        fields: List<AdminApiFieldRequest>,
        previousApiEndpoint: ApiEndpoint?
    ) {
        val now = LocalDateTime.now()
        val existingMetadataVersion = previousApiEndpoint
            ?.let { adminApiMapper.findApiVersionByEndpointAndVersion(it.endpoint, it.version) }
            ?: adminApiMapper.findApiVersionByEndpointAndVersion(apiEndpoint.endpoint, apiEndpoint.version)

        val metadataVersion = if (existingMetadataVersion != null && existingMetadataVersion.id != null) {
            adminApiMapper.updateApiDefinitionName(existingMetadataVersion.apiDefinitionId, apiEndpoint.name, now)
            val updatedVersion = existingMetadataVersion.copy(
                version = apiEndpoint.version,
                endpoint = apiEndpoint.endpoint,
                httpMethod = apiEndpoint.httpMethod,
                status = apiEndpoint.status,
                description = apiEndpoint.description,
                updatedAt = now
            )
            adminApiMapper.updateApiVersion(updatedVersion)
            updatedVersion
        } else {
            val apiDefinition = ApiDefinition(
                apiCode = generateApiCode(apiEndpoint),
                apiName = apiEndpoint.name,
                createdAt = now,
                updatedAt = now
            )
            adminApiMapper.insertApiDefinition(apiDefinition)
            val createdVersion = ApiEndpointVersion(
                apiDefinitionId = apiDefinition.id ?: throw IllegalStateException("API definition id was not generated."),
                version = apiEndpoint.version,
                endpoint = apiEndpoint.endpoint,
                httpMethod = apiEndpoint.httpMethod,
                status = apiEndpoint.status,
                description = apiEndpoint.description,
                createdAt = now,
                updatedAt = now
            )
            adminApiMapper.insertApiVersion(createdVersion)
            createdVersion
        }

        val apiVersionId = metadataVersion.id ?: throw IllegalStateException("API version id was not generated.")
        adminApiMapper.deleteFieldsByApiVersionId(apiVersionId)

        fields
            .filter { it.fieldName.isNotBlank() && it.fieldType.isNotBlank() }
            .sortedBy { it.fieldOrder }
            .forEachIndexed { index, field ->
                adminApiMapper.insertApiField(
                    apiVersionId,
                    ApiEndpointField(
                        id = 0L,
                        fieldScope = field.fieldScope,
                        fieldLocation = field.fieldLocation,
                        fieldName = field.fieldName.trim(),
                        fieldType = field.fieldType.trim(),
                        requiredYn = field.requiredYn,
                        fieldOrder = index + 1,
                        description = field.description?.trim()?.ifBlank { null },
                        sampleValue = field.sampleValue?.trim()?.ifBlank { null },
                        defaultValue = field.defaultValue?.trim()?.ifBlank { null }
                    )
                )
            }
    }

    private fun generateApiCode(apiEndpoint: ApiEndpoint): String {
        val normalized = "${apiEndpoint.httpMethod}_${apiEndpoint.endpoint}"
            .uppercase()
            .replace(Regex("[^A-Z0-9]+"), "_")
            .trim('_')

        return if (normalized.isBlank()) {
            "API_${System.currentTimeMillis()}"
        } else {
            normalized
        }
    }

    private fun resolveUniqueVersion(name: String, requestedVersion: String, currentId: Long? = null): String {
        var candidateVersion = requestedVersion.trim().ifBlank { "v1.0.0" }
        var duplicate = adminApiMapper.findByNameAndVersion(name, candidateVersion)

        while (duplicate != null && duplicate.id != currentId) {
            candidateVersion = incrementVersion(candidateVersion)
            duplicate = adminApiMapper.findByNameAndVersion(name, candidateVersion)
        }

        return candidateVersion
    }

    private fun incrementVersion(version: String): String {
        val semanticMatch = Regex("^(v?)(\\d+)\\.(\\d+)\\.(\\d+)$").matchEntire(version)
        if (semanticMatch != null) {
            val prefix = semanticMatch.groupValues[1]
            val major = semanticMatch.groupValues[2].toInt()
            val minor = semanticMatch.groupValues[3].toInt()
            val patch = semanticMatch.groupValues[4].toInt() + 1
            return "${prefix}${major}.${minor}.${patch}"
        }

        val shortSemanticMatch = Regex("^(v?)(\\d+)\\.(\\d+)$").matchEntire(version)
        if (shortSemanticMatch != null) {
            val prefix = shortSemanticMatch.groupValues[1]
            val major = shortSemanticMatch.groupValues[2].toInt()
            val minor = shortSemanticMatch.groupValues[3].toInt()
            return "${prefix}${major}.${minor}.1"
        }

        val numberedSuffixMatch = Regex("^(.*?)-(\\d+)$").matchEntire(version)
        if (numberedSuffixMatch != null) {
            val base = numberedSuffixMatch.groupValues[1]
            val number = numberedSuffixMatch.groupValues[2].toInt() + 1
            return "$base-$number"
        }

        return "$version-1"
    }
}
