package eu.bcvsolutions.idm.core.rest.domain;

import java.util.Collection;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author svandav
 *
 * @param <T>
 *            DTO type
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourcesWrapper<T> extends ResourceSupport {
	
	@JsonProperty("_embedded")
	private final EmbeddedsWrapper<T> embedded;
	private ResourcePage page;

	public ResourcesWrapper(Collection<T> resources) {
		this.embedded = new EmbeddedsWrapper<T>(resources);
		this.page = new ResourcePage(resources.size(), resources.size(), 1, 0);
	}

	public ResourcesWrapper(Collection<T> resources, long recordsTotal) {
		this.embedded = new EmbeddedsWrapper<T>(resources);
		this.page = new ResourcePage(resources.size(), recordsTotal, 1, 0);
	}
	
	public ResourcesWrapper(Collection<T> resources, long totalElements, long totalPages, long pageNumber, long pageSize) {
		this.embedded = new EmbeddedsWrapper<T>(resources);
		this.page = new ResourcePage(pageSize, totalElements, totalPages, pageNumber);
	}

	public ResourcePage getPage() {
		return page;
	}

	public void setPage(ResourcePage page) {
		this.page = page;
	}

	@JsonIgnore
	public Collection<T> getResources() {
		return embedded.getResources();
	}


}
