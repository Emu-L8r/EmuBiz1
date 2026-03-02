package com.emul8r.bizap.data.network

import com.emul8r.bizap.BaseUnitTest
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import kotlin.test.assertFailsWith

/**
 * Unit tests for ErrorInterceptor.
 * Verifies that raw HTTP errors are transformed into business exceptions.
 */
class ErrorInterceptorTest : BaseUnitTest() {
    
    private val interceptor = ErrorInterceptor()
    
    @Test
    fun `test 401 throws authentication exception`() {
        val chain = createMockChain(401)
        
        assertFailsWith<ApiException.AuthenticationException> {
            interceptor.intercept(chain)
        }
    }
    
    @Test
    fun `test 404 throws not found exception`() {
        val chain = createMockChain(404)
        
        assertFailsWith<ApiException.NotFoundException> {
            interceptor.intercept(chain)
        }
    }
    
    @Test
    fun `test 500 throws server exception`() {
        val chain = createMockChain(500)
        
        assertFailsWith<ApiException.ServerException> {
            interceptor.intercept(chain)
        }
    }

    private fun createMockChain(code: Int): Interceptor.Chain {
        val request = Request.Builder().url("https://test.com").build()
        
        // Fix: OkHttp responses need a body even if empty to be valid for some checks
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("Error")
            .body("".toResponseBody(null))
            .build()
            
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(any()) } returns response
        return chain
    }
}
