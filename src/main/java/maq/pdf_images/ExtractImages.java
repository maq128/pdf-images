package maq.pdf_images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class ExtractImages {

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.exit(0);
		}

		File pdfFile = new File(args[0]).getCanonicalFile();
		if (!pdfFile.isFile()) {
			println("文件错误: " + args[0]);
			System.exit(0);
		}

//		dump(pdfFile);
		extract(pdfFile);
	}

	static void extract(File pdfFile) throws IOException {
		String path = pdfFile.getParent();
		String base = pdfFile.getName();
		if (base.toLowerCase().endsWith(".pdf")) {
			base = base.substring(0, base.length() - 4);
		}

		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
		PDDocument document = PDDocument.load(pdfFile);

		for (int p = 0; p < document.getNumberOfPages(); p++) {
			PDPage page = document.getPage(p);
			List<PDImageXObject> images = getImagesFromResources(page.getResources());
			for (int i = 0; i < images.size(); i++) {
				PDImageXObject image = images.get(i);

				// TIKA-3040, PDFBOX-4771: can't save ARGB as JPEG
				String type = "jpg";
				if (image.getMask() != null || image.getSoftMask() != null) {
					type = "png";
				}

				String imgFileName = base + "-" + (p + 1) + "-" + (i + 1) + "." + type;
				println(imgFileName + " (" + image.getWidth() + "x" + image.getHeight() + ")");

				BufferedImage bufImg = image.getImage();
				ImageIO.write(bufImg, type, new File(path, imgFileName));
			}
		}

		document.close();
		println("完成");
	}

	static void println(String str) {
		Charset utf8Charset = Charset.forName("UTF-8");
		Charset defaultCharset = Charset.defaultCharset();
		try {
			str = new String(str.getBytes(utf8Charset), defaultCharset.name());
		} catch (UnsupportedEncodingException e) {
		}
		System.out.println(str);
	}

	static List<PDImageXObject> getImagesFromResources(PDResources resources) throws IOException {
		List<PDImageXObject> images = new ArrayList<>();

		for (COSName xObjectName : resources.getXObjectNames()) {
			PDXObject xObject = resources.getXObject(xObjectName);
			if (xObject instanceof PDImageXObject) {
				images.add(((PDImageXObject) xObject));
			}
		}

		return images;
	}

	static void dump(File file) throws IOException {
		PDDocument document = PDDocument.load(file);

		int pageTotal = document.getNumberOfPages();
		println("总页数: " + pageTotal);

		dumpDict(document.getDocument().getTrailer(), 1);

		for (int i = 0; i < document.getNumberOfPages(); i++) {
			PDPage page = document.getPage(i);
			PDRectangle mediaBox = page.getMediaBox();
			float height = mediaBox.getHeight();
			float width = mediaBox.getWidth();

			println(i + ": " + width + "×" + height);
			dumpDict(page.getCOSObject(), 1);
		}

		document.close();
		println("完成");
	}

	static void dumpDict(COSDictionary dict, int level) {
		String prefix = String.join("", Collections.nCopies(level, "  "));
		for (COSName name : dict.keySet()) {
			COSBase base = dict.getDictionaryObject(name.getName());
			if (base instanceof COSDictionary) {
				println(prefix + name.getName() + ": COSDictionary");
				dumpDict((COSDictionary) base, level + 1);

			} else if (base instanceof COSArray) {
				println(prefix + name.getName() + ": COSArray");
				dumpArray((COSArray) base, level + 1);

			} else {
				println(prefix + name.getName() + ":" + base);
			}
		}
	}

	static void dumpArray(COSArray array, int level) {
		String prefix = String.join("", Collections.nCopies(level, "  "));
		for (COSBase base : array) {
			if (base instanceof COSDictionary) {
				println(prefix + ": COSDictionary");
				dumpDict((COSDictionary) base, level + 1);

			} else if (base instanceof COSArray) {
				println(prefix + ": COSArray");
				dumpArray((COSArray) base, level + 1);

			} else {
				println(prefix + ":" + base);
			}
		}
	}
}
