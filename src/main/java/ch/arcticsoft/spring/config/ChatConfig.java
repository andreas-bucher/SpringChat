package ch.arcticsoft.spring.config;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor;
import org.springaicommunity.tool.search.ToolSearcher;
import org.springaicommunity.tool.searcher.LuceneToolSearcher;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.arcticsoft.spring.tools.TimeTools;

@Configuration
public class ChatConfig {
  
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private ToolSearcher toolSearcher;
  private ToolSearchToolCallAdvisor toolSearchToolCallAdvisor;
	
  @Bean
  TimeTools timeTools() {
	log.info("timeTools");
    return new TimeTools();
  }

  @Bean
  ObjectMapper objectMapper() {
    return JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();
  }
  
  @Bean
  ToolSearcher toolSearcher() {
    log.info("toolSearcher");
    return new LuceneToolSearcher();
  }
  
  @Bean
  ToolSearchToolCallAdvisor toolSearchAdvisor(ToolSearcher toolSearcher) {
	log.info("toolSearchAdvisor");
	this.toolSearcher = toolSearcher;
	this.toolSearchToolCallAdvisor = toolSearchToolCallAdvisor;
	
    return ToolSearchToolCallAdvisor.builder()
        .toolSearcher(toolSearcher)
        .build();
  }

  @Bean
  ChatClient chatClient(ChatClient.Builder builder,
                        ToolSearchToolCallAdvisor toolSearchAdvisor,
                        TimeTools timeTools) {
	log.info("chatClient");
    return builder
        .defaultTools(timeTools)          // don’t `new` inside
        .defaultAdvisors(toolSearchAdvisor)
        .build();
  }
}