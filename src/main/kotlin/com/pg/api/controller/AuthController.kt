package com.pg.api.controller

import com.pg.api.dto.ApiResponse
import com.pg.api.dto.LoginRequest
import com.pg.api.dto.LoginResponse
import com.pg.api.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(ApiResponse("SUCCESS", response))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(ApiResponse("ERROR", null, e.message))
        }
    }
}
