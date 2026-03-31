package com.pg.api.config

import com.pg.api.service.AuthService
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class DataInitializer(private val authService: AuthService) {

    @PostConstruct
    fun seed() {
        authService.seedDefaultUser(
            username = "demo.user",
            password = "password",
            email = "demo.user@cjone.com",
            role = "ADMIN"
        )
    }
}
