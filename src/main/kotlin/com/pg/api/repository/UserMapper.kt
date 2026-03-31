package com.pg.api.repository

import com.pg.api.domain.User
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserMapper {
    fun findByUsername(username: String): User?
    fun insertUser(user: User): Int
}
