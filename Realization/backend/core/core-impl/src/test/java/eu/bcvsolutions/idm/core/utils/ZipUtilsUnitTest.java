package eu.bcvsolutions.idm.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import eu.bcvsolutions.idm.core.api.utils.ZipUtils;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Compress / extract test.
 *
 * @author Radek Tomiška
 *
 */
public class ZipUtilsUnitTest extends AbstractUnitTest {

	@Test
	public void testCompressAndExtract() throws Exception {
		File source = new File(this.getClass().getClassLoader().getResource("eu/bcvsolutions/idm/workflow").getFile());
		File destination = new File("target/zip-test");
		if (destination.exists()) {
			FileUtils.deleteDirectory(destination);
		}
		destination.mkdirs();
		// compress directory
		long filesCount = countFiles(source);
		long filesSize = getFileSize(source);
		Assert.assertTrue(filesCount > 0);
		Assert.assertTrue(filesSize > 0);
		File wfZip = new File(destination, "wf.zip");
		ZipUtils.compress(source, wfZip.getPath());
		Assert.assertTrue(wfZip.exists());
		//
		// and extract again => check is the same
		File wfExtracted = new File(destination, "wfExtracted");
		ZipUtils.extract(wfZip, wfExtracted.getPath());
		//
		Assert.assertEquals(filesCount, countFiles(wfExtracted));
		Assert.assertEquals(filesSize, getFileSize(wfExtracted));
	}
	
	private long countFiles(File file) throws IOException {
		return Files
				.walk(Paths.get(file.getPath()))
				.parallel()
				.filter(p -> !p.toFile().isDirectory())
				.count();
	}
	
	private long getFileSize(File file) throws IOException {
		return Files
				.walk(Paths.get(file.getPath()))
				.mapToLong(p -> p.toFile().length())
				.sum();
	}
}
