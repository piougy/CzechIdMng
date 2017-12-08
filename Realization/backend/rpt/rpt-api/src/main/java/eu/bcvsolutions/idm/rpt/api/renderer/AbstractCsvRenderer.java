package eu.bcvsolutions.idm.rpt.api.renderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.springframework.http.MediaType;

import com.opencsv.CSVWriter;

import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;

/**
 * Render report into csv
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractCsvRenderer extends AbstractReportRenderer {
	
	public static final String RENDERER_EXTENSION = "csv";
	
	@Override
	public MediaType getFormat() {
		return new MediaType("text", getExtension());
	}
	
	@Override
	public String getExtension() {
		return RENDERER_EXTENSION;
	}
	
	protected CSVWriter createCSVWriter(File file) throws IOException {
		//  BOM encoding to csv file 
		FileOutputStream os = new FileOutputStream(file);
		os.write(0xef);
		os.write(0xbb);
		os.write(0xbf);
		//
		return new CSVWriter(new OutputStreamWriter(os, AttachableEntity.DEFAULT_CHARSET));
	}
}
