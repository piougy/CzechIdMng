package eu.bcvsolutions.idm.core.api.rest.lookup;

import org.springframework.core.GenericTypeResolver;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Lookup support by example.
 * 
 * @author Radek Tomiška
 * @param <DTO> dto type
 * @since 10.8.0
 */
public abstract class AbstractDtoLookupByExample<DTO extends BaseDto> implements DtoLookupByExample<DTO> {

	private final Class<?> domainType;

	/**
	 * Creates a new {@link AbstractDtoLookupByExample} instance discovering the supported type from the generics signature.
	 */
	public AbstractDtoLookupByExample() {
		this.domainType = GenericTypeResolver.resolveTypeArgument(getClass(), DtoLookupByExample.class);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return domainType.isAssignableFrom(delimiter);
	}
}