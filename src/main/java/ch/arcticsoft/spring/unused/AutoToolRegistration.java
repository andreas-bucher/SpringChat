package ch.arcticsoft.spring.unused;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class AutoToolRegistration {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
  private static final String TOOLS_PKG = "ch.arcticsoft.spring.tools.";

  private final ApplicationContext ctx;
  
  public AutoToolRegistration(ApplicationContext ctx) {
	  log.debug("AutoToolRegistration  -  {}", ctx.getApplicationName());
	  this.ctx = ctx;
  }

  @Bean
  public ToolCallbackProvider autoToolCallbackProvider(ListableBeanFactory bf) {

    // 1) Discover candidate beans by TYPE ONLY (no instantiation)
    List<String> toolBeanNames = new ArrayList<>();
    for (String name : bf.getBeanDefinitionNames()) {
      Class<?> type = bf.getType(name, false); // ✅ allowEagerInit=false => no bean creation
      if (type == null) continue;
      if (!type.getName().startsWith(TOOLS_PKG)) continue;
      if (!hasToolMethod(type)) continue;
      toolBeanNames.add(name);
    }
    log.info("AutoToolRegistration: found {} tool beans in {}*", toolBeanNames.size(), TOOLS_PKG);

    // 2) Return provider that creates callbacks lazily
    return () -> {
      Object[] toolBeans = toolBeanNames.stream()
          .map(n -> ((BeanFactory) bf).getBean(n)) // ✅ instantiate ONLY tool beans, when needed
          .toArray();
      ToolCallback[] callbacks = MethodToolCallbackProvider.builder()
          .toolObjects(toolBeans)
          .build()
          .getToolCallbacks();
      log.debug("AutoToolRegistration: providing {} tool callbacks", callbacks.length);
      return callbacks;
    };
  }
  

  private static boolean hasToolMethod(Class<?> clazz) {
    if (clazz == null) return false;
    for (Method m : clazz.getMethods()) {
      if (m.isAnnotationPresent(Tool.class)) {
        return true;
      }
    }
    return false;
  }
}