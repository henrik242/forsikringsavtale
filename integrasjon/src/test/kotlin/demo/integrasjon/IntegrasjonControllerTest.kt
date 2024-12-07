import demo.integrasjon.IntegrasjonController
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandler

class IntegrasjonControllerTest {
    private var httpClient: HttpClient = mockk()
    private val controller = IntegrasjonController(httpClient)

    @Test
    fun `should create avtale successfully`() {
        val kundeNummer = "123"
        val avtaleNummer = "456"

        every { httpClient.send(any<HttpRequest>(), any<BodyHandler<String>>()) } answers {
            when (val request = firstArg<HttpRequest>().uri().toString()) {
                "http://fagsystem/opprettKunde" -> createMockedHttpResponse(200, kundeNummer)
                "http://fagsystem/opprettAvtale" -> createMockedHttpResponse(200, avtaleNummer)
                "http://brevtjeneste/sendAvtale" -> createMockedHttpResponse(200, "true")
                "http://fagsystem/avtaleStatus" -> createMockedHttpResponse(200, "true")
                else -> throw RuntimeException("Unexpected request: $request")
            }
        }
        val result = controller.opprettAvtale("test@example.com", "Testavtale")

        val expectedResult = mapOf(
            "avtaleNummer" to avtaleNummer,
            "avtaleStatus" to "true"
        )
        assertEquals(expectedResult, result)
    }

    @Test
    fun `should handle missing kundeEpost`() {
        val result = controller.opprettAvtale("", "Testavtale")

        assertEquals("kundeEpost mangler", result["error"])
    }

    @Test
    fun `should handle external system failure`() {
        val mockedResponse = createMockedHttpResponse(500)

        every { httpClient.send(any<HttpRequest>(), any<BodyHandler<String>>()) } returns mockedResponse

        val result = controller.opprettAvtale("test@example.com", "Test Avtale")

        assertEquals("kunne ikke opprette kunde", result["error"])
    }

    private fun createMockedHttpResponse(statusCode: Int, body: String? = null): HttpResponse<String> {
        val mockResponse = mockk<HttpResponse<String>>()
        every { mockResponse.statusCode() } returns statusCode
        every { mockResponse.body() } returns body
        return mockResponse
    }

    // ... other test cases for JSON serialization errors, logging, etc.
}