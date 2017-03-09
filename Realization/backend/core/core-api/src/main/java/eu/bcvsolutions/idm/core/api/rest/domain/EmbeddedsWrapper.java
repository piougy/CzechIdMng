package eu.bcvsolutions.idm.core.api.rest.domain;

import java.util.Collection;

/**
 * Wrap object only for add embedded attribute over list of resources
 * @author svandav
 *
 * @param <T>
 * @deprecated use Resource or Resources from spring instead
 */
@Deprecated
public class EmbeddedsWrapper<T> {

	private final Collection<T> resources;

	
	public EmbeddedsWrapper(Collection<T> resources) {
		this.resources = resources;
	}
	
	public Collection<T> getResources() {
		return resources;
	}

}
