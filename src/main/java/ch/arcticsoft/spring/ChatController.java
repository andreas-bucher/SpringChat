package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.arcticsoft.spring.embed.DesigningAiRagService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ChatController {
	
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final ObjectMapper objectMapper;
    private final ChatClient chatClient;
	private final DesigningAiRagService designingAiRagService;

	private static final String promptText = """
You are an expert technical assistant.

You reason carefully, step by step, but you only output the final answer unless explicitly asked for reasoning.

Rules:
- Be precise, concise, and correct.
- Prefer structured answers (headings, bullet points, tables).
- When unsure, say so explicitly instead of guessing.
- Do not invent APIs, classes, or configuration values.
- Use concrete examples when helpful.

Code:
- When producing code, ensure it is complete, minimal, and idiomatic.
- Match the requested language, framework, and versions exactly.
- Avoid unnecessary abstractions.

Output:
- Default language: English
- No emojis unless explicitly requested.
  	    """;

	private static final String promptText2 = """
You are an expert technical assistant.
  	    """;
	
    public record ChatRequest(String message) {}

    public record QdrantDebugSearchRequest(
            String collection,
            String query,
            Integer topK,
            Double similarityThreshold,
            Map<String, Object> filters,
            Boolean includeEmbeddings
    ) {}
    
	public ChatController(
			ChatClient chatClient, 
			ObjectMapper objectMapper,
			DesigningAiRagService designingAiRagService) {
		
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        this.designingAiRagService = designingAiRagService;
    }
    
	  
  @PostMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> debug(
		  @RequestBody String req, 
		  ServerWebExchange exchange) {
	log.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXX debug ...");
	
	log.debug("test request:  {}", req);
	
	return Mono.just("ok");
  }

  @PostMapping(
  		value="/debug2/qdrant/search", 	
  		consumes = MediaType.APPLICATION_JSON_VALUE,
  		produces = MediaType.TEXT_PLAIN_VALUE
  		)
  public Mono<String> debugQdrantSearch( 
  		ServerWebExchange ex,
  		@RequestBody QdrantDebugSearchRequest req ) {
  	log.info("debugQdrantSearch");
      
    log.info("Content-Type: {}", ex.getRequest().getHeaders().getContentType());
    log.info("Content-Length: {}", ex.getRequest().getHeaders().getContentLength());
    log.info("Query: {}", req.query);

  	Mono<String> ctxMono = this.designingAiRagService.retrieveContext(req.query, req.topK);
  	return ctxMono;
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
    	      .system(promptText2)
    	      .user(msg.trim())
    	      .stream()
    	      .content()
    	      .doOnSubscribe(s -> log.info("stream started"))
    	      .doOnComplete(() -> log.info("stream completed"))
    	      .doOnError(e -> log.error("error in stream()", e))
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
