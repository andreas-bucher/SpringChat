package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PageController {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
    @GetMapping("/")
    public String chatPage() {
    	log.info("/ .. render page chat");
        return "chat";
    }
    
    @GetMapping("/debug")
    public String debugPage() {
    	log.info("/debug .. render page chat");
        return "debug";
    } 
    @GetMapping("/test")
    public String test() {
    	log.info("/test .. render page chat");
        return "test";
    } 
    
}