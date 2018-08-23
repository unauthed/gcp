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

		log.debug("Posting message '{}' to '{}'.", message, runtimeConfiguration.getProxyEndpoint());
		Map<String, String> params = new HashMap<String, String>();
		params.put("message", message);

		try {
			return restTemplate.postForEntity(runtimeConfiguration.getProxyEndpoint(), params, String.class);
		} catch (Exception e) {
			log.warn("Posting message '{}' returned '{}'.", message, e.getMessage());
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
