package ch.arcticsoft.spring.web;



import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.arcticsoft.spring.ChatRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
public class ChatController {
	
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String promptText = """
  	      You have access to tool functions.

  	      IMPORTANT TOOL RULES:
  	      - Before calling any other tool, you MUST call the tool named 'searchTools' to discover relevant tools.
  	      - If the user asks about time/date/now/today, you MUST call searchTools with a query like "current date time Zurich".
  	      - After searchTools returns results, call the most relevant tool.
  	      - If searchTools returns no relevant tools, answer normally without tools.
  	    """;
	private static final String promptText2 = """
			If you need to call a tool, do NOT print the tool call as text.
			Only call tools using the tool calling mechanism.
			If tool calling is unavailable, say: "TOOLS_UNAVAILABLE".
	  	    """;
	
    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping
    public Mono<String> chat(@RequestBody ChatRequest request) {
        String msg = request == null ? null : request.message();
        if (msg == null || msg.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "message must not be empty"
            );
        }
        log.info("*********************************************************************\nQuestion:\n{}", msg);
        return Mono.fromSupplier(() -> {
            var response = chatClient
                .prompt()
                .system(promptText)
                .user(msg.trim())
                .call(); // ⬅️ BLOCKING
            log.info("🧠 LLM RAW RESPONSE:\n{}", response);
            log.info("🧠 LLM FINAL CONTENT:\n{}", response.content());
            return response.content();
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
          .doOnNext(r -> log.info("📤 emitting response to client: '{}'", r))
          .doOnError(e -> log.error("❌ error in chat()", e));
    }

    
    /**
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody ch.arcticsoft.spring.ChatRequest request) {
    	log.debug("stream question: {}", request.message().trim());
        String msg = request == null ? null : request.message();
        if (msg == null || msg.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be empty");
        }

        return chatClientStream.prompt()
        	    .system(promtText2)
        	    .user(msg.trim())
        	    .stream()
        	    .content()
        	    .doOnSubscribe(s -> log.trace("LLM stream started"))
        	    .doOnNext(chunk -> log.trace("LLM chunk: [{}]", chunk))
        	    .doOnComplete(() -> log.trace("LLM stream completed"))
        	    .doOnError(e -> log.error("LLM stream error", e))
        	    .map(chunk -> chunk.startsWith(" ") ? " " + chunk : chunk);
    }*/
}