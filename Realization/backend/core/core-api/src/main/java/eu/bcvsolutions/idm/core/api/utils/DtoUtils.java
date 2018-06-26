package eu.bcvsolutions.idm.core.api.utils;

import java.util.UUID;

import javax.persistence.metamodel.SingularAttribute;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Common dto helpers
 *
 * @author Radek Tomiška <tomiska@ders.cz>
 */
public abstract class DtoUtils {
	
	/**
	 * Returns embedded DTO from given dto. Returns default value, if embedded object is not found or is {@code null}.
	 * 
	 * @param dto
	 * @param attributeName
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <DTO> DTO getEmbedded(AbstractDto dto, String attributeName, DTO defaultValue) {
		Assert.notNull(dto);
		Assert.notNull(dto.getEmbedded());
		Assert.hasLength(attributeName);
		//
		if (!dto.getEmbedded().containsKey(attributeName)) {
    		return defaultValue;
    	}
    	return (DTO) dto.getEmbedded().get(attributeName);
	}
	
	/**
	 * Returns embedded DTO from given dto.
	 * 
	 * @throws IllegalArgumentException if embedded dto not found
	 * @param dto
	 * @param attributeName
	 * @return
	 */
	public static <DTO> DTO getEmbedded(AbstractDto dto, String attributeName) {
		DTO embedded = getEmbedded(dto, attributeName, (DTO) null);
		if (embedded == null) {
			throw new IllegalArgumentException(String.format("Embedded dto with key [%s] not found in given dto [%s]", attributeName, dto));
		}
		return embedded;
	}
	
	/**
	 * Returns embedded DTO from given dto
	 * 
	 * @throws IllegalArgumentException if embedded dto not found
	 * @param dto
	 * @param attribute
	 * @return
	 */
	public static <DTO> DTO getEmbedded(AbstractDto dto, SingularAttribute<?, ?> attribute) {
		Assert.notNull(attribute);
		//
		return getEmbedded(dto, attribute.getName());
	}
	
	/**
	 * Returns embedded DTO from given dto. Returns default value, if embedded object is not found or is {@code null}.
	 * 
	 * @param dto
	 * @param attribute
	 * @param defaultValue
	 * @return
	 */
	public static <DTO> DTO getEmbedded(AbstractDto dto, SingularAttribute<?, ?> attribute, DTO defaultValue) {
		Assert.notNull(attribute);
		//
		return getEmbedded(dto, attribute.getName(), (DTO) defaultValue);
	}
	
	/**
	 * Returns embedded DTO from given dto
	 * 
	 * @throws IllegalArgumentException if embedded dto not found
	 * @param dto 
	 * @param attributeName
	 * @return
	 */
	public static <DTO> DTO getEmbedded(AbstractDto dto, String attributeName, Class<DTO> dtoClass) {
		DTO embedded = getEmbedded(dto, attributeName, dtoClass, null);
		if (embedded == null) {
			throw new IllegalArgumentException(String.format("Embedded dto with key [%s] not found in given dto [%s]", attributeName, dto));
		}
		return embedded;
	}
	
	/**
	 * Returns embedded DTO from given dto. Returns default value, if embedded object is not found or is {@code null}.
	 * 
	 * @param dto 
	 * @param attributeName
	 * @return
	 */
	public static <DTO> DTO getEmbedded(AbstractDto dto, String attributeName, Class<DTO> dtoClass, DTO defaultValue) {
		Assert.notNull(dto);
		Assert.notNull(dto.getEmbedded());
		Assert.hasLength(attributeName);
		//
		return getEmbedded(dto, attributeName, defaultValue);
	}
	
	/**
	 * Returns embedded DTO from given dto
	 * 
	 * @throws IllegalArgumentException if embedded dto not found
	 * @param dto 
	 * @param attributeName
	 * @return
	 */
	public static <DTO> DTO getEmbedded(AbstractDto dto, SingularAttribute<?, ?> attribute, Class<DTO> dtoClass) {
		Assert.notNull(dto);
		Assert.notNull(dto.getEmbedded());
		Assert.notNull(attribute);
		//
		return getEmbedded(dto, attribute.getName(), dtoClass);
	}
	
	/**
	 * Returns embedded DTO from given dto. Returns default value, if embedded object is not found or is {@code null}.
	 * 
	 * @param dto 
	 * @param attributeName
	 * @return
	 */
	public static <DTO> DTO getEmbedded(AbstractDto dto, SingularAttribute<?, ?> attribute, Class<DTO> dtoClass, DTO defaultValue) {
		Assert.notNull(dto);
		Assert.notNull(dto.getEmbedded());
		Assert.notNull(attribute);
		//
		return getEmbedded(dto, attribute.getName(), dtoClass, defaultValue);
	}
	
	/**
	 * Method clear auditable fields. 
	 * 
	 * @param entity or dto
	 */
	public static void clearAuditFields(Auditable auditable) {
		EntityUtils.clearAuditFields(auditable);
	}
	
	/**
	 * Returns {@link UUID} from given {@code string} or {@link UUID}.
	 * 
	 * @param identifier {@code string} or {@link UUID} 
	 * @return
     * @throws IllegalArgumentException If identifier does not conform to the string representation as
     *          described in {@link #toString}
	 */	
	public static UUID toUuid(Object identifier) {
		return EntityUtils.toUuid(identifier);
	}
	
}
