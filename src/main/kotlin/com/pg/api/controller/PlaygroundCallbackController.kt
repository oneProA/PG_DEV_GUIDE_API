package com.pg.api.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder

@Controller
class PlaygroundCallbackController(
    @Value("\${integration.guide-view.local-base-url}") private val localGuideViewBaseUrl: String,
    @Value("\${integration.guide-view.cloud-base-url}") private val cloudGuideViewBaseUrl: String,
) {

    @GetMapping("/playground/{resultType}")
    fun relayCallback(
        @PathVariable resultType: String,
        @RequestParam params: Map<String, String>,
    ): RedirectView {
        val redirectUrl = UriComponentsBuilder
            .fromHttpUrl("${resolveGuideViewBaseUrl().removeSuffix("/")}/playground/$resultType")
            .apply {
                params.forEach { (key, value) ->
                    queryParam(key, value)
                }
            }
            .build()
            .encode()
            .toUriString()

        return RedirectView(redirectUrl)
    }

    private fun resolveGuideViewBaseUrl(): String {
        return if (System.getenv("K_SERVICE").isNullOrBlank()) {
            localGuideViewBaseUrl
        } else {
            cloudGuideViewBaseUrl
        }
    }
}
