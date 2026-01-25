package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.searcher.LuceneToolSearcher;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;

import reactor.core.publisher.Flux;


public class LuceneAdvisor implements StreamAdvisor {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
	private final int order;
	
	private LuceneToolSearcher luceneToolSearcher;
	
	public LuceneAdvisor(int order) {
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
		
		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

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

		public LuceneAdvisor build() {
			return new LuceneAdvisor(this.order);
		}

	}
	
	
}
