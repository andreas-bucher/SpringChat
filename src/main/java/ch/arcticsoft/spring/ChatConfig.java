package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.advisor.ChatModelStreamAdvisor;
import org.springframework.ai.chat.model.ChatModel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.arcticsoft.spring.embed.DesigningAiRagService;
import ch.arcticsoft.spring.tools.TimeTools;

@Configuration
public class ChatConfig {
  
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
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
  ChatClient chatClient(ChatClient.Builder     builder,
		  				ChatModel              chatModel,
		  				ToolsService           toolsService,
		  				DesigningAiRagService  designingAiRagService
                        ) {
	log.info("chatClient");
	log.debug("ToolsService: {}", toolsService);
    return builder
        .defaultAdvisors(
        		ApertusAdvisor.builder().toolsService(toolsService).designingAiRagService(designingAiRagService).build(),
        		ChatModelStreamAdvisor.builder().chatModel(chatModel).build() 
        	)
        .build();
  }
  
}