package eu.bcvsolutions.idm.rpt.api.dto;

import org.springframework.hateoas.core.Relation;
import org.springframework.http.MediaType;

import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;

/**
 * Report renderer information
 * 
 * @author Radek Tomiška
 *
 * @see MediaType
 */
@Relation(collectionRelation = "reportRenderers")
public class RptReportRendererDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	//
	private MediaType format;
	private String extension;
	
	public void setFormat(MediaType format) {
		this.format = format;
	}
	
	public MediaType getFormat() {
		return format;
	}	
	
	public String getExtension() {
		return extension;
	}
	
	public void setExtension(String extension) {
		this.extension = extension;
	}
}
