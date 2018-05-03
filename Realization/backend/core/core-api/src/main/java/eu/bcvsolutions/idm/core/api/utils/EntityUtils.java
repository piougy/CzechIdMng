/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bcvsolutions.idm.core.api.utils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Common entity helpers
 *
 * @author Radek Tomiška <tomiska@ders.cz>
 */
public class EntityUtils {

	private EntityUtils() {
	}

	/**
	 * Returns true, when given entity is valid, false otherwise
	 *
	 * @param entity
	 * @return
	 */
	public static boolean isValid(ValidableEntity entity) {
		return isValid(entity, new LocalDate());
	}
	
	/**
	 * Returns if entity is valid for given date.
	 * 
	 * @param entity
	 * @param targetDate
	 * @return
	 */
	public static boolean isValid(ValidableEntity entity, LocalDate targetDate) {
		Assert.notNull(targetDate);
		//
		if (entity == null) {
			return false;
		}				
		return (entity.getValidFrom() == null || entity.getValidFrom().compareTo(targetDate) <= 0)
				&& (entity.getValidTill() == null || entity.getValidTill().compareTo(targetDate) >= 0);
	}
	
	/**
	 * Returns true, when given dates are currently is valid, false otherwise
	 * 
	 * @param validFrom
	 * @param validTill
	 * @return
	 */
	public static boolean isValid(DateTime validFrom, DateTime validTill) {
		return isValid(validFrom, validTill, new DateTime());
	}
	
	/**
	 * Returns if dates are valid for given date.
	 * 
	 * @param validFrom
	 * @param validTill
	 * @param targetDate
	 * @return
	 */
	public static boolean isValid(DateTime validFrom, DateTime validTill, DateTime targetDate) {
		return (validFrom == null || validFrom.compareTo(targetDate) <= 0)
				&& (validTill == null || validTill.compareTo(targetDate) >= 0);
	}
	
	/**
	 * Returns true, if entity is valid in future, but not now.
	 * 
	 * @param entity
	 * @return
	 */
	public static boolean isValidInFuture(ValidableEntity entity) {
		if (entity == null) {
			return false;
		}		
		LocalDate now = new LocalDate();	
		return entity.getValidFrom() != null && entity.getValidFrom().compareTo(now) > 0
				&& (entity.getValidTill() == null || entity.getValidTill().compareTo(now) > 0);
	}
	
	/**
	 * Returns true, if entity is valid now or in future.
	 * 
	 * @param entity
	 * @return
	 */
	public static boolean isValidNowOrInFuture(ValidableEntity entity) {
		if (entity == null) {
			return false;
		}	
		LocalDate now = new LocalDate();
		return entity.getValidTill() == null || entity.getValidTill().compareTo(now) >= 0;
	}	
	
	/**
	 * Returns false, when validable information are the same
	 * 
	 * @param previous
	 * @param current
	 * @return
	 */
	public static boolean validableChanged(ValidableEntity previous, ValidableEntity current) {
		return !Objects.equals(previous.getValidFrom(), current.getValidFrom()) || !Objects.equals(previous.getValidTill(), current.getValidTill());
	}
	
	/**
	 * Returns module name by entity package (by convention) 
     * <p>
	 * TODO: Does not work for inline classes
	 * 
	 * @param entityClass
	 * @return module identifier
	 */
	public static String getModule(Class<?> entityClass) {
		Assert.notNull(entityClass);
		//
		String name = entityClass.getCanonicalName();
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		String packages[] = name.split("\\.");
		if (packages.length > 3) {
			return packages[3];
		}
		return null;
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
        if(identifier == null) {
        	return null;
        }
    
        try {
            if(identifier instanceof UUID) {
                return (UUID) identifier;
            }
            return UUID.fromString((String) identifier);
        } catch (Exception ex) {
            throw new ClassCastException(String.format("Identified object [%s] is not an UUID.", identifier));
        }
    }
	
