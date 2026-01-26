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
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;

import ch.arcticsoft.spring.tools.TimeTools;
import reactor.core.publisher.Flux;


public class ApertusAdvisor extends ToolCallAdvisor{

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
	private final LuceneToolSearcher luceneToolSearcher;
	//private final VectorToolSearcher vectorToolSearcher;
	private final Random random = new Random();
	@Autowired
	private TimeTools timeTools;
	//private final ToolIndexer toolIndexer;
	
	public ApertusAdvisor(
			ToolCallingManager toolCallingManager,
			VectorStore vectorStore,
			int advisorOrder
			) {
		super(toolCallingManager, advisorOrder);
		log.debug("ApertusAdvisor");
		//this.autoToolRegistration = autoToolRegistration;
		this.luceneToolSearcher = new LuceneToolSearcher();

		//this.vectorToolSearcher = new VectorToolSearcher(vectorStore);
	}
	/*
	@Bean
	ToolSearcher toolSearcher() {
	  return new LuceneToolSearcher(0.4f);
	}*/
	
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	
	@Override
	public Flux<ChatClientResponse> adviseStream(
			ChatClientRequest  chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		
		this.logRequest(chatClientRequest);
		/*
		if (chatClientRequest.prompt().getOptions() instanceof ToolCallingChatOptions toolOptions) {
			log.debug("ToolCallingChatOptions ---> guess never called");
			String conversationId = "default_" + random.nextInt();
			var toolDefinitions = this.toolCallingManager.resolveToolDefinitions(toolOptions);
			log.debug(" ToolDefinitions.size: {}", toolDefinitions.size());
			toolDefinitions.stream()
				.forEach(toolDef -> {
					log.debug("ToolDef: {}  inputSchema: {}", toolDef.name(), toolDef.inputSchema());
				});
		}*/
		
		String sessionId = "default_"+ random.nextInt();
		String toolId    = "tool-1";
		String toolName  = "Date/Time Tool";
		String toolDescription = """
		        Returns the current date and time today/now/time in the Europe/Zurich timezone.
		        Always includes both date and time in ISO-8601 format.
		        """;
		
		
		//this.autoToolRegistration.setToolCalbackProvider();
		
		String userQuery = chatClientRequest.prompt().getInstructions().stream()
											.filter(UserMessage.class::isInstance)
											.map(UserMessage.class::cast)
											.map(UserMessage::getText)
											.reduce((a, b) -> b)
											.orElse("");
		
		log.info("userQuery: {}", userQuery);
		
		this.luceneToolSearcher.add(sessionId, toolId, toolName, toolDescription);
		log.debug("Indexed tool [{}] for session {}", toolName, sessionId);
		
		ToolSearchRequest toolSearchRequest = new ToolSearchRequest(sessionId, userQuery, 5, null);
		ToolSearchResponse toolSearchResponse = this.luceneToolSearcher.search(toolSearchRequest);
		log.debug("luceneToolSearcher.search ... matches: {}", toolSearchResponse.toolReferences().size());
		List<ToolReference> toolReferences = toolSearchResponse.toolReferences();
		for(ToolReference ref : toolReferences) {
			log.info("  ->  Tool found: {} relevanceScore: {} ", ref.toolName(), ref.relevanceScore());
		}
			
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
		
		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(enrichedChatClientRequest);

		return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);

	}

	protected void logRequest(ChatClientRequest request) {
		log.debug("adviseStream - request: {}", request);
	}
	
	protected void logResponse(ChatClientResponse chatClientResponse) {
		log.debug("adviceStream - response: {}", chatClientResponse);
	}	
	/**
	 * Creates a new Builder instance for constructing a PreSelectToolCallAdvisor.
	 * @return a new Builder instance
	 */
	public static Builder<?> builder() {
		return new Builder<>();
	}

	/**
	 * Builder for creating instances of PreSelectToolCallAdvisor.
	 * <p>
	 * This builder extends {@link ToolCallAdvisor.Builder} and adds configuration options
	 * specific to tool search functionality.
	 *
	 * @param <T> the builder type, used for self-referential generics to support method
	 * chaining in subclasses
	 */
	public static class Builder<T extends Builder<T>> extends ToolCallAdvisor.Builder<T> {

		//private ToolSearcher toolSearcher;
		//private AutoToolRegistration autoToolRegistration;

		protected Builder() {
		}

		/**
		 * Sets the ToolSearcher to be used for finding tools.
		 * @param toolSearcher the ToolSearcher instance
		 * @return this Builder instance for method chaining
		 */ /**
		public T toolSearcher(ToolSearcher toolSearcher) {
			log.debug("T  -   toolSearcher");
			this.toolSearcher = toolSearcher;
			return self();
		}
		
		public T autoToolRegistration(AutoToolRegistration autoToolRegistration) {
			log.debug("T  -  autoToolRegistration");
			this.autoToolRegistration = autoToolRegistration;
			return self();
		}*/

		/**
		 * Builds and returns a new PreSelectToolCallAdvisor instance with the configured
		 * properties.
		 * @return a new PreSelectToolCallAdvisor instance
		 * @throws IllegalArgumentException if required parameters are null or invalid
		 */
		/**
		@Override
		public ApertusAdvisor build() {
			log.debug("build");
			return new ApertusAdvisor(getToolCallingManager(), getAdvisorOrder());
		}
		*/
	}	
}
