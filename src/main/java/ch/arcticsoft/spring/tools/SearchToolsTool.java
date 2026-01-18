package ch.arcticsoft.spring.tools;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class SearchToolsTool {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ToolCatalog catalog;

    public SearchToolsTool(ToolCatalog catalog) {
        this.catalog = catalog;
    }

    @Tool(description = """
      Search available tools by intent. ALWAYS call this first before calling any other tool.
      Input: a short natural language query.
      Output: matching tool names with descriptions (top results).
      """)
    public List<ToolCatalog.ToolInfo> searchTools(String query) {
    	log.info("🧰 LLM called searchTools(query='{}')", query);

        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);

        // Minimal deterministic preselection. Replace later with Lucene ToolSearcher.
        return catalog.listAllTools().stream()
                .filter(t ->
                        t.name().toLowerCase(Locale.ROOT).contains(q) ||
                        t.description().toLowerCase(Locale.ROOT).contains(q)
                )
                .limit(10)
                .toList();
    }
}