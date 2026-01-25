package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ChatController {
	
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final ObjectMapper objectMapper;
    private final ChatClient chatClient;

	private static final String promptText = """
You have access to tool-calling, but you must NOT assume which tools exist. Tool availability is dynamic.

Tool-use policy:
1) If the user’s request requires external actions or structured retrieval (e.g., search, lookup, fetch current information, compute using a tool, access app data), FIRST call the tool-search capability to discover the best tool(s) for the task.
2) Do NOT invent tool names. Only call tools that were returned by tool-search.
3) If tool-search returns no suitable tools, continue without tools and explain what you can do and what you cannot do.
4) Do not call tool-search for simple conversational requests that you can answer directly.

Execution policy:
- Prefer to gather the minimum tool information needed, then produce the final answer.
- Do not call any tools after you have produced the final answer.
- If the user asks for “now/current time/date”, treat it as requiring a tool unless you are explicitly told to answer without tools.

Style:
- Be concise, accurate, and practical.
  	    """;

	private static final String promptTextApertus = """
You are a helpful assistent. Please answer the question.
  	    """;
	
	
    public record ChatRequest(String message) {}
	
	public ChatController(
			ChatClient chatClient, 
			ObjectMapper objectMapper) {
		
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }
    
	@RequestMapping("/chat")
    @PostMapping
    public Mono<String> chat(@RequestBody ChatRequest request) {
        String msg = request == null ? null : request.message();
        if (msg == null || msg.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "message must not be empty"
            );
        }
        log.info("*********************************************************************");
        log.info("Question: {}", msg);
        return Mono.fromSupplier(() -> {
            var call = chatClient
                .prompt()
                .system(promptText)
                .user(msg.trim())
                .call(); // ⬅️ BLOCKING
            
            var cr = call.chatResponse();
            var out = cr.getResult().getOutput();
            
            //log.info("🧠 LLM RAW ChatResponse: {}", toJson(cr));
            log.info("🛠 toolCalls: {}", out.getToolCalls());
            //log.info("🧠 assistant text: {}", out.getText());
            
            return out.getText();
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
          //.doOnNext(r -> log.info("📤 emitting response to client: '{}'", r))
          .doOnError(e -> log.error("❌ error in chat()", e));
    }
    
    @PostMapping(
    		value="/stream",
    		consumes = MediaType.APPLICATION_JSON_VALUE,
    		produces = MediaType.TEXT_EVENT_STREAM_VALUE
    		)
    public Flux<ServerSentEvent<String>> stream(@RequestBody ChatRequest request) {
      log.info("stream");
      String msg = request == null ? null : request.message();
      if (msg == null || msg.trim().isEmpty()) {
          return Flux.error(new ResponseStatusException(
                  HttpStatus.BAD_REQUEST,
                  "message must not be empty"
          ));
      }
      log.info("*********************************************************************");
      log.info("Question: {}", msg);
      Flux<String> stream = chatClient.prompt()
    	      .system(promptText)
    	      .user(msg.trim())
    	      .stream()
    	      .content()
    	      .doOnSubscribe(s -> log.info("📡 stream started"))
    	      .doOnComplete(() -> log.info("✅ stream completed"))
    	      .doOnError(e -> log.error("❌ error in stream()", e))
    	      .share();

    	  Flux<ServerSentEvent<String>> tokens =
    	      stream.map(t -> ServerSentEvent.builder(t).event("token").build());

    	  Flux<ServerSentEvent<String>> keepAlive =
    	      Flux.interval(Duration.ofSeconds(15))
    	          .map(i -> ServerSentEvent.<String>builder().event("keepalive").data("").build())
    	          .takeUntilOther(stream.ignoreElements());

    	  return Flux.merge(tokens, keepAlive);
  }
    
    
  private String toJson(Object o) {
    	  try {
    	    return objectMapper
    	        .writerWithDefaultPrettyPrinter()
    	        .writeValueAsString(o);
    	  } catch (Exception e) {
    	    return "<json-serialization-failed: " + e.getMessage() + ">";
    	  }
    	}	
}
