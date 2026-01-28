package ch.arcticsoft.spring.embed;


/**
@Configuration
public class EmbeddingConfig {

    @Bean
    EmbeddingModel embeddingModel(OllamaApi ollamaApi) {
        return new OllamaEmbeddingModel(
        				ollamaApi,
        				OllamaEmbeddingOptions.builder()
        					.model("bge-m3")
        					.build(),
        				null,
        				null
        );
    }
}

*/