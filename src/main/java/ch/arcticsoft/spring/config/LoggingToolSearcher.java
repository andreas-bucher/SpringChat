package ch.arcticsoft.spring.config;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.search.SearchType;
import org.springaicommunity.tool.search.ToolReference;
import org.springaicommunity.tool.search.ToolSearchRequest;
import org.springaicommunity.tool.search.ToolSearchResponse;
import org.springaicommunity.tool.search.ToolSearcher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class LoggingToolSearcher implements ToolSearcher {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ConcurrentHashMap<String, AtomicInteger> indexed = new ConcurrentHashMap<>();
    private final ToolSearcher delegate;

    public LoggingToolSearcher(ObjectProvider<ToolSearcher> provider) {
        this.delegate = provider.orderedStream()
                .filter(ts -> ts.getClass() != LoggingToolSearcher.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No delegate ToolSearcher found (expected LuceneToolSearcher)"
                ));
        log.info("🔎 ToolSearcher delegate: {}", delegate.getClass().getName());
    }

    @Override
    public void clearIndex(String sessionId) {
        log.debug("🔎 clearIndex(sessionId={})", sessionId);
        delegate.clearIndex(sessionId);
    }

    @Override
    public void indexTool(String sessionId, ToolReference toolReference) {
    	  indexed.computeIfAbsent(sessionId, k -> new AtomicInteger()).incrementAndGet();
    	  log.debug("🔎 indexTool(sessionId={}, count={})", sessionId, indexed.get(sessionId).get());
    	  delegate.indexTool(sessionId, toolReference);
    	}


    @Override
    public ToolSearchResponse search(ToolSearchRequest req) {
        // Only call accessors you already confirmed exist in your IDE
    	int count = indexed.getOrDefault(req.sessionId(), new AtomicInteger(0)).get();
    	log.info("🔎 tool-search request: sessionId={}, indexedTools={}", req.sessionId(), count);
        // If you still want more detail, this is always safe:
        log.trace("🔎 tool-search request (full): {}", req);
        ToolSearchResponse resp = delegate.search(req);
        if (resp == null || resp.toolReferences() == null) {
            log.warn("🔎 tool-search response: null / empty");
            return resp;
        }
        log.info("🔎 tool-search hits: {}", resp.toolReferences().size());
        resp.toolReferences().forEach(tr -> log.info("   • {}", tr));
        return resp;
    }

    @Override
    public SearchType searchType() {
        return delegate.searchType();
    }
}