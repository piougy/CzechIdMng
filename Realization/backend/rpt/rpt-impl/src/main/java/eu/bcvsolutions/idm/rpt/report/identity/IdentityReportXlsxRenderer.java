package eu.bcvsolutions.idm.rpt.report.identity;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportRenderException;
import eu.bcvsolutions.idm.rpt.api.renderer.AbstractXlsxRenderer;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;

/**
 * Basic identity report xlsx renderer
 * 
 * @author Radek Tomiška
 *
 */
@Component("identityReportRenderer")
@Description(AbstractXlsxRenderer.RENDERER_EXTENSION) // will be show as format for download
public class IdentityReportXlsxRenderer 
		extends AbstractXlsxRenderer 
		implements RendererRegistrar {

	@Override
	public InputStream render(RptReportDto report) {
		try {
			// read json stream
			JsonParser jParser = getMapper().getFactory().createParser(getReportData(report));
			XSSFWorkbook workbook = new XSSFWorkbook();
			CreationHelper createHelper = workbook.getCreationHelper();
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
			cell.setCellValue("Disabled");
			int rowNum = 1;
			//
			// json is array of identities
			if (jParser.nextToken() == JsonToken.START_ARRAY) {
				// write single identity
				while (jParser.nextToken() == JsonToken.START_OBJECT) {
					IdmIdentityDto identity = getMapper().readValue(jParser, IdmIdentityDto.class);
					row = sheet.createRow(rowNum++);
					cell = row.createCell(0);
					cell.setCellValue(identity.getUsername());
					cell = row.createCell(1);
					cell.setCellValue(identity.getFirstName());
					cell = row.createCell(2);
					cell.setCellValue(identity.getLastName());
					cell = row.createCell(3);
					cell.setCellValue(identity.isDisabled());
				}
			}
			// close json stream
			jParser.close();
			//
			// footer info about more available reports
			rowNum++;
			rowNum++;
			row = sheet.createRow(rowNum++);
			cell = row.createCell(0);
			cell.setCellValue("More reports are available in reports module:");
			row = sheet.createRow(rowNum++);
			cell = row.createCell(0);
			cell.setCellValue("https://wiki.czechidm.com/devel/documentation/modules_reports");
			Hyperlink link = createHelper.createHyperlink(XSSFHyperlink.LINK_URL);
            link.setAddress("https://wiki.czechidm.com/devel/documentation/modules_reports");
            cell.setHyperlink(link);
			//
			// close and return input stream
			return getInputStream(workbook);
		} catch (IOException ex) {
			throw new ReportRenderException(report.getName(), ex);
		}
	}

	/**
	 * Register renderer to example report
	 */
	@Override
	public String[] register(String reportName) {
		if (IdentityReportExecutor.REPORT_NAME.equals(reportName)) {
			return new String[] { getName() };
		}
		return new String[] {};
	}

}
