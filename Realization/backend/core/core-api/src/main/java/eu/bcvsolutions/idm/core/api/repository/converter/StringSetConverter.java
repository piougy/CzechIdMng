package eu.bcvsolutions.idm.core.api.repository.converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * "Naive" set to string converter
 * 
 * @author Radek Tomiška
 *
 */
@Converter
public class StringSetConverter implements AttributeConverter<Set<String>, String> {

  public static final String SEPARATOR = ",";
	
  @Override
  public String convertToDatabaseColumn(Set<String> list) {
    return String.join(SEPARATOR, list); 
  }

  @Override
  public Set<String> convertToEntityAttribute(String joined) {
    return new HashSet<>(Arrays.asList(joined.split(SEPARATOR)));
  }

}