package com.pg.api.controller

import com.pg.api.domain.ApiEndpoint
import com.pg.api.dto.AdminApiDetailResponse
import com.pg.api.dto.AdminApiEntryResponse
import com.pg.api.dto.AdminApiFieldResponse
import com.pg.api.dto.ApiResponse
import com.pg.api.service.AdminApiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/docs/apis")
class ApiDocsController(private val adminApiService: AdminApiService) {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @GetMapping
    fun getAllApiDocs(): ResponseEntity<ApiResponse<List<AdminApiEntryResponse>>> {
        val apis = adminApiService.getAllApiEndpoints()
        return ResponseEntity.ok(ApiResponse("SUCCESS", apis.map { convertToResponse(it) }))
    }

    @GetMapping("/{id}")
    fun getApiDoc(@PathVariable id: Long): ResponseEntity<ApiResponse<AdminApiDetailResponse?>> {
        val api = adminApiService.getApiEndpointById(id)
            ?: return ResponseEntity.status(404).body(ApiResponse("ERROR", null, "API를 찾을 수 없습니다: $id"))

        val fields = adminApiService.getApiFields(api.name, api.endpoint, api.version)
        return ResponseEntity.ok(
            ApiResponse(
                "SUCCESS",
                convertToDetailResponse(api, fields.map { convertFieldToResponse(it) })
            )
        )
    }

    private fun convertToResponse(api: ApiEndpoint) = AdminApiEntryResponse(
        id = api.id.toString(),
        name = api.name,
        endpoint = api.endpoint,
        method = api.httpMethod,
        version = api.version,
        displayOrder = api.displayOrder,
        status = api.status,
        lastModified = api.updatedAt.format(dateTimeFormatter),
        description = api.description
    )

    private fun convertToDetailResponse(api: ApiEndpoint, fields: List<AdminApiFieldResponse>) =
        AdminApiDetailResponse(
            id = api.id.toString(),
            name = api.name,
            endpoint = api.endpoint,
            method = api.httpMethod,
            version = api.version,
            displayOrder = api.displayOrder,
            status = api.status,
            lastModified = api.updatedAt.format(dateTimeFormatter),
            description = api.description,
            fields = fields
        )

    private fun convertFieldToResponse(field: com.pg.api.domain.ApiEndpointField) = AdminApiFieldResponse(
        id = field.id.toString(),
        fieldScope = field.fieldScope,
        fieldLocation = field.fieldLocation,
        fieldName = field.fieldName,
        fieldType = field.fieldType,
        requiredYn = field.requiredYn,
        fieldOrder = field.fieldOrder,
        description = field.description,
        sampleValue = field.sampleValue,
        defaultValue = field.defaultValue
    )
}
