package com.athul.bhaang;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BhaangApplicationTests {

	@MockitoBean
	private VectorStore vectorStore;

	@MockitoBean
	private ChatClient chatClient;

	@Test
	void contextLoads() {}
}