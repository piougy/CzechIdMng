package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.metamodel.SingularAttribute;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.rest.lookup.DtoLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.EntityLookup;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Support for loading {@link BaseDto} and {@link BaseEntity} by identifier.
 * Provide basic services through application.
 *
 * @see EntityLookup
 * @see DtoLookup
 * @see ReadDtoService
 * 
 * @author Radek Tomiška
 */
public interface LookupService extends ScriptEnabled {

	/**
	 * Returns {@link BaseEntity} by given identifier and type. 
	 * {@link BaseEntity} and {@link BaseDto} type can be given.
	 * 
	 * @param identifiableType
	 * @param entityId
	 * @return {@link BaseEntity}
	 */
	<E extends BaseEntity> E lookupEntity(Class<? extends Identifiable> identifiableType, Serializable entityId);
	
	/**
	 * Returns {@link BaseDto} by given identifier and type. 
	 * {@link BaseEntity} and {@link BaseDto} type can be given. 
	 * 
	 * @param identifiableType
	 * @param entityId
	 * @return {@link BaseDto}
	 * @throws IllegalArgumentException if service for load dto not found
	 */
	<DTO extends BaseDto> DTO lookupDto(Class<? extends Identifiable> identifiableType, Serializable entityId);
	
	/**
	 * Returns embedded DTO from given dto. If DTO is not found in embedded, then DTO will be load by lookup.
	 * 
	 * @param <DTO> result dto type
	 * @param dto source dto
	 * @param attribute attribute to get
	 * @return dto
	 * @see DtoUtils#getEmbedded(AbstractDto, SingularAttribute)
	 * @since 10.2.0
	 */
	<DTO extends BaseDto> DTO lookupEmbeddedDto(AbstractDto dto, SingularAttribute<?, ?> attribute);
	
	/**
	 * Returns embedded DTO from given dto. If DTO is not found in embedded, then DTO will be load by lookup.
	 * 
	 * @param <DTO> result dto type
	 * @param dto source dto
	 * @param attributeName attribute to get
	 * @return dto
	 * @see DtoUtils#getEmbedded(AbstractDto, String)
	 * @since 10.2.0
	 */
	<DTO extends BaseDto> DTO lookupEmbeddedDto(AbstractDto dto, String attributeName) ;
	
	/**
	 * Returns {@link BaseDto} by given identifier and type. 
	 * {@link BaseEntity} and {@link BaseDto} type can be given. 
	 * 
	 * @param identifiableType cannonical class name
	 * @param entityId
	 * @return {@link BaseDto}
	 * @throws IllegalArgumentExceptionif service for load dto not found or identifiableType is not valid
	 * @since 8.1.7
	 */
	<DTO extends BaseDto> DTO lookupDto(String identifiableType, Serializable entityId);
	
	/**
	 * Returns {@link EntityLookup} for given identifiable class.
	 * 
	 * @param entityClass
	 * @param <E> {@link Identifiable} type
	 * @return
	 */
	<I extends BaseEntity> EntityLookup<I> getEntityLookup(Class<? extends Identifiable> identifiableType);
	
	/**
	 * Returns {@link DtoLookup} for given identifiable class.
	 * 
	 * @param identifiableType
	 * @param <E> {@link Identifiable} type
	 * @return
	 */
	<I extends BaseDto> DtoLookup<I> getDtoLookup(Class<? extends Identifiable> identifiableType);

	/**
	 * Returns base service for given dto.
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
	
	/**
	 * If {@link BaseDto} instance is given as identifiable type, then {@link BaseEntity} type will be found by registered {@link ReadDtoService}.
	 * Returns simple string representing owner class (e.g. canonical name by default).
	 * 
	 * @param owner
	 * @return
	 * @since 9.0.0
	 */
	String getOwnerType(Identifiable owner);
	
	/**
	 * If {@link BaseDto} class is given as identifiable type, then {@link BaseEntity} type will be found by registered {@link ReadDtoService}.
	 * Returns simple string representing owner class (e.g. canonical name by default).
	 * 
	 * @param ownerType
	 * @return
	 * @since 9.0.0
	 */
	String getOwnerType(Class<? extends Identifiable> ownerType);
	
	/**
	 * {@link UUID} identifier from given owner. Only owners with {@link UUID} can own something (e.g. attachments, tokens ...).
	 * 
	 * @param owner
	 * @return
	 * @since 9.0.0
	 */
	UUID getOwnerId(Identifiable owner);
}
