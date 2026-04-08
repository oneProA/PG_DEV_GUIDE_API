package com.pg.api.service

import com.pg.api.dto.GuideCancelRequest
import com.pg.api.dto.GuideCancelResponse
import com.pg.api.dto.GuidePaymentRequest
import com.pg.api.dto.GuidePaymentRequestResponse
import com.pg.api.dto.GuidePaymentStatusResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.util.UriComponentsBuilder

@Service
class PgApiBridgeService(
    @Value("\${integration.pg-api.local-base-url}") private val localBaseUrl: String,
    @Value("\${integration.pg-api.cloud-base-url}") private val cloudBaseUrl: String,
    @Value("\${integration.pg-api.default-payment-method-id}") private val defaultPaymentMethodId: String,
    @Value("\${integration.pg-api.default-approval-url}") private val defaultApprovalUrl: String,
    @Value("\${integration.pg-api.default-cancel-url}") private val defaultCancelUrl: String,
    @Value("\${integration.pg-api.default-fail-url}") private val defaultFailUrl: String,
) {
    private val restClient = RestClient.create()

    fun requestPayment(request: GuidePaymentRequest): GuidePaymentRequestResponse {
        val resolvedRequest = request.copy(
            paymentMethodId = request.paymentMethodId ?: defaultPaymentMethodId,
            approvalUrl = request.approvalUrl ?: defaultApprovalUrl,
            cancelUrl = request.cancelUrl ?: defaultCancelUrl,
            failUrl = request.failUrl ?: defaultFailUrl,
        )

        val uri = UriComponentsBuilder
            .fromHttpUrl("${resolveBaseUrl()}/api/pay/ready")
            .queryParam("paymentMethodId", resolvedRequest.paymentMethodId)
            .queryParamIfPresent("orderId", java.util.Optional.ofNullable(resolvedRequest.orderId))
            .queryParam("userId", resolvedRequest.userId)
            .queryParam("itemName", resolvedRequest.itemName)
            .queryParam("amount", resolvedRequest.amount)
            .queryParam("approvalUrl", resolvedRequest.approvalUrl)
            .queryParam("cancelUrl", resolvedRequest.cancelUrl)
            .queryParam("failUrl", resolvedRequest.failUrl)
            .build()
            .encode()
            .toUri()

        val response = exchangeForMap(uri.toString())

        return GuidePaymentRequestResponse(
            orderId = response["orderId"]?.toString().orEmpty(),
            paymentMethodId = resolvedRequest.paymentMethodId.orEmpty(),
            paymentId = response["paymentId"]?.toString(),
            status = "READY",
            amount = resolvedRequest.amount,
            approvedAt = response["approvedAt"]?.toString(),
            nextRedirectPcUrl = response["next_redirect_pc_url"]?.toString(),
        )
    }

    fun getPaymentStatus(orderId: String): GuidePaymentStatusResponse {
        val response = restClient.get()
            .uri("${resolveBaseUrl()}/api/pay/status/{orderId}", orderId)
            .retrieve()
            .body(Map::class.java)
            ?.mapKeys { it.key.toString() }
            ?: emptyMap()

        return GuidePaymentStatusResponse(
            orderId = response["orderId"]?.toString().orEmpty(),
            userId = response["userId"]?.toString(),
            amount = response["amount"]?.toString()?.toLongOrNull() ?: 0L,
            status = response["status"]?.toString().orEmpty(),
            paymentMethodId = response["paymentMethodId"]?.toString(),
            paymentId = response["paymentId"]?.toString(),
            createdAt = response["createdAt"]?.toString(),
            approvalAt = response["approvalAt"]?.toString(),
        )
    }

    fun cancelPayment(request: GuideCancelRequest): GuideCancelResponse {
        val uriBuilder = UriComponentsBuilder
            .fromHttpUrl("${resolveBaseUrl()}/api/pay/cancel")
            .queryParam("orderId", request.orderId)

        request.cancelAmount?.let { uriBuilder.queryParam("cancelAmount", it) }
        uriBuilder.queryParam("cancelReason", request.cancelReason ?: "고객 변심")

        val response = exchangeForMap(uriBuilder.build().encode().toUriString())

        return GuideCancelResponse(
            orderId = response["orderId"]?.toString().orEmpty(),
            paymentMethodId = response["paymentMethodId"]?.toString(),
            status = response["status"]?.toString().orEmpty(),
            cancelAmount = response["cancelAmount"]?.toString()?.toLongOrNull(),
            remainAmount = response["remainAmount"]?.toString()?.toLongOrNull(),
            canceledAt = response["canceledAt"]?.toString(),
            paymentId = response["paymentId"]?.toString(),
        )
    }

    private fun exchangeForMap(uri: String): Map<String, Any?> {
        return try {
            restClient.post()
                .uri(uri)
                .retrieve()
                .body(Map::class.java)
                ?.mapKeys { it.key.toString() }
                ?: emptyMap()
        } catch (error: RestClientResponseException) {
            throw IllegalStateException(
                error.responseBodyAsString.ifBlank { "pg_api 호출에 실패했습니다: ${error.statusCode}" },
                error,
            )
        }
    }

    private fun resolveBaseUrl(): String {
        return if (System.getenv("K_SERVICE").isNullOrBlank()) {
            localBaseUrl
        } else {
            cloudBaseUrl
        }
    }
}
