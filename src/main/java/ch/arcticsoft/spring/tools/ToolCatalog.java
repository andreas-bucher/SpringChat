package ch.arcticsoft.spring.tools;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ToolCatalog {
	
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ApplicationContext ctx;

    public ToolCatalog(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    public List<ToolInfo> listAllTools() {
    	log.info("listAllTools");
        List<ToolInfo> tools = new ArrayList<>();

        for (String beanName : ctx.getBeanDefinitionNames()) {
            Object bean = ctx.getBean(beanName);

            for (Method m : bean.getClass().getMethods()) {
                Tool tool = m.getAnnotation(Tool.class);
                if (tool != null) {
                    tools.add(new ToolInfo(
                            m.getName(),
                            tool.description()
                    ));
                }
            }
        }
        return tools;
    }

    public record ToolInfo(String name, String description) {}
}