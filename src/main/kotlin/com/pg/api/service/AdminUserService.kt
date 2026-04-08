package com.pg.api.service

import com.pg.api.domain.AdminUser
import com.pg.api.domain.UserActivityLog
import com.pg.api.repository.AdminUserMapper
import org.springframework.stereotype.Service

@Service
class AdminUserService(private val adminUserMapper: AdminUserMapper) {

    fun getUsers(
        page: Int,
        size: Int,
        keyword: String?,
        status: String?,
    ): Pair<List<AdminUser>, Long> {
        val sanitizedPage = page.coerceAtLeast(1)
        val sanitizedSize = size.coerceIn(1, 100)
        val normalizedKeyword = keyword?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedStatus = status?.trim()?.uppercase()?.takeIf { it == "ACTIVE" || it == "INACTIVE" }
        val offset = (sanitizedPage - 1) * sanitizedSize

        val users = adminUserMapper.findPage(normalizedKeyword, normalizedStatus, sanitizedSize, offset)
        val totalCount = adminUserMapper.countAll(normalizedKeyword, normalizedStatus)

        return users to totalCount
    }

    fun getUserById(id: Long): AdminUser? = adminUserMapper.findById(id)

    fun getUserActivityLogsByActor(username: String, limit: Int = 4): List<UserActivityLog> =
        adminUserMapper.findActivityLogsByActorUsername(username, limit)
}
