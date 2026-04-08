package com.pg.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PlaygroundCallbackController(
    private val objectMapper: ObjectMapper,
) {

    @GetMapping("/playground/{resultType}", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun relayCallback(
        @PathVariable resultType: String,
        @RequestParam params: Map<String, String>,
    ): String {
        val payloadJson = objectMapper.writeValueAsString(
            mapOf(
                "type" to "pg-playground-callback",
                "resultType" to resultType,
                "params" to params,
            ),
        )

        return """
            <!doctype html>
            <html lang="ko">
              <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>결제 결과 처리중</title>
                <style>
                  body {
                    margin: 0;
                    min-height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                    background: #f4f7fb;
                    color: #0f172a;
                  }
                  .card {
                    width: min(480px, calc(100vw - 32px));
                    padding: 32px;
                    border-radius: 24px;
                    background: #ffffff;
                    box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
                    text-align: center;
                  }
                  h1 {
                    margin: 0 0 12px;
                    font-size: 24px;
                  }
                  p {
                    margin: 0;
                    color: #64748b;
                    line-height: 1.6;
                    font-size: 14px;
                  }
                </style>
              </head>
              <body>
                <div class="card">
                  <h1>결제 결과를 처리하는 중입니다</h1>
                  <p>잠시 후 창이 자동으로 닫힙니다. 닫히지 않으면 직접 닫아주세요.</p>
                </div>
                <script>
                  (function () {
                    var payload = $payloadJson;
                    if (window.opener && !window.opener.closed) {
                      window.opener.postMessage(payload, "*");
                    }
                    window.setTimeout(function () {
                      window.close();
                    }, 800);
                  })();
                </script>
              </body>
            </html>
        """.trimIndent()
    }
}
