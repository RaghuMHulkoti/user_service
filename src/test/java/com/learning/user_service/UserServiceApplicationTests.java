package com.learning.user_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIf("com.learning.user_service.support.LocalInfrastructure#available")
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
