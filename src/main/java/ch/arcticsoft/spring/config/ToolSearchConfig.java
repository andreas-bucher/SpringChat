package ch.arcticsoft.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springaicommunity.tool.search.ToolSearcher;
// import org.springaicommunity.tool.search.searcher.lucene.LuceneToolSearcher;  // (from tool-searcher-lucene)
import org.springaicommunity.tool.searcher.LuceneToolSearcher;

@Configuration
public class ToolSearchConfig {

    @Bean
    ToolSearcher toolSearcher() {
        // Typical: new LuceneToolSearcher()
        // (use your IDE autocomplete for the exact class/ctor in 2.0.0)
        return new LuceneToolSearcher();
    }
}