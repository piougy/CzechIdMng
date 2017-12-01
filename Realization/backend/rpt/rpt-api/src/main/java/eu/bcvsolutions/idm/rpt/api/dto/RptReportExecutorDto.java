package eu.bcvsolutions.idm.rpt.api.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Report executor (implementation) info
 *
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "reportExecutors")
public class RptReportExecutorDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	//
	private List<RptReportRendererDto> renderers;
	private IdmFormDefinitionDto formDefinition;
	
	public List<RptReportRendererDto> getRenderers() {
		if (renderers == null) {
			renderers = new ArrayList<>();
		}
		return renderers;
	}
	
	public void setRenderers(List<RptReportRendererDto> renderers) {
		this.renderers = renderers;
	}
	
	public void setFormDefinition(IdmFormDefinitionDto formDefinition) {
		this.formDefinition = formDefinition;
	}
	
	public IdmFormDefinitionDto getFormDefinition() {
		return formDefinition;
	}
}
