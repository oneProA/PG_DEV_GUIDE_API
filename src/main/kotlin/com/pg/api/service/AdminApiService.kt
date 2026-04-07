package com.pg.api.service

import com.pg.api.domain.ApiEndpoint
import com.pg.api.domain.ApiEndpointField
import com.pg.api.repository.AdminApiMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminApiService(private val adminApiMapper: AdminApiMapper) {

    /**
     * 전체 API 엔드포인트 목록 조회
     */
    fun getAllApiEndpoints(): List<ApiEndpoint> {
        return adminApiMapper.findAll()
    }

    /**
     * 단일 API 엔드포인트 조회
     */
    fun getApiEndpointById(id: Long): ApiEndpoint? {
        return adminApiMapper.findById(id)
    }

    /**
     * API 상세 정보와 필드 메타데이터 조회
     */
    fun getApiFields(apiName: String, version: String): List<ApiEndpointField> {
        return adminApiMapper.findFieldsByApiNameAndVersion(apiName, version)
    }

    /**
     * API 엔드포인트 등록
     */
    @Transactional
    fun createApiEndpoint(apiEndpoint: ApiEndpoint): ApiEndpoint {
        // 중복 엔드포인트 확인
        val existing = adminApiMapper.findByEndpoint(apiEndpoint.endpoint)
        if (existing != null) {
            throw IllegalArgumentException("이미 등록된 엔드포인트입니다: ${apiEndpoint.endpoint}")
        }

        val newEndpoint = apiEndpoint.copy(
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        adminApiMapper.insert(newEndpoint)
        return newEndpoint
    }

    /**
     * API 엔드포인트 수정
     */
    @Transactional
    fun updateApiEndpoint(id: Long, apiEndpoint: ApiEndpoint): ApiEndpoint {
        val existing = adminApiMapper.findById(id)
            ?: throw IllegalArgumentException("존재하지 않는 API입니다: $id")

        val updated = apiEndpoint.copy(
            id = id,
            createdAt = existing.createdAt,
            updatedAt = LocalDateTime.now()
        )
        adminApiMapper.update(updated)
        return updated
    }

    /**
     * API 엔드포인트 삭제
     */
    @Transactional
    fun deleteApiEndpoint(id: Long) {
        val existing = adminApiMapper.findById(id)
            ?: throw IllegalArgumentException("존재하지 않는 API입니다: $id")

        adminApiMapper.deleteById(id)
    }

    /**
     * API 상태 업데이트
     */
    @Transactional
    fun updateApiStatus(id: Long, status: String): ApiEndpoint {
        val existing = adminApiMapper.findById(id)
            ?: throw IllegalArgumentException("존재하지 않는 API입니다: $id")

        val updated = existing.copy(
            status = status,
            updatedAt = LocalDateTime.now()
        )
        adminApiMapper.update(updated)
        return updated
    }
}
