package demo.klient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KlientApplication

fun main(args: Array<String>) {
	runApplication<KlientApplication>(*args)
}
