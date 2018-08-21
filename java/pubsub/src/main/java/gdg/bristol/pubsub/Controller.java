package gdg.bristol.pubsub;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import gdg.bristol.pubsub.Application.PubSubOutboundGateway;
import io.micrometer.core.annotation.Timed;

@RestController
public class Controller {

	private static final Logger log = LoggerFactory.getLogger(Controller.class);

	private final URI proxyEndpoint;

	private final RestTemplate restTemplate;

	private final PubSubOutboundGateway outboundGateway;

	public Controller(@Value("${gdg.proxy.url}") URI proxyEndpoint, RestTemplate restTemplate,
			PubSubOutboundGateway outboundGateway) {

		this.proxyEndpoint = proxyEndpoint;
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
	public String proxyMessage(@RequestParam("message") String message) {

		Map<String, String> params = new HashMap<String, String>();
		params.put("message", message);

		log.info("Posting message '{}' to '{}'.", message, proxyEndpoint);
		return restTemplate.postForObject(proxyEndpoint, params, String.class);
	}
}
