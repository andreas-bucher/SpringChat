package ch.arcticsoft.spring.embed;

import io.qdrant.client.QdrantClient;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatVectorStoresConfig {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    /**
     * Second VectorStore pointing to collection: designing_ai_products_and_services_2
     * Reuses the same QdrantClient + EmbeddingModel that Spring Boot auto-configures.
     */ 
    @Bean("designingAiVectorStore")
    public VectorStore designingAiVectorStore(QdrantClient qdrantClient,
                                              EmbeddingModel embeddingModel) {
    	log.debug("designingAiVectorStore");
        return QdrantVectorStore.builder(qdrantClient, embeddingModel)
                .collectionName("designing_ai_products_and_services_2")
                .initializeSchema(true)   // set false if you don’t want auto-create
                .build();
    }
    
    @Bean("certificatesVectorStore")
    public VectorStore certificatesVectorStore(QdrantClient qdrantClient,
                                              EmbeddingModel embeddingModel) {
    	log.debug("designingAiVectorStore");
        return QdrantVectorStore.builder(qdrantClient, embeddingModel)
                .collectionName("certificates")
                .initializeSchema(true)   // set false if you don’t want auto-create
                .build();
    }
    
    
}