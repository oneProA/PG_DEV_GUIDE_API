package com.pg.api.repository

import com.pg.api.domain.User
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface UserMapper {
    fun findByUsername(username: String): User?

    fun findUserIdByUsername(@Param("username") username: String): Long?

    fun insertUserActivityLog(
        @Param("userId") userId: Long,
        @Param("activityType") activityType: String,
        @Param("activityTitle") activityTitle: String,
        @Param("activityDetail") activityDetail: String?,
        @Param("actorUsername") actorUsername: String?,
        @Param("ipAddress") ipAddress: String?,
        @Param("userAgent") userAgent: String?,
    ): Int
}
