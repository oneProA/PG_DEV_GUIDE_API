package com.pg.api.controller

import com.pg.api.dto.LoginRequest
import com.pg.api.dto.LoginResponse
import com.pg.api.dto.LoginResponseData
import com.pg.api.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            println("Auth Error: ${e.message}")
            ResponseEntity.status(401).body(LoginResponse(
                data = LoginResponseData(accessToken = "", username = "", role = ""),
                message = e.message
            ))
        } catch (e: Exception) {
            println("Server Error during login: ${e.message}")
            e.printStackTrace()
            ResponseEntity.status(500).body(LoginResponse(
                data = LoginResponseData(accessToken = "", username = "", role = ""),
                message = "서버 오류가 발생했습니다."
            ))
        }
    }
}
