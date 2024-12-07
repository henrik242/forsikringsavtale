package demo.fagsystem

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

@RestController
class FagsystemController {

    val kunder = mutableMapOf<Int, String>()
    private val kundeNummerGenerator = AtomicInteger(0)

    val avtaler = mutableMapOf<Int, String>()
    private val avtaleNummerGenerator = AtomicInteger(0)

    val avtaleMapping = mutableMapOf<Int, Int?>()
    val avtaleStatus = mutableMapOf<Int, Boolean>()

    @PostMapping("/opprettKunde", consumes = ["application/json"])
    fun opprettKunde(@RequestBody kunde: KundeEpost): Int? {
        log.info("[opprettKunde] $kunde")

        if (!kunde.kundeEpost.contains(".+@.+\\..+".toRegex())) {
            log.severe("kundeEpost har feil format: ${kunde.kundeEpost}")
            return null
        }
        if (kunder.containsValue(kunde.kundeEpost)) {
            return kunder.entries.find { it.value == kunde.kundeEpost }?.key
                ?: throw RuntimeException("this cannot happen")
        }

        val kundeNummer = kundeNummerGenerator.incrementAndGet()
        log.info("[opprettKunde] kundeNummer = $kundeNummer")
        kunder[kundeNummer] = kunde.kundeEpost
        return kundeNummer
    }

    data class KundeEpost(val kundeEpost: String)

    @PostMapping("/opprettAvtale", consumes = ["application/json"])
    fun opprettAvtale(@RequestBody avtale: Avtale): Int {
        log.info("[opprettAvtale] $avtale")

        val avtaleNummer = avtaleNummerGenerator.incrementAndGet()
        log.info("[opprettAvtale] avtaleNummer = $avtaleNummer")

        avtaler[avtaleNummer] = avtale.avtaleNavn
        avtaleMapping[avtaleNummer] = avtale.kundeNummer
        return avtaleNummer
    }

    data class Avtale(val avtaleNavn: String, val kundeNummer: Int)

    @PostMapping("/avtaleStatus", consumes = ["application/json"])
    fun avtaleStatus(@RequestBody status: AvtaleStatus): Boolean {
        log.info("[avtalestatus] $status")
        avtaleStatus[status.avtaleNummer] = status.avtaleSendt
        return true
    }

    @PostMapping("/visData", produces = ["application/json"])
    fun visData(): Map<String, Any> {
        val avtaleDetaljer = avtaleMapping.entries
            .associate {
                it.key to mapOf(
                    "avtaleNavn" to avtaler[it.key],
                    "avtaleStatus" to (avtaleStatus[it.key] ?: false),
                    "kundeNummer" to it.value,
                    "kundeEpost" to kunder[it.value]
                )
            }

        return mapOf(
            "kunder" to kunder,
            "avtaler" to avtaleDetaljer
        )
    }

    data class AvtaleStatus(val avtaleNummer: Int, val avtaleSendt: Boolean)

    companion object {
        var log = Logger.getLogger(Companion::class.java.name)
    }
}