	/**
	 * Check if is string convertible to {@link UUID} 
	 * 
	 * @param uuid
	 * @return true if is given string convertible to {@link UUID}
	 */
	public static boolean isUuid(String uuid){
		if( uuid == null){
			return false;
		}
		try {
			UUID.fromString(uuid);
		} catch(IllegalArgumentException ex){
			// Simple is not UUID
			return false;
		}
		return true;
	}

    public static Field getFirstFieldInClassHierarchy(Class<?> sourceType, String field) throws NoSuchFieldException {
        Field result = getFirstFieldInClassHierarchyInternal(sourceType, field);
        if (result == null) {
            throw new NoSuchFieldException(String.format("No field %s found in class %s", field, sourceType));
        }
        return result;
    }

    private static Field getFirstFieldInClassHierarchyInternal(Class<?> sourceType, String field) {
        if (sourceType == null || field == null) {
            return null;
        }
        final Field[] fields = sourceType.getDeclaredFields();
        return Arrays.stream(fields)
                .filter(f -> field.equals(f.getName()))
                .findFirst()
                .orElseGet(() -> getFirstFieldInClassHierarchyInternal(sourceType.getSuperclass(), field));
    }
    
	/**
	 * Return object from entity for given property name
	 * 
	 * @param entity
	 * @param propertyName
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static Object getEntityValue(Object entity, String propertyName) throws 
	IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
			 {
		Optional<PropertyDescriptor> propertyDescriptionOptional = Arrays
				.asList(Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors())
				.stream().filter(propertyDescriptor -> {
					return propertyName.equals(propertyDescriptor.getName());
				}).findFirst();
		if (!propertyDescriptionOptional.isPresent()) {
			throw new IllegalAccessException("Field " + propertyName + " not found!");
		}
		PropertyDescriptor propertyDescriptor = propertyDescriptionOptional.get();

		return propertyDescriptor.getReadMethod().invoke(entity);
	}
	
	/**
	 * Get value from given entity field. 
	 * If is first parameter in write method String and value is not String, then will be value converted to String.
	 * 
	 * @param entity
	 * @param propertyName
	 * @param value
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static Object setEntityValue(Object entity, String propertyName, Object value)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Optional<PropertyDescriptor> propertyDescriptionOptional = Arrays
				.asList(Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors()).stream()
				.filter(propertyDescriptor -> {
					return propertyName.equals(propertyDescriptor.getName());
				}).findFirst(); 
		if (!propertyDescriptionOptional.isPresent()) {
			throw new IllegalAccessException("Field " + propertyName + " not found!");
		}
		PropertyDescriptor propertyDescriptor = propertyDescriptionOptional.get();
		Class<?> parameterClass = propertyDescriptor.getWriteMethod().getParameterTypes()[0];
		if (value != null && String.class.equals(parameterClass) && !(value instanceof String)) {
			value = String.valueOf(value);
		}
		if (value != null && !parameterClass.isAssignableFrom(value.getClass()) && !(value.getClass().isPrimitive() || parameterClass.isPrimitive())) {
			throw new IllegalAccessException(
					MessageFormat.format("Wrong type of value [{0}]. Value must be instance of [{1}] type, but has type [{2}]!",
							value, parameterClass, value.getClass()));
		}
		return propertyDescriptor.getWriteMethod().invoke(entity, value);
	}

	/**
	 * Method clear audit fields in entity. Entity must not be null.
	 * 
	 * @param entity or dto
	 */
	public static void clearAuditFields(Auditable auditable) {
		Asserts.notNull(auditable, "Entity must be not null!");
		//
		auditable.setCreated(null);
		auditable.setCreator(null);
		auditable.setCreatorId(null);
		auditable.setModified(null);
		auditable.setModifier(null);
		auditable.setModifierId(null);
		auditable.setOriginalCreator(null);
		auditable.setOriginalCreatorId(null);
		auditable.setOriginalModifier(null);
		auditable.setOriginalModifierId(null);
		auditable.setTransactionId(null);
	}
}
