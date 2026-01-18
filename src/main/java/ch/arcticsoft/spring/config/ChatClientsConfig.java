package ch.arcticsoft.spring.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springaicommunity.tool.search.ToolSearcher;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor;

@Configuration
public class ChatClientsConfig {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/**
    @Bean
    ToolSearchToolCallAdvisor toolSearchAdvisor(ToolSearcher toolSearcher) {
    	log.info("toolSearchAdvisor");
        return ToolSearchToolCallAdvisor.builder()
                .toolSearcher(toolSearcher)
                .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder,
                          ToolSearchToolCallAdvisor advisor) {
    	log.info("chatClient ...");
        return builder
                .defaultAdvisors(advisor)   // ✅ tools + search work here
                .build();
    }
    
    
    /** Non-streaming client: advisors OK here */
    /**
    @Bean
    @Qualifier("chatClientTools")
    ChatClient chatClientTools(ChatClient.Builder builder, ToolSearchToolCallAdvisor advisor) {
    	log.info("chatClientTools");
        return builder
                .defaultAdvisors(advisor)
                .build();
    }*/

    /** Streaming client: NO advisors (M1 limitation) */
    /**
    @Bean
    @Qualifier("chatClientStream")
    ChatClient chatClientStream(ChatClient.Builder builder) {
    	log.info("chatClientStream");
        return builder.build();
    }*/
}