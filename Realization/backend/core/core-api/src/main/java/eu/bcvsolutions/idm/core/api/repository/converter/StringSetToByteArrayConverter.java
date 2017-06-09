package eu.bcvsolutions.idm.core.api.repository.converter;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.SerializationUtils;

/**
 * "Naive" set of strings to byte[] converter
 * 
 * @author Radek Tomiška
 */
@Converter
public class StringSetToByteArrayConverter implements AttributeConverter<Set<String>, byte[]> {

	public static final String SEPARATOR = ",";

	@Override
	public byte[] convertToDatabaseColumn(Set<String> list) {
		return SerializationUtils.serialize((Serializable) list);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> convertToEntityAttribute(byte[] joined) {
		return (Set<String>) SerializationUtils.deserialize(joined);
	}

}