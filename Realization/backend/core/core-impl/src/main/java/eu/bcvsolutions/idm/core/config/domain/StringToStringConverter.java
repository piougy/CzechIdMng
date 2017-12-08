package eu.bcvsolutions.idm.core.config.domain;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

/**
 * Converter for resolve problem with 0x00 character in Postgres. Column with text type cannot contains (in Postgres DB) null as "/0x00"
 * This converter solve this problem with replace 0x00 with "".
 * 
 * @author svandav
 *
 */
public class StringToStringConverter implements Converter<String, String> {

	@Override
	public String convert(MappingContext<String, String> context) {
		if (context != null && context.getSource() != null && context.getSource() instanceof String) {
			return context.getSource().replaceAll("\u0000", "");
		}
		return context == null ? null : (String) context.getSource();
	}
	
}