package com.pg.api.repository

import com.pg.api.domain.SupportInquiryCreateCommand
import com.pg.api.domain.SupportInquiryDetail
import com.pg.api.domain.SupportInquiryFileCreateCommand
import com.pg.api.domain.SupportInquiryFileSummary
import com.pg.api.domain.SupportInquirySummary
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface SupportInquiryMapper {
    fun insertInquiry(command: SupportInquiryCreateCommand): Int

    fun insertInquiryFile(command: SupportInquiryFileCreateCommand): Int

    fun findRecentByUserId(
        @Param("userId") userId: Long,
        @Param("limit") limit: Int,
    ): List<SupportInquirySummary>

    fun findDetailByIdAndUserId(
        @Param("inquiryId") inquiryId: Long,
        @Param("userId") userId: Long,
    ): SupportInquiryDetail?

    fun findFilesByInquiryId(@Param("inquiryId") inquiryId: Long): List<SupportInquiryFileSummary>
}
