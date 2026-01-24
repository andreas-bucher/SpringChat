package ch.arcticsoft.spring;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.SystemMessage;

public class LuceneAdvidor implements Advisor {

  @Override
  public int getOrder() {
	// TODO Auto-generated method stub
	return 0;
  }

  @Override
  public String getName() {
	// TODO Auto-generated method stub
	return null;
  }
/*
  @Override
  public AdvisorContext advise(AdvisorContext ctx) {

    // Grab last user message (typical)
    var lastUser = ctx.prompt().getInstructions().stream()
        .filter(m -> m instanceof UserMessage)
        .map(m -> (UserMessage) m)
        .reduce((a,b) -> b)
        .orElse(null);

    if (lastUser == null) return ctx;

//    var question = lastUser.getContent();
//    if (!RagHeuristics.shouldSearch(question)) return ctx;
//
//    var hits = rag.search(question, 6);
//    if (hits.isEmpty()) return ctx;

    var contextBlock = new StringBuilder();
    contextBlock.append("""
      You may use the following retrieved context.
      - If the context is irrelevant, ignore it.
      - If you use it, cite the source tag like [source: ...].
      
      <context>
      """);

//    for (int i = 0; i < hits.size(); i++) {
//      var h = hits.get(i);
//      contextBlock.append("\n[chunk ").append(i + 1).append("] ")
//          .append("[source: ").append(h.source()).append("]\n")
//          .append(h.text()).append("\n");
//    }
    contextBlock.append("</context>\n");

    // Inject as a SYSTEM message (strong guidance)
    ctx.prompt().getInstructions().addFirst(new SystemMessage(contextBlock.toString()));
    return ctx;
  }
	*/
}
