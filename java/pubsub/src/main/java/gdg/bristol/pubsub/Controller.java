package gdg.bristol.pubsub;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

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

	@PostMapping("/publishMessage")
	public RedirectView publishMessage(@RequestParam("message") String message) {

		log.info("Publishing message '{}'.", message);
		outboundGateway.sendToPubSub(message);
		return new RedirectView("/");
	}

	@Timed(value = "post.proxyMessage.requests", histogram = true, percentiles = { 0.95, 0.99 }, extraTags = {
			"version", "v1" })
	@PostMapping("/proxyMessage")
	public ResponseEntity<String> proxyMessage(@RequestParam("message") String message) {

		log.debug("Proxy message '{}' to '{}'.", message, runtimeConfiguration.getProxyEndpoint());
		Map<String, String> params = new HashMap<String, String>();
		params.put("message", message);

		try {
			return restTemplate.postForEntity(runtimeConfiguration.getProxyEndpoint(), params, String.class);
		} catch (Exception e) {
			log.warn("Posting message '{}' returned '{}'.", message, e.getMessage());
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
