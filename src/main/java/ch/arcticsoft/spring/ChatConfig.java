package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor;
import org.springaicommunity.tool.search.ToolSearcher;
import org.springaicommunity.tool.searcher.LuceneToolSearcher;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.advisor.ChatModelCallAdvisor;
import org.springframework.ai.chat.client.advisor.ChatModelStreamAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.Model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.arcticsoft.spring.tools.TimeTools;

@Configuration
public class ChatConfig {
  
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private ToolSearcher toolSearcher;
  private ToolSearchToolCallAdvisor toolSearchToolCallAdvisor;
  private ApertusAdvisor luceneAdvisor;

  
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
  /**
  
  @Bean
  ToolSearcher toolSearcher() {
    log.info("toolSearcher");
    return new LuceneToolSearcher();
  }
  
  @Bean
  ToolSearchToolCallAdvisor toolSearchAdvisor(ToolSearcher toolSearcher) {
	log.info("toolSearchAdvisor");
	this.toolSearcher = toolSearcher;
	
    return ToolSearchToolCallAdvisor.builder()
        .toolSearcher(toolSearcher)
        .build();
  }*/
  
//  @Bean
//  LuceneAdvisor luceneAdvisor() {
//    log.info("luceneAdvisor");
//    this.luceneAdvisor = new LuceneAdvisor();
//    return luceneAdvisor;
//  }

  @Bean
  ChatClient chatClient(ChatClient.Builder builder,
		  				ChatModel chatModel
                        //ToolSearchToolCallAdvisor toolSearchToolCallAdvisor,
                        //LuceneAdvisor luceneAdvisor
                        ) {
	log.info("chatClient");
    return builder
        .defaultAdvisors(
        		//PreSelectToolCallAdvisor.builder().build(),
        		ApertusAdvisor.builder().build(),
        		ChatModelStreamAdvisor.builder().chatModel(chatModel).build() 
        	)
        .build();
  }
  
  /**
  @Bean
  ChatClient chatClient(ChatClient.Builder builder,
		  				ChatModel chatModel,
                        //ToolSearchToolCallAdvisor toolSearchToolCallAdvisor,
                        LuceneAdvisor luceneAdvisor,
                        TimeTools timeTools) {
	log.info("chatClient");
	//this.toolSearchToolCallAdvisor = toolSearchToolCallAdvisor;
    return builder
        //.defaultTools(timeTools)
        //.defaultAdvisors(toolSearchToolCallAdvisor, SimpleLoggerAdvisor.builder().build())
        //.defaultAdvisors(SimpleLoggerAdvisor.builder().build())
        //.defaultAdvisors(toolSearchToolCallAdvisor)
        
        .defaultAdvisors(
        		this.luceneAdvisor,
        		ChatModelStreamAdvisor.builder().chatModel(chatModel).build() 
        	)
        .build();
  }*/
}