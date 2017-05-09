package eu.bcvsolutions.idm.core.api.rest.lookup;

import org.springframework.core.GenericTypeResolver;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Lookup support
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 */
public abstract class AbstractDtoLookup<DTO extends BaseDto> implements DtoLookup<DTO> {

	private final Class<?> domainType;

	/**
	 * Creates a new {@link AbstractDtoLookup} instance discovering the supported type from the generics signature.
	 */
	public AbstractDtoLookup() {
		this.domainType = GenericTypeResolver.resolveTypeArgument(getClass(), DtoLookup.class);
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