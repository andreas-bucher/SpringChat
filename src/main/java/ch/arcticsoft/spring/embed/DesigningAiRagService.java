package ch.arcticsoft.spring.embed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DesigningAiRagService {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final VectorStore designingAiVectorStore;

    public DesigningAiRagService(
            @Qualifier("designingAiVectorStore") VectorStore designingAiVectorStore) {
    	log.debug("DesigningAiRagService");
        this.designingAiVectorStore = designingAiVectorStore;
        
        //log.info("make one entry into Qdrant Collection");
        //this.addEntries();
        
    }

    
    public Mono<String> retrieveContext(String userQuery, int topK) {
        return Mono.fromCallable(() -> {
        	log.debug("retrieveContext : {}", userQuery);
        	
        	SearchRequest req = SearchRequest.builder().query(userQuery).topK(topK).build();
        	
            List<Document> docs = designingAiVectorStore.similaritySearch( req );
            
            return docs.stream()
            		.limit(topK)
                    .map(this::formatDoc)
                    .collect(Collectors.joining("\n\n---\n\n"));

        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSubscribe(s -> log.debug("doOnSubscribe - {}", Thread.currentThread().getName()))
        .doOnSuccess(s -> log.debug("doOnSuccess - "))
        .doOnNext(ctx -> log.debug("doOnNext - " ));
    }

    private String formatDoc(Document d) {
        Map<String, Object> m = d.getMetadata();

        Object sourceFile = m.get("source_file");
        Object page = m.get("page");
        
        log.trace("text score({}): {}", d.getScore() ,d.getText());

        return "SOURCE: " + sourceFile
                + " | PAGE: " + page
                + "\n"
                + d.getText();
    }
    
    
    private void addEntries() {
    	
    	List<Document> docs = new ArrayList<Document>();
    	
    	String text = "This is text entry";
    	
    	Map<String, Object> metadata = Map.of(
    		    "source_file", "Module 8_Quick Reference Guide.pdf",
    		    "page", 2,
    		    "embedding_model", "bge-m3"
    		);
    	
    	Document doc = new Document(text, metadata);
    	docs.add(doc);
    	
    	designingAiVectorStore.accept(docs);
    	
    }
    
}