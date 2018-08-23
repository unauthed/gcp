package gdg.bristol.pubsub;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties("pubsub")
public class RuntimeConfiguration {

	// @NotNull
	private URI proxyEndpoint;

	public URI getProxyEndpoint() {
		return proxyEndpoint;
	}

	public void setProxyEndpoint(URI proxyEndpoint) {
		this.proxyEndpoint = proxyEndpoint;
	}
}