package com.pg.api.repository

import com.pg.api.domain.AdminUser
import com.pg.api.domain.UserActivityLog
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface AdminUserMapper {
    fun findPage(
        @Param("keyword") keyword: String?,
        @Param("status") status: String?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): List<AdminUser>

    fun countAll(
        @Param("keyword") keyword: String?,
        @Param("status") status: String?,
    ): Long

    fun findById(@Param("id") id: Long): AdminUser?

    fun findActivityLogsByActorUsername(
        @Param("actorUsername") actorUsername: String,
        @Param("limit") limit: Int,
    ): List<UserActivityLog>
}
