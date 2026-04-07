package com.pg.api.repository

import com.pg.api.domain.ApiEndpoint
import com.pg.api.domain.ApiEndpointField
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface AdminApiMapper {
    /**
     * 전체 API 엔드포인트 조회
     */
    fun findAll(): List<ApiEndpoint>

    /**
     * 단일 API 엔드포인트 조회
     */
    fun findById(id: Long): ApiEndpoint?

    /**
     * 엔드포인트 경로로 조회
     */
    fun findByEndpoint(endpoint: String): ApiEndpoint?

    /**
     * API 이름/버전 기준 필드 메타데이터 조회
     */
    fun findFieldsByApiNameAndVersion(
        @Param("apiName") apiName: String,
        @Param("version") version: String
    ): List<ApiEndpointField>

    /**
     * API 엔드포인트 등록
     */
    fun insert(apiEndpoint: ApiEndpoint): Int

    /**
     * API 엔드포인트 수정
     */
    fun update(apiEndpoint: ApiEndpoint): Int

    /**
     * API 엔드포인트 삭제
     */
    fun deleteById(id: Long): Int
}
