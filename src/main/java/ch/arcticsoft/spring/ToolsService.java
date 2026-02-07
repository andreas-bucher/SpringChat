package ch.arcticsoft.spring;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.tool.search.ToolSearchRequest;
import org.springaicommunity.tool.search.ToolSearchResponse;
import org.springaicommunity.tool.searcher.VectorToolSearcher;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ToolsService {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String sessionId = "default_session";
	private VectorToolSearcher vectorToolSearcher;	
	
	public ToolsService(VectorStore vectorStore) {
		log.debug("ToolsConfig");
		this.vectorToolSearcher = new VectorToolSearcher(vectorStore);
	}
	
	@Bean
	public VectorToolSearcher vectorToolSearcher() {
		log.debug("vectorToolSearcher ...");
		return this.vectorToolSearcher;
	}
	
	public Mono<ToolSearchResponse> toolSearch(String userQuery) {
		log.debug("toolSearch...");
		ToolSearchRequest toolSearchRequest = new ToolSearchRequest(sessionId, userQuery, 5, null);
		return Mono.fromCallable(() -> {
				log.debug("toolSearch .. executing search ..");
				return vectorToolSearcher.search(toolSearchRequest);
			})
			.subscribeOn(Schedulers.boundedElastic());		
	}
	
	
	//@PostConstruct
	public void init(){
		log.debug("init");
		String sessionId = "default_session";
		List<ToolDefinition> toolDefinitions = new ArrayList<ToolDefinition>();
		toolDefinitions.add(this.currentDateTimeTool());
		toolDefinitions.add(this.searchCertificatesTool());
		toolDefinitions.add(this.outline_MIT_AI_course());
		toolDefinitions.add(this.semantic_MIT_AI_course());
		toolDefinitions.add(this.getLatestActivities());
		toolDefinitions.add(this.getLatestActivitiesFromTo());
		toolDefinitions.add(this.loadActivitiesFromWhoop());
		toolDefinitions.add(this.getRecoveryForDate());
		
		for (ToolDefinition toolDef : toolDefinitions){
			log.info("VectorToolSearcher add tool : {}", toolDef.name());
			this.vectorToolSearcher.add(sessionId, UUID.randomUUID().toString(), toolDef.name(), toolDef.description());
		}
	}
	
	
	public ToolDefinition currentDateTimeTool() {
		ToolDefinition toolDefinition = ToolDefinition.builder()
				.name("nowZurich")
				.description("""
				Returns the current date and time today/now/time in the Europe/Zurich timezone.
		        Always includes both date and time in ISO-8601 format.
						""")
				.inputSchema("""
						{}
						""")
				.build();
		return toolDefinition;
	}

	public ToolDefinition searchCertificatesTool() {
		ToolDefinition toolDefinition = ToolDefinition.builder()
				.name("certificates")
				.description("""
				Provide information about certificates and educational programs. Either outline or rag responses.
						""")
				.inputSchema("""
						{}
						""")
				.build();
		return toolDefinition;
	}

	public ToolDefinition outline_MIT_AI_course() {
		ToolDefinition toolDefinition = ToolDefinition.builder()
				.name("outline_MIT_AI_course")
				.description("""
				MIT Online program called Designing and Building AI (Artificial Intelligence) Products and Services.
						""")
				.inputSchema("""
						{}
						""")
				.build();
		return toolDefinition;
	}

	public ToolDefinition semantic_MIT_AI_course() {
		ToolDefinition toolDefinition = ToolDefinition.builder()
				.name("semantic_MIT_AI_course")
				.description("""
				Provide information about MIT Online program called Designing and Building Artificial Intelligence (AI).
						""")
				.inputSchema("""
						{}
						""")
				.build();
		return toolDefinition;
	}
	
	public ToolDefinition getLatestActivities() {
		ToolDefinition toolDefinition = ToolDefinition.builder()
				.name("recentWhoopActivities")
				.description("""
				Get the user's most recent activities (workouts) from the Whoop data store.
						""")
				.inputSchema("""
						{}
						""")
				.build();
		return toolDefinition;
	}
	
	public ToolDefinition getLatestActivitiesFromTo() {
		ToolDefinition toolDefinition = ToolDefinition.builder()
				.name("activitiesBetweenTwoDates")
				.description("""
				Get activities (workouts) between two dates (YYYY-MM-DD).
						""")
				.inputSchema("""
						{}
						""")
				.build();
		return toolDefinition;
	}
	
	public ToolDefinition loadActivitiesFromWhoop() {
		ToolDefinition toolDefinition = ToolDefinition.builder()
				.name("loadActivitiesFromWhoop")
				.description("""
				Load activities (workouts) between two dates (YYYY-MM-DD) from Whoop.
						""")
				.inputSchema("""
						{}
						""")
				.build();
		return toolDefinition;
	}

	public ToolDefinition getRecoveryForDate() {
		ToolDefinition toolDefinition = ToolDefinition.builder()
				.name("whoopRecoveryValues")
				.description("""
				Get recovery score metrics for a user on a specific date (YYYY-MM-DD).
						""")
				.inputSchema("""
						{}
						""")
				.build();
		return toolDefinition;
	}	
}
