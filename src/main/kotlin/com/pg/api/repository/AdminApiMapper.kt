package com.pg.api.repository

import com.pg.api.domain.ApiDefinition
import com.pg.api.domain.ApiEndpoint
import com.pg.api.domain.ApiEndpointField
import com.pg.api.domain.ApiEndpointVersion
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.LocalDateTime

@Mapper
interface AdminApiMapper {
    fun findAll(): List<ApiEndpoint>

    fun findById(id: Long): ApiEndpoint?

    fun findByEndpoint(endpoint: String): ApiEndpoint?

    fun findByNameAndVersion(
        @Param("name") name: String,
        @Param("version") version: String
    ): ApiEndpoint?

    fun findFieldsByEndpointAndVersion(
        @Param("endpoint") endpoint: String,
        @Param("version") version: String
    ): List<ApiEndpointField>

    fun findFieldsByApiNameAndVersion(
        @Param("apiName") apiName: String,
        @Param("version") version: String
    ): List<ApiEndpointField>

    fun insert(apiEndpoint: ApiEndpoint): Int

    fun update(apiEndpoint: ApiEndpoint): Int

    fun deleteById(id: Long): Int

    fun findApiVersionByEndpointAndVersion(
        @Param("endpoint") endpoint: String,
        @Param("version") version: String
    ): ApiEndpointVersion?

    fun insertApiDefinition(apiDefinition: ApiDefinition): Int

    fun updateApiDefinitionName(
        @Param("id") id: Long,
        @Param("apiName") apiName: String,
        @Param("updatedAt") updatedAt: LocalDateTime
    ): Int

    fun insertApiVersion(apiVersion: ApiEndpointVersion): Int

    fun updateApiVersion(apiVersion: ApiEndpointVersion): Int

    fun deleteFieldsByApiVersionId(@Param("apiVersionId") apiVersionId: Long): Int

    fun insertApiField(
        @Param("apiVersionId") apiVersionId: Long,
        @Param("field") field: ApiEndpointField
    ): Int
}
