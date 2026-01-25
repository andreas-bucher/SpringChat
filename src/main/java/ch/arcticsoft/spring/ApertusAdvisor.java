package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.searcher.LuceneToolSearcher;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import ch.arcticsoft.spring.tools.TimeTools;
import reactor.core.publisher.Flux;


public class ApertusAdvisor implements StreamAdvisor {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
	private final int order;
	
	private LuceneToolSearcher luceneToolSearcher;
	
	public ApertusAdvisor(int order) {
		this.order = order;
		luceneToolSearcher = new LuceneToolSearcher();
		
	}
	
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(
			ChatClientRequest  chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		
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
	

	public static Builder builder() {
		return new Builder();
	}

	
	public static final class Builder {

		private int order = 0;

		private Builder() {
		}

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public ApertusAdvisor build() {
			return new ApertusAdvisor(this.order);
		}

	}
	
	
}
