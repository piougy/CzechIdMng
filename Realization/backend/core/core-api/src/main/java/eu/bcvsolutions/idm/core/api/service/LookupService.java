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
 * @see DtoLookup
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
	 * Returns base service for given dto
	 * 
	 * @param dtoClass
	 * @param <DTO> {@link BaseDto} type
	 * @return
	 */
	ReadDtoService<?, ?> getDtoService(Class<? extends Identifiable> identifiableType);
	
	/**
	 * If {@link BaseDto} is given as identifiable type, then {@link BaseEntity} type will be found by registered {@link ReadDtoService}. 
	 * 
	 * @param identifiableType
	 * @return
	 */
	Class<? extends BaseEntity> getEntityClass(Class<? extends Identifiable> identifiableType);
}
