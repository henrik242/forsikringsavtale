package demo.klient

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class KlientController {

    @GetMapping("/index")
   fun index(): String = "index"
}
