package com.pg.api.controller

import com.pg.api.dto.LoginRequest
import com.pg.api.dto.LoginResponse
import com.pg.api.dto.LoginResponseData
import com.pg.api.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<LoginResponse> {
        return try {
            val ipAddress = extractClientIp(httpRequest)
            val userAgent = httpRequest.getHeader("User-Agent")
            val response = authService.login(request, ipAddress, userAgent)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            println("Auth Error: ${e.message}")
            ResponseEntity.status(401).body(
                LoginResponse(
                    data = LoginResponseData(accessToken = "", username = "", email = "", role = ""),
                    message = e.message,
                ),
            )
        } catch (e: Exception) {
            println("Server Error during login: ${e.message}")
            e.printStackTrace()
            ResponseEntity.status(500).body(
                LoginResponse(
                    data = LoginResponseData(accessToken = "", username = "", email = "", role = ""),
                    message = "서버 오류가 발생했습니다.",
                ),
            )
        }
    }

    private fun extractClientIp(request: HttpServletRequest): String? {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",").first().trim()
        }
        return request.remoteAddr
    }
}
