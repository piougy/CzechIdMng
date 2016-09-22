package eu.bcvsolutions.idm.core.rest.domain;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.mapping.Associations;

/**
 * Spring data rest Associations extension - we want to assemble embedded object to not exported repositories too.
 * 
 * @author Radek Tomiška
 *
 */
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

		if (metadata != null && isLinkableRepository(metadata, property)) {
			return true;
		}

		metadata = mappings.getMetadataFor(property.getActualType());
		return metadata == null ? false : isLinkableRepository(metadata, null);
	}
	
	/**
	 * We want to assemble embedded object to not exported repositories with excerpt projection too
	 * @param metadata
	 * @return
	 */
	protected boolean isLinkableRepository(ResourceMetadata metadata, PersistentProperty<?> property) {
		if (property != null) {			
			return metadata.isExported(property);
		}
		return metadata.isExported() || metadata.getExcerptProjection() != null;
	}

}
