package eu.bcvsolutions.idm.core.api.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

/**
 * Naive zip utility to extract / compress directory recursively.
 * Uses default zip compress level.
 *
 * @author Radek Tomiška
 * @since 10.1.0
 */
public class ZipUtils {

	private ZipUtils() {
	}

	/**
	 * Extract zip file.
	 *
	 * @param source zip file
	 * @param destination destination directory
	 * @throws IOException
	 */
	public static void extract(File source, String destination) throws IOException {
		Assert.notNull(source, "Source directory is required.");
		Assert.notNull(destination, "Destination folder is required.");
		//
		ZipFile zipFile = new ZipFile(source);
		try {
		  Enumeration<? extends ZipEntry> entries = zipFile.entries();
		  while (entries.hasMoreElements()) {
		    ZipEntry entry = entries.nextElement();
		    File entryDestination = new File(destination,  entry.getName());
		    if (entry.isDirectory()) {
		        entryDestination.mkdirs();
		    } else {
		        entryDestination.getParentFile().mkdirs();
		        try (InputStream in = zipFile.getInputStream(entry)) {
			        OutputStream out = new FileOutputStream(entryDestination);
			        IOUtils.copy(in, out);
			        out.close();
		        }
		    }
		  }
		} finally {
		  zipFile.close();
		}
	}

	/**
	 * Compress given source file or directory (recursively) to destination file on given path.
	 *
	 * @param source file or folder. If folder is given, then children will be in destination zip file.
	 * @param destinationFilePath zip file path
	 * @throws IOException
	 */
	public static void compress(File source, String destinationFilePath) throws IOException {
		Path zipFilePath = Files.createFile(Paths.get(destinationFilePath));
	    try (ZipOutputStream zipStream = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
	        Path pp = Paths.get(source.getPath());
	        List<Path> children = Files
	        		.walk(pp)
	        		// .filter(path -> !Files.isDirectory(path))
	        		.collect(Collectors.toList());
	        for (Path path : children) {
	        	// add only empty directories
	            if (Files.isDirectory(path)) {
	            	if (path.toFile().list().length == 0) {
	            		zipStream.putNextEntry(new ZipEntry(getRelativePath(pp, path) + "/")); // normal slash is needed => prevent to use back slash in zip entry
	            		zipStream.closeEntry();
	            	}
	            } else {
		        	ZipEntry zipEntry = new ZipEntry(getRelativePath(pp, path));
		        	//
	            	zipStream.putNextEntry(zipEntry);
	            	Files.copy(path, zipStream);
	            	zipStream.closeEntry();
	            }
	        }
	    }
	}
	
	/**
	 * Normal slash is needed => prevent to use back slash in zip entry.
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	private static String getRelativePath(Path parent, Path child) {
	    String relativePath = parent.relativize(child).toString();
	    // Normal slash is needed => prevent to use back slash in zip entry.
	    if (File.separator.equals("\\")) {
	        relativePath = relativePath.replace("\\", "/");
	    }
	    //
	    return relativePath;
	}
}
