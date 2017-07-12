package eu.bcvsolutions.idm.core.api.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XML adapter for CDATA
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class CDATAAdapter extends XmlAdapter<String, String> {

	@Override
	public String unmarshal(String v) throws Exception {
		return v;
	}

	@Override
	public String marshal(String v) throws Exception {
		return "<![CDATA[" + v + "]]>";
	}

}
