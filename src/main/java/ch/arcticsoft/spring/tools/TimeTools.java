package ch.arcticsoft.spring.tools;

import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class TimeTools {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Tool(
    	name = "nowZurich",
    	description = """
        Returns the current date and time today/now/time in the Europe/Zurich timezone.
        Always includes both date and time in ISO-8601 format.
        """)
    public String nowZurich() {
    	log.debug("nowZurich");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Zurich"));
        String str = """
        {
          "timezone": "Europe/Zurich",
          "date": "%s",
          "time": "%s",
          "iso": "%s"
        }
        """.formatted(
                now.toLocalDate(),
                now.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        log.trace("TimeTools - nowZurich : {}", str);
        return str;
        
    }
    
    
    public Mono<ChatClientRequest> enrichChatClientRequest(ChatClientRequest chatClientRequest) {
		log.debug("enrichedChatlientRequest");
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
        
    	return Mono.just(enrichedChatClientRequest);
    }
}