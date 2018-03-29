package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Control image and create thumbnail
 * 
 * @author Petr Hanák
 *
 */
@Component
@Description("Control image and create thumbnail")
public class IdentityImageProcessor {
	
	public IdentityImageProcessor () {
		
	}
	
	public boolean verifyImage(MultipartFile data) throws IllegalStateException, IOException {
		File file = convertData(data);
		return isImage(file);
	}
	
	public BufferedImage processImage(MultipartFile data) throws IOException {
		File file = multipartToFile(data);
		int newWidth = 100;
		BufferedImage source = ImageIO.read(file);
		double ratio = (double)source.getWidth() / newWidth;
//		BufferedImage scaled = scale(source, ratio);
//		saveImage(scaled, file, "png");
		System.out.println("Source width: " + source.getWidth());
		System.out.println("Source height: " + source.getHeight());
		BufferedImage scaled = resizeImage(source);
		System.out.println("Scaled width: " + scaled.getWidth());
		System.out.println("Scaled height: " + scaled.getHeight());
		return scaled;
	}
	
	private static File convertData(MultipartFile data) throws IOException {
		File convFile = new File(data.getOriginalFilename());
	    convFile.createNewFile(); 
	    FileOutputStream fos = new FileOutputStream(convFile); 
	    fos.write(data.getBytes());
	    fos.close();
	    return convFile;
	}
	
	public File multipartToFile(MultipartFile data) throws IllegalStateException, IOException {
	    File convFile = new File(data.getOriginalFilename());
	    data.transferTo(convFile);
	    return convFile;
	}
	
	public boolean isImage(File file) {
		String mimetype = new MimetypesFileTypeMap().getContentType(file);
		String type = mimetype.split("/")[0];
		if(type.equals("image")) {
//			System.out.println("It's an image");
			return true;
		}
		else {
//			System.out.println("It's NOT an image");
			return false;
		}
	}

	private BufferedImage resizeImage(BufferedImage originalBufferedImage) {
		int thumbnailWidth = 300;
		int widthToScale, heightToScale, transferX, transferY;
		if (originalBufferedImage.getHeight() >= originalBufferedImage.getWidth()) {		 
		    widthToScale = (int)(1 * thumbnailWidth);
		    heightToScale = (int)((widthToScale * 1.0) / originalBufferedImage.getWidth() 
		                    * originalBufferedImage.getHeight());
		} else {
		    heightToScale = (int)(1 * thumbnailWidth);
		    widthToScale = (int)((heightToScale * 1.0) / originalBufferedImage.getHeight() 
		                    * originalBufferedImage.getWidth());
		}
		BufferedImage resizedImage = new BufferedImage(thumbnailWidth, 
				thumbnailWidth, originalBufferedImage.getType());
		Graphics2D g = resizedImage.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if(heightToScale > widthToScale) {
			transferX = 0;
			transferY = (thumbnailWidth - heightToScale) / 2;
		} else if(heightToScale < widthToScale) {
			transferX = (thumbnailWidth - widthToScale) / 2;
			transferY = 0;
		} else {
			transferX = 0;
			transferY = 0;
		}
		g.drawImage(originalBufferedImage, transferX, transferY, widthToScale, heightToScale, null);
		g.dispose();
		return resizedImage;
	}
	
	public InputStream imageToInputStream(BufferedImage image) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(image,"png", os);
		InputStream fis = new ByteArrayInputStream(os.toByteArray());
		return fis;
	}
}
