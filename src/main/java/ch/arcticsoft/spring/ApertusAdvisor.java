package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.search.ToolReference;
import org.springaicommunity.tool.search.ToolSearchResponse;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;

import ch.arcticsoft.spring.embed.DesigningAiRagService;
import ch.arcticsoft.spring.tools.TimeTools;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class ApertusAdvisor extends ToolCallAdvisor{

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final ToolsService           toolsService;
	private final TimeTools              timeTools = new TimeTools();
	private final DesigningAiRagService  designingAiRagService;
	
	public ApertusAdvisor(
			ToolCallingManager     toolCallingManager,
			int                    advisorOrder, 
			ToolsService           toolsService,
			DesigningAiRagService  designingAiRagService
			) {
		super(toolCallingManager, advisorOrder);
		log.debug("ApertusAdvisor");
		this.toolsService          = toolsService;
		this.designingAiRagService = designingAiRagService;
	}

	
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(
			ChatClientRequest  chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		
		log.debug("adviseStream");
		this.logRequest("origin", chatClientRequest);

		String userQuery = chatClientRequest.prompt().getInstructions().stream()
											.filter(UserMessage.class::isInstance)
											.map(UserMessage.class::cast)
											.map(UserMessage::getText)
											.reduce((a, b) -> b)
											.orElse("");
		
		log.info("userQuery: {}", userQuery);
		
		Mono<ToolSearchResponse> toolSearchResponse2 = this.toolsService.toolSearch(userQuery);
		return toolSearchResponse2
	            .flatMapMany(resp -> {
	            		log.debug("flatMapMany");
	            		log.debug("ToolSearch .. {}", resp.toolReferences().size());
	            		
	            		for(ToolReference ref : resp.toolReferences()) {
	            			log.debug("    --> {}  Tool: {}", ref.relevanceScore(), ref.toolName());
	            		}
	            		
	            		if (resp.toolReferences().isEmpty()) {
	            		    log.debug("No tool references found");
	            		    log.info("******** NOT TOOL CALL ******** ");
		        	        Flux<ChatClientResponse> responses = streamAdvisorChain.nextStream(chatClientRequest);
		        	        return new ChatClientMessageAggregator().aggregateChatClientResponse(responses, this::logResponse);

	            		} 
	            		String toolName = resp.toolReferences().getFirst().toolName();
	            		
	            		Mono<ChatClientRequest> enrichedChatClientRequest;
	            		
	            		if( toolName.equals("nowZurich") ) {
		        	        log.info("******** TOOL CALL {} ******** ", toolName);
	            			enrichedChatClientRequest = timeTools.enrichChatClientRequest(chatClientRequest);

	            		} else if(  toolName.equals("outline_MIT_AI_course")  ||  toolName.equals("semantic_MIT_AI_course")  ) {
		        	        log.info("******** TOOL CALL {} ******** ", toolName);
		        	        enrichedChatClientRequest = designingAiRagService.enrichChatClientRequest(chatClientRequest, userQuery);
		        	        
	            		} else {
		        	        log.info("******** TOOL CALL {} ******** not implemented", toolName);
		        	        enrichedChatClientRequest = Mono.just(chatClientRequest);
		        	        
	            		}
	            		return enrichedChatClientRequest.flatMapMany( req -> {
	            			this.logRequest("enriched", req);
		        	        Flux<ChatClientResponse> responses = streamAdvisorChain.nextStream(req);
		        	        return new ChatClientMessageAggregator().aggregateChatClientResponse(responses, this::logResponse); 

	            		});

				});
	}


	protected void logRequest(String tag, ChatClientRequest request) {
		log.trace("adviseStream - ({}) request: {}", tag, request);
	}
	
	protected void logResponse(ChatClientResponse chatClientResponse) {
		log.trace("adviceStream - response: {}", chatClientResponse);
	}
	
	
	
	/**
	 * Creates a new Builder instance for constructing a PreSelectToolCallAdvisor.
	 * @return a new Builder instance
	 */
	public static Builder<?> builder() {
		return new Builder<>();
	}


	public static class Builder<T extends Builder<T>> extends ToolCallAdvisor.Builder<T> {

		private ToolsService toolsService;
		private DesigningAiRagService designingAiRagService;
		
		protected Builder() {
		}
		
		public T toolsService(ToolsService toolsService) {
			this.toolsService = toolsService;
			log.debug("Builder.toolsService");
			return self();
		}
		
		public T designingAiRagService(DesigningAiRagService designingAiRagService) {
			this.designingAiRagService = designingAiRagService;
			log.debug("Builder.designingAiRagService");
			return self();
		}

		@Override
		public ApertusAdvisor build() {
			log.debug("build");
			
		    if (this.toolsService == null) {
		        throw new IllegalStateException("toolsService must be set");
		    }
		    if (this.designingAiRagService == null) {
		        throw new IllegalStateException("designingAiRagService must be set");
		    }
			
			return new ApertusAdvisor(
					getToolCallingManager(), 
					getAdvisorOrder(), 
					this.toolsService, 
					this.designingAiRagService);
		}
		
	}	
}
