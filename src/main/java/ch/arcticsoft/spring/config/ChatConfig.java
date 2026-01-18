package ch.arcticsoft.spring.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor;
import org.springaicommunity.tool.search.ToolSearcher;

@Configuration
public class ChatConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ToolSearcher toolSearcher) {

        var advisor = ToolSearchToolCallAdvisor.builder()
                .toolSearcher(toolSearcher)
                .build();

        return builder
                .defaultAdvisors(advisor)
                .build();
    }
}