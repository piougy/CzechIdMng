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

import javax.imageio.ImageIO;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Control image and create thumb-nail
 * 
 * @author Petr Hanák
 *
 */
@Component
@Description("Verify image suffix and create square thumbnail")
public class ImageUtils {

	public ImageUtils() {
	}

	public boolean verifyImage(MultipartFile data) throws IllegalStateException, IOException {
		File file = convertData(data);
		return isImage(file);
	}

	public BufferedImage processImage(MultipartFile data) throws IOException {
		File file = multipartToFile(data);
		BufferedImage source = ImageIO.read(file);
		// int newWidth = 100;
		// double ratio = (double)source.getWidth() / newWidth;
		// BufferedImage scaled = resizeImage(source);
		// Return scaled for BE resize
		return source;
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
		try {
			BufferedImage image = ImageIO.read(file);
			if (image == null) {
				return false;
			}
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	private BufferedImage resizeImage(BufferedImage originalBufferedImage) {
		int thumbnailWidth = 300;
		int widthToScale, heightToScale, transferX, transferY;
		if (originalBufferedImage.getHeight() >= originalBufferedImage.getWidth()) {
			widthToScale = (int) (thumbnailWidth);
			heightToScale = (int) ((thumbnailWidth * 1.0) / originalBufferedImage.getWidth()
					* originalBufferedImage.getHeight());
		} else {
			heightToScale = (int) (thumbnailWidth);
			widthToScale = (int) ((thumbnailWidth * 1.0) / originalBufferedImage.getHeight()
					* originalBufferedImage.getWidth());
		}
		BufferedImage resizedImage = new BufferedImage(thumbnailWidth, thumbnailWidth, originalBufferedImage.getType());
		Graphics2D graphics = resizedImage.createGraphics();
		graphics.setComposite(AlphaComposite.Src);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (heightToScale > widthToScale) {
			transferX = 0;
			transferY = (thumbnailWidth - heightToScale) / 2;
		} else if (widthToScale > heightToScale) {
			transferX = (thumbnailWidth - widthToScale) / 2;
			transferY = 0;
		} else {
			transferX = 0;
			transferY = 0;
		}
		graphics.drawImage(originalBufferedImage, transferX, transferY, widthToScale, heightToScale, null);
		graphics.dispose();
		return resizedImage;
	}

	public InputStream imageToInputStream(BufferedImage image) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(image, "png", os);
		InputStream fis = new ByteArrayInputStream(os.toByteArray());
		return fis;
	}
}
