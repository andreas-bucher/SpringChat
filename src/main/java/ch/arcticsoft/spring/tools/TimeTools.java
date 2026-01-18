package ch.arcticsoft.spring.tools;

import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class TimeTools {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Tool(description = """
        Returns the current date and time in the Europe/Zurich timezone.
        Always includes both date and time in ISO-8601 format.
        """)
    public String nowZurich() {
    	log.info("TimeTools - nowZurich");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Zurich"));
        return """
        {
          "timezone": "Europe/Zurich",
          "date": "%s",
          "time": "%s",
          "iso": "%s"
        }
        """.formatted(
                now.toLocalDate(),
                now.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
    }
}