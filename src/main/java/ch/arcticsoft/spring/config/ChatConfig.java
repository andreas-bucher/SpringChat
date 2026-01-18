package ch.arcticsoft.spring.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor;
import org.springaicommunity.tool.search.ToolSearcher;

@Configuration
public class ChatConfig {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ToolSearcher toolSearcher) {
    	log.info("chatClient ...");

        var advisor = ToolSearchToolCallAdvisor.builder()
                .toolSearcher(toolSearcher)
                .build();

        return builder
                .defaultAdvisors(advisor)
                .build();
    }
}