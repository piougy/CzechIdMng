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
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
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
		if (entity == null) {
			return false;
		}		
		LocalDate now = new LocalDate();		
		return (entity.getValidFrom() == null || entity.getValidFrom().compareTo(now) <= 0)
				&& (entity.getValidTill() == null || entity.getValidTill().compareTo(now) >= 0);
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
	 * @param uuid
	 * @return true if is given string convertible to {@link UUID}
	 */
	public static boolean isUuid(String uuid){
		if(uuid == null){
			return false;
		}
		try{
			UUID.fromString(uuid);
		}catch(IllegalArgumentException ex){
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
		String parameterClass = propertyDescriptor.getWriteMethod().getParameterTypes()[0].getName();
		if(value != null && String.class.getName().equals(parameterClass) && !(value instanceof String)){
			value = String.valueOf(value);
		}
		return propertyDescriptor.getWriteMethod().invoke(entity, value);
	}
}
