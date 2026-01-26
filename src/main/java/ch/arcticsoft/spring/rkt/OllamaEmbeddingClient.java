package ch.arcticsoft.spring.rkt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class OllamaEmbeddingClient {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
    private final WebClient webClient;
    private final String ollamaBaseUrl = "http://localhost:11434";

    public OllamaEmbeddingClient() {
    	log.debug("OllamaEmbeddingClient");
        this.webClient = WebClient.builder()
                .baseUrl(ollamaBaseUrl)
                .build();
    }

    /**
     * Creates a dense embedding for the given text using Ollama + bge-m3.
     */
    public float[] embed(String text) {
    	log.debug("embed ...");
        OllamaEmbedRequest request = new OllamaEmbedRequest(
                "bge-m3",
                text
        );

        OllamaEmbedResponse response = webClient.post()
                .uri("/api/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaEmbedResponse.class)
                .block();

        if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
            throw new IllegalStateException("No embedding returned from Ollama");
        }

        // Ollama returns List<List<Double>>
        List<Double> vector = response.embeddings().get(0);

        float[] embedding = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            embedding[i] = vector.get(i).floatValue();
        }

        return embedding;
    }

    /* =========================
       Request / Response DTOs
       ========================= */

    record OllamaEmbedRequest(
            String model,
            Object input
    ) {}

    record OllamaEmbedResponse(
            List<List<Double>> embeddings
    ) {}
    
    /**
    public static void main(String[] args) {

        OllamaEmbeddingClient client =
                new OllamaEmbeddingClient("http://localhost:11434");

        float[] embedding = client.embed(
                "Returns the current date and time in the Europe/Zurich timezone"
        );

        System.out.println("Embedding size: " + embedding.length);
    }*/
    
}