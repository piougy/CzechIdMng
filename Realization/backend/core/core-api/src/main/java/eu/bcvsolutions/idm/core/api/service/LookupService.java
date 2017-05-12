package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.rest.lookup.DtoLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.EntityLookup;

/**
 * Support for loading {@link BaseEntity} by identifier.
 * Provide basic services through application.
 *
 * @see EntityLookup
 * @see ReadDtoService
 * 
 * @author Radek Tomiška
 */
public interface LookupService {

	/**
	 * Returns entity by given identifier and 
	 * 
	 * @param identifiableType
	 * @param entityId
	 * @return
	 */
	BaseEntity lookupEntity(Class<? extends Identifiable> identifiableType, Serializable entityId);
	
	/**
	 * Returns dto by given identifier and 
	 * 
	 * @param identifiableType
	 * @param entityId
	 * @return
	 */
	BaseDto lookupDto(Class<? extends Identifiable> identifiableType, Serializable entityId);
	
	/**
	 * Returns {@link EntityLookup} for given identifiable class
	 * 
	 * @param entityClass
	 * @param <E> {@link Identifiable} type
	 * @return
	 */
	<I extends BaseEntity> EntityLookup<I> getEntityLookup(Class<? extends Identifiable> identifiableType);
	
	/**
	 * Returns {@link DtoLookup} for given identifiable class
	 * 
	 * @param identifiableType
	 * @param <E> {@link Identifiable} type
	 * @return
	 */
	<I extends BaseDto> DtoLookup<I> getDtoLookup(Class<? extends Identifiable> identifiableType);
	
	/**
	 * Returns base service for given entity
	 * 
	 * @param entityClass
	 * @param <E> {@link BaseEntity} type
	 * @return
	 * @deprecated will be removed (dto services usage)
	 */
	@Deprecated
	<E extends BaseEntity> ReadEntityService<E, ?> getEntityService(Class<E> entityClass);

	/**
	 * Returns base service for given dto
	 * 
	 * @param dtoClass
	 * @param <DTO> {@link BaseDto} type
	 * @return
	 */
	ReadDtoService<?, ?> getDtoService(Class<? extends Identifiable> identifiableType);

	/**
	 * Returns base service for given entity in given type
	 * 
	 * @param entityClass
	 * @param entityServiceClass
	 * @param <E> {@link BaseEntity} type
	 * @param <S> {@link ReadEntityService} type
	 * @return
	 * @deprecated will be removed (dto services usage)
	 */
	@Deprecated
	<E extends BaseEntity, S extends ReadEntityService<E, ?>> S getEntityService(Class<E> entityClass, Class<S> entityServiceClass);
}
