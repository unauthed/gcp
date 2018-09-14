package gdg.bristol.pubsub;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;

import gdg.bristol.pubsub.Application.PubSubOutboundGateway;
import io.micrometer.core.annotation.Timed;

@RefreshScope
@RestController
public class Controller {

	private static final Logger log = LoggerFactory.getLogger(Controller.class);

	private final RuntimeConfiguration runtimeConfiguration;

	private final RestTemplate restTemplate;

	private final PubSubOutboundGateway outboundGateway;

	public Controller(RuntimeConfiguration runtimeConfiguration, RestTemplate restTemplate,
			PubSubOutboundGateway outboundGateway) {

		this.runtimeConfiguration = runtimeConfiguration;
		this.restTemplate = restTemplate;
		this.outboundGateway = outboundGateway;
	}

	@GetMapping("/listen/**")
	public @ResponseBody ResponseEntity<String> listen(HttpServletRequest request) {

		String message = request.getRequestURI();
		message = message.substring("/listen".length());

		if (StringUtils.isEmpty(message)) {
			final String warning = String.format("Warning empty message from GET /listen request.");
			log.warn(warning);
			return ResponseEntity.badRequest().body(warning);
		}

		if (!StringUtils.isEmpty(request.getQueryString())) {
			message += "?" + request.getQueryString();
		}

		log.info("Publishing message from GET /listen request '{}'.", message);
		outboundGateway.sendToPubSub(message);
		return ResponseEntity.ok(String.format("Published message from GET /listen request '%s'.", message));
	}

	@GetMapping("/publishMessage/{message}")
	public @ResponseBody ResponseEntity<String> publishMessageWithGet(@PathVariable("message") String message) {

		log.info("Publishing message from GET request '{}'.", message);
		outboundGateway.sendToPubSub(message);
		return ResponseEntity.ok("Published message from GET request.");
	}

	@PostMapping("/publishMessage")
	public @ResponseBody ResponseEntity<String> publishMessageWithPost(@RequestParam("message") String message) {

		log.info("Publishing message from POST request '{}'.", message);
		outboundGateway.sendToPubSub(message);
		return ResponseEntity.ok("Published message from POST request.");
	}

	@Timed(value = "post.proxyMessage.requests", histogram = true, percentiles = { 0.95, 0.99 }, extraTags = {
			"version", "v1" })
	@PostMapping("/proxyMessage")
	public ResponseEntity<String> proxyMessage(@RequestParam("message") String message) {

		log.debug("Proxying message '{}' to '{}'.", message, runtimeConfiguration.getProxyEndpoint());
		Map<String, String> params = new HashMap<String, String>();
		params.put("message", message);

		try {
			return restTemplate.postForEntity(runtimeConfiguration.getProxyEndpoint(), params, String.class);
		} catch (Exception e) {
			log.warn("Proxing message '{}' returned with error: '{}'.", message, e.getMessage());
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/saveMessage")
	public ResponseEntity<String> saveMessage(@RequestParam("message") String message) {

		Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		KeyFactory keyFactory = datastore.newKeyFactory().setKind("messages");
		IncompleteKey key = keyFactory.setKind("messages").newKey();

		FullEntity<IncompleteKey> dataEntity = FullEntity.newBuilder(key).set("message", message)
				.set("timestamp", Timestamp.now()).build();
		datastore.add(dataEntity);

		// retrieve the last 10 messages from the datastore, ordered by timestamp.
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("messages")
				.setOrderBy(StructuredQuery.OrderBy.desc("timestamp")).setLimit(10).build();
		QueryResults<Entity> results = datastore.run(query);

		StringBuffer body = new StringBuffer();
		while (results.hasNext()) {
			Entity entity = results.next();
			String line = String.format("time: %s message: %s\n", entity.getTimestamp("timestamp"),
					entity.getString("message"));
			body.append(line);

		}

		return ResponseEntity.ok(body.toString());
	}

}
