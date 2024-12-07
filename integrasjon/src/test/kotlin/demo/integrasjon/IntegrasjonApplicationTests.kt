package demo.integrasjon

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import kotlin.test.assertTrue

@SpringBootTest
class IntegrasjonApplicationTests {

	@Autowired
	lateinit var context: ApplicationContext

	@Test
	fun contextLoads() {
		assertTrue(context.beanDefinitionCount > 0)
	}
}
