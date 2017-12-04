package eu.bcvsolutions.idm.rpt.api.renderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;

/**
 * Render report into xlsx
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractXlsxRenderer extends AbstractReportRenderer {

	public static final String RENDERER_EXTENSION = "xlsx";
	
	@Override
	public MediaType getFormat() {
		return new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	}
	
	@Override
	public String getExtension() {
		return RENDERER_EXTENSION;
	}
	
	/**
	 * Close workbook and get output as input stream
	 * 
	 * @param workbook
	 * @return
	 * @throws IOException
	 */
	protected InputStream getInputStream(XSSFWorkbook workbook) throws IOException {
		//
        // save temp file
    	File temp = getAttachmentManager().createTempFile();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(temp);
            workbook.write(outputStream);
            workbook.close();
            return new FileInputStream(temp);
        } finally {
        	IOUtils.closeQuietly(outputStream);
        }  
	}
}
