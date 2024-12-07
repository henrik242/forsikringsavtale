package demo.fagsystem

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

@SpringBootTest
class FagsystemApplicationTests {

	@Autowired
	lateinit var context: ApplicationContext

	@Test
	fun contextLoads() {
		assertTrue(context.beanDefinitionCount > 0)
	}
}
