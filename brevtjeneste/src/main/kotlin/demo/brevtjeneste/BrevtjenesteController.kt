package demo.brevtjeneste

import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

@RestController
class BrevtjenesteController {

    @PostMapping("/sendAvtale", consumes = ["application/json"])
    fun sendAvtale(@RequestBody brev: Brev): Boolean {
        log.info("[sendAvtale] $brev")
        return if (brev.kundeEpost.contains(".+@.+\\..+".toRegex())) {
            true
        } else {
            log.severe("ugyldig epost: ${brev.kundeEpost}")
            false
        }
    }

    data class Brev(val kundeEpost: String, val kundeNummer: Int, val avtaleNavn: String, val avtaleNummer: Int)

    companion object {
        var log = Logger.getLogger(BrevtjenesteController.Companion::class.java.name)
    }

}