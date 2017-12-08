package eu.bcvsolutions.idm.rpt.renderer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportRenderException;
import eu.bcvsolutions.idm.rpt.api.renderer.AbstractXlsxRenderer;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityRoleDto;

/**
 * Renders given data to xls
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description(AbstractXlsxRenderer.RENDERER_EXTENSION)
public class IdentityRoleXlsxRenderer extends AbstractXlsxRenderer {

	public static final String RENDERER_NAME = "identity-role-xlsx-renderer";
	
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
			XSSFWorkbook workbook = new XSSFWorkbook();
	        XSSFSheet sheet = workbook.createSheet("Report");
	        // header
	        Row row = sheet.createRow(0);
	        Cell cell = row.createCell(0);
	        cell.setCellValue("Username");
	        cell = row.createCell(1);
	        cell.setCellValue("First name");
	        cell = row.createCell(2);
	        cell.setCellValue("Last name");
	        cell = row.createCell(3);
	        cell.setCellValue("Role");
	        cell = row.createCell(4);
	        cell.setCellValue("Valid from");
	        cell = row.createCell(5);
	        cell.setCellValue("Valid till");
	        // data
	        int rowNum = 1;
	        //
	        if (jParser.nextToken() == JsonToken.START_ARRAY) {
	        	while(jParser.nextToken() == JsonToken.START_OBJECT) {
	        		RptIdentityRoleDto identityRole = getMapper().readValue(jParser, RptIdentityRoleDto.class);
		          	IdmIdentityDto identity = identityRole.getIdentity();
		            row = sheet.createRow(rowNum++);
			        cell = row.createCell(0);
			        cell.setCellValue(identity.getUsername());
			        cell = row.createCell(1);
			        cell.setCellValue(identity.getFirstName());
			        cell = row.createCell(2);
			        cell.setCellValue(identity.getLastName());
			        //
			        if (identityRole.getRole() != null) {
			        	// role info
			        	cell = row.createCell(3);
				        cell.setCellValue(identityRole.getRole().getName());
				        cell = row.createCell(4);
				        if (identityRole.getValidTill() != null) {
				        	cell.setCellValue(identityRole.getValidFrom().toString(getConfigurationService().getDateFormat()));
				        }
				        cell = row.createCell(5);
				        if (identityRole.getValidTill() != null) {
				        	cell.setCellValue(identityRole.getValidTill().toString(getConfigurationService().getDateFormat()));  
				        }
			        }
	          	}
	    	}
        	jParser.close();
        	//
	        // close and return input stream
	        return getInputStream(workbook);    
        } catch (IOException ex) {
        	throw new ReportRenderException(report.getName(), ex);
        }
	}
}
