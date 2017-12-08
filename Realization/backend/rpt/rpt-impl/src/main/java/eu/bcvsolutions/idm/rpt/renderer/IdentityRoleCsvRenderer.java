package eu.bcvsolutions.idm.rpt.renderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.Lists;
import com.opencsv.CSVWriter;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportRenderException;
import eu.bcvsolutions.idm.rpt.api.renderer.AbstractCsvRenderer;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityRoleDto;

/**
 * Renders given data to csv
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description(AbstractCsvRenderer.RENDERER_EXTENSION)
public class IdentityRoleCsvRenderer extends AbstractCsvRenderer {

	public static final String RENDERER_NAME = "identity-role-csv-renderer";
	
	@Override
	public String getName() {
		return RENDERER_NAME;
	}
	
	@Override
	public String getModule() {
		return RptModuleDescriptor.MODULE_ID;
	}
	
	@Override
	public InputStream render(RptReportDto report) {
		try {
			JsonParser jParser = getMapper().getFactory().createParser(getReportData(report));
			File temp = getAttachmentManager().createTempFile();
			//
			CSVWriter writer = createCSVWriter(temp);
			//
			// header
			String[] header = new String[]{
			        "Username",
			        "First name",
			        "Last name",
			        "Role",
			        "Valid from",
			        "Valid till"
			        };
			writer.writeNext(header);
			//
	        if (jParser.nextToken() == JsonToken.START_ARRAY) {
	        	while(jParser.nextToken() == JsonToken.START_OBJECT) {
	        		RptIdentityRoleDto reportDto = getMapper().readValue(jParser, RptIdentityRoleDto.class);
		          	IdmIdentityDto identity = reportDto.getIdentity();
		          	//
		          	// row is elementary data
		          	List<String> row = Lists.newArrayList(
		          			identity.getUsername(),
		          			identity.getFirstName(),
		          			identity.getLastName());
		          	if (reportDto.getRole() != null) {			        		
		          		row.add(reportDto.getRole().getName());
		          		row.add(
		          				reportDto.getValidFrom() == null 
				        		? null
				        		: reportDto.getValidFrom().toString(getConfigurationService().getDateFormat()));
		          		row.add(
		          				reportDto.getValidTill() == null 
				        		? null
				        		: reportDto.getValidTill().toString(getConfigurationService().getDateFormat()));	        	
			        }
			        writer.writeNext(row.toArray(new String[row.size()]));       
	          	}
	    	}
	        writer.close();
        	jParser.close();
	        //
            return new FileInputStream(temp);
        } catch (IOException ex) {
        	throw new ReportRenderException(report.getName(), ex);
        }
	}
}
