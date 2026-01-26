package ch.arcticsoft.spring.rkt;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class VectorService {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final VectorStore vectorStore;
    private final Random random = new Random();
    private final OllamaEmbeddingClient ollamaEmbeddingClient;
    
    public VectorService(VectorStore vectorStore, OllamaEmbeddingClient ollamaEmbeddingClient) {
    	log.debug("VectorService");
        this.vectorStore = vectorStore;
        log.info("VectorStore implementation = {}", vectorStore.getClass().getName());
        this.ollamaEmbeddingClient = ollamaEmbeddingClient;
    }
    
    @PostConstruct
    public void init() {
    	log.debug("init");
		String sessionId = "default_"+ random.nextInt();
		UUID   toolId    = UUID.randomUUID();
		String toolName  = "Date/Time Tool";
		String toolDescription = """
		        Returns the current date and time today/now/time in the Europe/Zurich timezone.
		        Always includes both date and time in ISO-8601 format.
		        """;
		//float[] embedding = ollamaEmbeddingClient.embed(toolDescription);
		//log.debug("embedding.length: {}", embedding.length);
		Document doc = new Document(
				toolId.toString(),
				toolDescription,
				Map.of("name", toolName));
		List<Document> docs = new ArrayList<Document>();
		docs.add(doc);
		this.addDocs(docs);
    }

    public void addDocs(List<Document> docs) {
        vectorStore.add(docs);
    }

    public List<Document> search(String query) {
        return vectorStore.similaritySearch(query);
    }
}