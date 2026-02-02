package com.hygor.makeup_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.hygor.makeup_api.gateway.FileStorageGateway;

@SpringBootTest
class MakeupApiApplicationTests {

	@MockBean
    private FileStorageGateway fileStorageGateway;

	@Test
	void contextLoads() {
	}

}
