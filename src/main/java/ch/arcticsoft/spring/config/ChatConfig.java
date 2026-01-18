package ch.arcticsoft.spring.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.arcticsoft.spring.tools.TimeTools;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor;
import org.springaicommunity.tool.search.ToolSearcher;

@Configuration
public class ChatConfig {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Bean
  ToolSearchToolCallAdvisor toolSearchAdvisor(ToolSearcher toolSearcher) {
	log.info("toolSearchAdvisor");
    return ToolSearchToolCallAdvisor.builder()
        .toolSearcher(toolSearcher)
        .build();
  }

  @Bean
  ChatClient chatClient(ChatClient.Builder builder,
                        ToolSearchToolCallAdvisor advisor,
                        TimeTools timeTools
                        /* add other tool beans */) {
	log.info("chatClient");

    return builder
        // IMPORTANT: register tool beans so they can be indexed/searched
        .defaultTools(timeTools /*, ... */)
        // IMPORTANT: advisor does the indexing + tool-search flow
        .defaultAdvisors(advisor)
        .build();
  }
}