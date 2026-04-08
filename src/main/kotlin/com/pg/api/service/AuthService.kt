package com.pg.api.service

import com.pg.api.dto.LoginRequest
import com.pg.api.dto.LoginResponse
import com.pg.api.dto.LoginResponseData
import com.pg.api.repository.UserMapper
import org.springframework.stereotype.Service

@Service
class AuthService(private val userMapper: UserMapper) {

    fun login(request: LoginRequest, ipAddress: String?, userAgent: String?): LoginResponse {
        println("Login attempt for user: ${request.username}")

        val user = userMapper.findByUsername(request.username)
            ?: run {
                logActivityByUsername(
                    username = request.username,
                    activityType = "LOGIN_FAIL",
                    activityTitle = "로그인 실패",
                    activityDetail = "존재하지 않는 사용자로 로그인을 시도했습니다.",
                    actorUsername = request.username,
                    ipAddress = ipAddress,
                    userAgent = userAgent,
                )
                throw IllegalArgumentException("로그인 실패")
            }

        if (user.password != request.password) {
            logActivityByUsername(
                username = user.username,
                activityType = "LOGIN_FAIL",
                activityTitle = "로그인 실패",
                activityDetail = "비밀번호 불일치",
                actorUsername = user.username,
                ipAddress = ipAddress,
                userAgent = userAgent,
            )
            throw IllegalArgumentException("로그인 실패")
        }

        println("User found: ${user.username}, Role: ${user.role}")
        logActivityByUsername(
            username = user.username,
            activityType = "LOGIN_SUCCESS",
            activityTitle = "로그인 성공",
            activityDetail = "회원이 로그인했습니다.",
            actorUsername = user.username,
            ipAddress = ipAddress,
            userAgent = userAgent,
        )

        return LoginResponse(
            data = LoginResponseData(
                accessToken = "mock-jwt-token-${user.username}-${System.currentTimeMillis()}",
                username = user.username,
                email = user.email,
                role = user.role,
            ),
            message = "로그인 성공",
        )
    }

    private fun logActivityByUsername(
        username: String,
        activityType: String,
        activityTitle: String,
        activityDetail: String?,
        actorUsername: String?,
        ipAddress: String?,
        userAgent: String?,
    ) {
        val userId = userMapper.findUserIdByUsername(username) ?: return
        userMapper.insertUserActivityLog(
            userId = userId,
            activityType = activityType,
            activityTitle = activityTitle,
            activityDetail = activityDetail,
            actorUsername = actorUsername,
            ipAddress = ipAddress,
            userAgent = userAgent,
        )
    }
}
