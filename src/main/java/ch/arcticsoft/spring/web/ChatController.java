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

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class ChatController {
	
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ChatClient chatClientStream;

    public ChatController(@Qualifier("chatClientStream") ChatClient chatClientStream) {
        this.chatClientStream = chatClientStream;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody ch.arcticsoft.spring.ChatRequest request) {
    	log.debug("stream");
        String msg = request == null ? null : request.message();
        if (msg == null || msg.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be empty");
        }

        return chatClientStream.prompt()
        	    .system("""
        	      You have access to tool functions.

        	      IMPORTANT TOOL RULES:
        	      - Before calling any other tool, you MUST call the tool named 'searchTools' to discover relevant tools.
        	      - If the user asks about time/date/now/today, you MUST call searchTools with a query like "current date time Zurich".
        	      - After searchTools returns results, call the most relevant tool.
        	      - If searchTools returns no relevant tools, answer normally without tools.
        	    """)
        	    .user(msg.trim())
        	    .stream()
        	    .content()
        	    .map(chunk -> chunk.startsWith(" ") ? " " + chunk : chunk);
    }
}