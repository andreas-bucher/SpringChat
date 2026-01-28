package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.search.ToolReference;
import org.springaicommunity.tool.search.ToolSearchRequest;
import org.springaicommunity.tool.search.ToolSearchResponse;
import org.springaicommunity.tool.searcher.LuceneToolSearcher;
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
import org.springframework.beans.factory.annotation.Autowired;

import ch.arcticsoft.spring.tools.TimeTools;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class ApertusAdvisor extends ToolCallAdvisor{

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
	private final LuceneToolSearcher luceneToolSearcher;
	private final ToolsService toolsService;
	private final Random random = new Random();

	@Autowired
	private TimeTools timeTools;
	
	public ApertusAdvisor(
			ToolCallingManager toolCallingManager,
			int advisorOrder, 
			//VectorToolSearcher vectorToolSearcher,
			ToolsService toolsService
			) {
		super(toolCallingManager, advisorOrder);
		log.debug("ApertusAdvisor");
		this.luceneToolSearcher = new LuceneToolSearcher();
		//this.vectorToolSearcher = vectorToolSearcher;
		this.toolsService = toolsService;
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
		this.logRequest(chatClientRequest);
//		String sessionId = "session_" + random.nextInt();

		String userQuery = chatClientRequest.prompt().getInstructions().stream()
											.filter(UserMessage.class::isInstance)
											.map(UserMessage.class::cast)
											.map(UserMessage::getText)
											.reduce((a, b) -> b)
											.orElse("");
		
		log.info("userQuery: {}", userQuery);
		
		Mono<ToolSearchResponse> toolSearchResponse2 = this.toolsService.toolSearch(userQuery);
		return toolSearchResponse2
//				.doOnNext(resp -> {
//							log.debug("doOnNext - ToolSearch .. {}", resp.toolReferences().size());
//						})
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
	            		
	            		if( toolName.equals("nowZurich") ) {
	            			log.debug(" -- ");

		        			String ctx = new TimeTools().nowZurich();
	
		        	        String ctxBlock3 = """
		        	                Use the provided information to answer the question precise and concise.
		        	                %s
		        	                """.formatted(ctx == null ? "" : ctx);
		        	        
		        	        
		        	        Prompt original = chatClientRequest.prompt();
	
		        	        List<Message> newMessages = new ArrayList<>();
		        	        newMessages.add(new SystemMessage( ctxBlock3 )); // ← the ONE system message
	
		        	        // keep all non-system messages
		        	        for (Message m : original.getInstructions()) {
		        	            if (!(m instanceof SystemMessage)) {
		        	                newMessages.add(m);
		        	            }
		        	        }
	
		        	        Prompt newPrompt = new Prompt(newMessages, original.getOptions());        
		        	        ChatClientRequest enrichedChatClientRequest = chatClientRequest.mutate()
		        	        		.prompt(newPrompt)
		        	        		.build();
		        	        this.logRequest(enrichedChatClientRequest);
		        	        log.info("******** TOOL CALL {} ******** ", toolName);
		        	        Flux<ChatClientResponse> responses = streamAdvisorChain.nextStream(enrichedChatClientRequest);
		        	        return new ChatClientMessageAggregator().aggregateChatClientResponse(responses, this::logResponse);
		        	        
	            		}else {
		        	        log.info("******** TOOL CALL {} ******** not implemented", toolName);
		        	        Flux<ChatClientResponse> responses = streamAdvisorChain.nextStream(chatClientRequest);
		        	        return new ChatClientMessageAggregator().aggregateChatClientResponse(responses, this::logResponse);
	            		}
	        	        

		        	        
				});
		
	}

	
	
	//@Override
	public Flux<ChatClientResponse> adviseStreamXX(
			ChatClientRequest  chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		log.debug("adviseStream");
		this.logRequest(chatClientRequest);
		
		
		String userQuery = chatClientRequest.prompt().getInstructions().stream()
											.filter(UserMessage.class::isInstance)
											.map(UserMessage.class::cast)
											.map(UserMessage::getText)
											.reduce((a, b) -> b)
											.orElse("");
		
		log.info("userQuery: {}", userQuery);
		String ctx = new TimeTools().nowZurich();
        String ctxBlock3 = """
                Use the provided information to answer the question precise and concise.
                %s
                """.formatted(ctx == null ? "" : ctx);
        
        Prompt original = chatClientRequest.prompt();

        List<Message> newMessages = new ArrayList<>();
        newMessages.add(new SystemMessage( ctxBlock3 )); // ← the ONE system message
        for (Message m : original.getInstructions()) {
            if (!(m instanceof SystemMessage)) {
                newMessages.add(m);
            }
        }

        Prompt newPrompt = new Prompt(newMessages, original.getOptions());        
        ChatClientRequest enrichedChatClientRequest = chatClientRequest.mutate()
        		.prompt(newPrompt)
        		.build();
        
		this.logRequest(enrichedChatClientRequest);
		
		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(enrichedChatClientRequest);
		return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
	}

	protected void logRequest(ChatClientRequest request) {
		log.trace("adviseStream - request: {}", request);
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
		
		protected Builder() {
		}
		
		public T toolsService(ToolsService toolsService) {
			this.toolsService = toolsService;
			return self();
		}

		@Override
		public ApertusAdvisor build() {
			log.debug("build");
			return new ApertusAdvisor(getToolCallingManager(), getAdvisorOrder(), this.toolsService);
		}
		
	}	
}
