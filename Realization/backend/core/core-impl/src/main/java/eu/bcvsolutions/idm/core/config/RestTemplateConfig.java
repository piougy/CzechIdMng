package eu.bcvsolutions.idm.core.config;

import static eu.bcvsolutions.idm.core.api.service.ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX;
import static eu.bcvsolutions.idm.core.api.service.ConfigurationService.PROPERTY_SEPARATOR;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration beans required for rest communication with ReCaptcha services.
 * 
 * @author Filip Mestanek
 */
@Configuration
public class RestTemplateConfig {
	
	private static final Logger LOG = LoggerFactory.getLogger(RestTemplateConfig.class);
	private static final String PROXY_KEY = IDM_PRIVATE_PROPERTY_PREFIX + "core" + PROPERTY_SEPARATOR + "http" + PROPERTY_SEPARATOR + "proxy";
	
	@Autowired private ConfigurationService configuration;
	
	
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory httpRequestFactory) {
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }
    
    /**
     * Returns proxy to use for http requests. If no proxy is set, returns null.
     */
    public Proxy getHttpProxy() {
    	String proxyConfig = configuration.getValue(PROXY_KEY, null);
		if (StringUtils.hasText(proxyConfig)) {
			LOG.debug("Configuring proxy {}", proxyConfig);
			
			String[] split = proxyConfig.split(":");
			if (split.length != 2) throw new ResultCodeException(CoreResultCode.WRONG_PROXY_CONFIG);
			
			String host = split[0];
			int port = Integer.parseInt(split[1]);
		
			return new Proxy(Type.HTTP, new InetSocketAddress(host, port));
		}
		
		return null;
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory(HttpClient httpClient) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

		Proxy proxy = getHttpProxy();
		if (proxy != null) requestFactory.setProxy(proxy);

		return requestFactory;
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClientBuilder.create().build();
    }
}
