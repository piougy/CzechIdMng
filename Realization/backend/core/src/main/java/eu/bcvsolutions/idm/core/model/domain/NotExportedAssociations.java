package eu.bcvsolutions.idm.core.model.domain;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.mapping.Associations;

public class NotExportedAssociations extends Associations {
	
	private final ResourceMappings mappings;
	private final RepositoryRestConfiguration config;

	public NotExportedAssociations(ResourceMappings mappings, RepositoryRestConfiguration config) {
		super(mappings, config);
		this.config = config;
		this.mappings = mappings;
	}
	
	/**
	 * Returns whether the given property is an association that is linkable.
	 * 
	 * @param property can be {@literal null}.
	 * @return
	 */
	public boolean isLinkableAssociation(PersistentProperty<?> property) {

		if (property == null || !property.isAssociation() || config.isLookupType(property.getActualType())) {
			return false;
		}

		ResourceMetadata metadata = mappings.getMetadataFor(property.getOwner().getType());

		metadata = mappings.getMetadataFor(property.getActualType());
		return metadata == null ? false : true;
	}

}
