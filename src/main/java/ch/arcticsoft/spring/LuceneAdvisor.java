package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;

import reactor.core.publisher.Flux;


public class LuceneAdvisor implements StreamAdvisor {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
	private final int order = 0;
	
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
		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

		return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);

	}

	protected void logRequest(ChatClientRequest request) {
		log.debug("adviseStream - request: {}", request);
	}
	
	protected void logResponse(ChatClientResponse chatClientResponse) {
		log.debug("adviceStream - response: {}", chatClientResponse);
	}	
	
	
	
/*
  @Override
  public AdvisorContext advise(AdvisorContext ctx) {

    // Grab last user message (typical)
    var lastUser = ctx.prompt().getInstructions().stream()
        .filter(m -> m instanceof UserMessage)
        .map(m -> (UserMessage) m)
        .reduce((a,b) -> b)
        .orElse(null);

    if (lastUser == null) return ctx;

//    var question = lastUser.getContent();
//    if (!RagHeuristics.shouldSearch(question)) return ctx;
//
//    var hits = rag.search(question, 6);
//    if (hits.isEmpty()) return ctx;

    var contextBlock = new StringBuilder();
    contextBlock.append("""
      You may use the following retrieved context.
      - If the context is irrelevant, ignore it.
      - If you use it, cite the source tag like [source: ...].
      
      <context>
      """);

//    for (int i = 0; i < hits.size(); i++) {
//      var h = hits.get(i);
//      contextBlock.append("\n[chunk ").append(i + 1).append("] ")
//          .append("[source: ").append(h.source()).append("]\n")
//          .append(h.text()).append("\n");
//    }
    contextBlock.append("</context>\n");

    // Inject as a SYSTEM message (strong guidance)
    ctx.prompt().getInstructions().addFirst(new SystemMessage(contextBlock.toString()));
    return ctx;
  }
	*/
	
	

	
}
