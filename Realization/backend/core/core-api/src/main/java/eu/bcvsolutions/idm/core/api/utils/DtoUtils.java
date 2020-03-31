package eu.bcvsolutions.idm.core.api.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
		Assert.notNull(dto, "DTO is required.");
		Assert.notNull(dto.getEmbedded(), "DTO does not have embedded DTO map initialized and is required.");
		Assert.hasLength(attributeName, "Singular attribute is required to get embedded DTO.");
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
		Assert.notNull(attribute, "Singular attribute is required to get DTO from embedded.");
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
		Assert.notNull(attribute, "Singular attribute is required to get DTO from embedded.");
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
		Assert.notNull(attribute, "Singular attribute is required to get DTO from embedded.");
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
		Assert.notNull(attribute, "Singular attribute is required to get DTO from embedded.");
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
	 * Sets target audit fields by given source. 
	 * Id and realm id is not copied.
	 * 
	 * @param auditableSource entity or dto
	 * @param auditableTarget entity or dto
	 * @see #setAuditable(Auditable, Auditable)
	 * @since 10.2.0
	 */
	public static void copyAuditFields(Auditable source, Auditable target) {
		EntityUtils.copyAuditFields(source, target);
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
	
	/**
	 * Util to solve legacy issues with joda (old) vs. java time (new) usage.
	 * 
	 * @param dateTime
	 * @return
	 */
	public static ZonedDateTime toZonedDateTime(Object dateTime) {
		if (dateTime == null) {
			return null;
		}
		if (dateTime instanceof org.joda.time.DateTime) {
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(((org.joda.time.DateTime) dateTime).getMillis()), ZoneId.systemDefault());
		}
		return (ZonedDateTime) dateTime;
	}
	
	/**
	 * Util to solve legacy issues with joda (old) vs. java time (new) usage.
	 * 
	 * @param localDate as java time
	 * @return
	 */
	public static LocalDate toLocalDate(Object localDate) {
		if (localDate == null) {
			return null;
		}
		if (localDate instanceof org.joda.time.LocalDate) {
			org.joda.time.LocalDate joda = (org.joda.time.LocalDate) localDate;
			return LocalDate.of(joda.getYear(), joda.getMonthOfYear(), joda.getDayOfMonth());
		}
		return (LocalDate) localDate;
	}
	
}
