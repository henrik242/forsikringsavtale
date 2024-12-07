package demo.integrasjon

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.util.logging.Logger

@RestController
@CrossOrigin(originPatterns = ["*localhost:*"])
class IntegrasjonController(val http: HttpClient = HttpClient.newHttpClient()) {

    @PostMapping("/opprettAvtale", consumes = ["multipart/form-data"], produces = ["application/json"])
    fun opprettAvtale(@RequestParam kundeEpost: String, @RequestParam avtaleNavn: String): Map<String, String> {
        log.info("[opprettAvtale] $kundeEpost, $avtaleNavn")

        if (kundeEpost.isBlank()) {
            return fail("kundeEpost mangler")
        }
        if (avtaleNavn.isBlank()) {
            return fail("avtaleNavn mangler")
        }

        val kundeNummer = opprettFagsystemKunde(kundeEpost)
            ?: return fail("kunne ikke opprette kunde")

        val avtaleNummer = opprettFagsystemAvtale(kundeNummer, avtaleNavn)
            ?: return fail("kunne ikke opprette avtale")

        val avtaleSendt = sendAvtale(kundeEpost, kundeNummer, avtaleNummer, avtaleNavn)
        if (!avtaleSendt) return fail("avtale ble ikke sendt")

        val statusOk = avtaleStatus(avtaleNummer, avtaleSendt)
        if (!statusOk) return fail("kunne ikke sette avtalestatus")

        return mapOf(
            "avtaleNummer" to "$avtaleNummer",
            "avtaleStatus" to "$statusOk"
        )
    }

    @GetMapping("/visData", produces = ["application/json"])
    fun visData(): String? = postJson("http://fagsystem/visData", emptyMap())

    private fun fail(error: String): Map<String, String> {
        log.severe(error)
        return mapOf("error" to error)
    }

    private fun opprettFagsystemKunde(kundeEpost: String): Int? {
        val kundeData = mutableMapOf(
            "kundeEpost" to kundeEpost
        )
        val kundeNummer = postJson("http://fagsystem/opprettKunde", kundeData)
        log.info("[opprettAvtale] kundeNummer = $kundeNummer")
        return kundeNummer?.toIntOrNull()
    }

    private fun opprettFagsystemAvtale(kundeNummer: Int, avtaleNavn: String): Int? {
        val avtaleData = mutableMapOf(
            "kundeNummer" to "$kundeNummer",
            "avtaleNavn" to avtaleNavn
        )
        val avtaleNummer = postJson("http://fagsystem/opprettAvtale", avtaleData)
        log.info("[opprettAvtale] avtaleNummer = $avtaleNummer")
        return avtaleNummer?.toIntOrNull()
    }

    private fun sendAvtale(kundeEpost: String, kundeNummer: Int, avtaleNummer: Int, avtaleNavn: String): Boolean {
        val brevData = mutableMapOf(
            "kundeEpost" to kundeEpost,
            "kundeNummer" to "$kundeNummer",
            "avtaleNavn" to avtaleNavn,
            "avtaleNummer" to "$avtaleNummer"
        )

        val avtaleSendt = postJson("http://brevtjeneste/sendAvtale", brevData)
        log.info("[sendAvtale] avtaleSendt = $avtaleSendt")
        return avtaleSendt?.toBoolean() ?: false
    }

    private fun avtaleStatus(avtaleNummer: Int, avtaleSendt: Boolean): Boolean {
        val status = mutableMapOf(
            "avtaleNummer" to "$avtaleNummer",
            "avtaleSendt" to "$avtaleSendt"
        )
        val statusOK = postJson("http://fagsystem/avtaleStatus", status)
        log.info("[avtaleStatus] statusOK = $statusOK")
        return statusOK?.toBoolean() ?: false
    }

    private fun postJson(url: String, data: Map<String, String>): String? =
        try {
            val request = HttpRequest
                .newBuilder(URI.create(url))
                .header("content-type", "application/json")
                .POST(jsonBody(data))
                .build()

            http.send(request, HttpResponse.BodyHandlers.ofString()).body()
        } catch (e: IOException) {
            log.severe("Could not post json: $e")
            null
        }

    private fun jsonBody(kundeData: Map<String, String>): HttpRequest.BodyPublisher? =
        BodyPublishers.ofString(ObjectMapper().writeValueAsString(kundeData))

    companion object {
        var log = Logger.getLogger(IntegrasjonController.Companion::class.java.name)
    }
}
