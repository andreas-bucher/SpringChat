package ch.arcticsoft.spring.config;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ToolRegistryLogger {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ApplicationContext ctx;

    public ToolRegistryLogger(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logToolsAtStartup() {
        List<ToolEntry> tools = discoverTools();

        if (tools.isEmpty()) {
            log.warn("⚠️ No ChatTools discovered (@Tool)");
            return;
        }

        log.info("✅ Discovered {} ChatTool(s) (@Tool):", tools.size());
        tools.stream()
                .sorted(Comparator.comparing(ToolEntry::toolName))
                .forEach(t -> {
                    log.info("  • {}  [{}]", t.toolName(), t.beanName());
                    log.info("    desc: {}", t.description());
                    log.info("    sig : {}", t.signature());
                });
    }

    private List<ToolEntry> discoverTools() {
        List<ToolEntry> result = new ArrayList<>();

        for (String beanName : ctx.getBeanDefinitionNames()) {
            Object bean = ctx.getBean(beanName);

            // In Spring, proxies can hide annotations on interfaces.
            // We check all public methods and also try the target class methods via getClass().
            for (Method m : bean.getClass().getMethods()) {
                Tool tool = m.getAnnotation(Tool.class);
                if (tool == null) continue;

                result.add(new ToolEntry(
                        beanName,
                        m.getName(),
                        tool.description(),
                        formatSignature(m)
                ));
            }
        }
        return result;
    }

    private static String formatSignature(Method m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getReturnType().getSimpleName()).append(" ");
        sb.append(m.getDeclaringClass().getSimpleName()).append("#").append(m.getName()).append("(");

        Parameter[] params = m.getParameters();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(params[i].getType().getSimpleName());
            if (params[i].getName() != null) {
                sb.append(" ").append(params[i].getName());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private record ToolEntry(String beanName, String toolName, String description, String signature) {}
}