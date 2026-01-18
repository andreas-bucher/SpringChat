package ch.arcticsoft.spring.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springaicommunity.tool.search.ToolSearcher;
import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor;

@Configuration
public class ChatClientsConfig {

    @Bean
    ToolSearchToolCallAdvisor toolSearchAdvisor(ToolSearcher toolSearcher) {
        return ToolSearchToolCallAdvisor.builder()
                .toolSearcher(toolSearcher)
                .build();
    }

    /** Non-streaming client: advisors OK here */
    @Bean
    @Qualifier("chatClientTools")
    ChatClient chatClientTools(ChatClient.Builder builder, ToolSearchToolCallAdvisor advisor) {
        return builder
                .defaultAdvisors(advisor)
                .build();
    }

    /** Streaming client: NO advisors (M1 limitation) */
    @Bean
    @Qualifier("chatClientStream")
    ChatClient chatClientStream(ChatClient.Builder builder) {
        return builder.build();
    }
}