package com.immortalcrab.bill.pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.commons.io.FilenameUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenderImgHelper {

    private static final String IMAGE_EXT = "jpg";
    private static final String DEFAULT_OUTPUT_IMAGE_DIR = "outputImages";

    private final String outputImageDirectory;
    private final InputStream inputStream;

    public static String[] transformFromPdf(
            final String outputImageDirectory,
            InputStream is,
            String title) throws IOException {
        Optional<String> outputImgDir = Optional.ofNullable(outputImageDirectory);
        var rh = new RenderImgHelper(
                outputImgDir.orElse(DEFAULT_OUTPUT_IMAGE_DIR), is);
        return rh.snapshotForEachPage(title);
    }

    public static String[] transformFromPdf(
            final String outputImageDirectory, File file) throws IOException {
        return transformFromPdf(
                outputImageDirectory,
                new FileInputStream(file),
                FilenameUtils.removeExtension(file.getName()));
    }

    public String[] snapshotForEachPage(final String title) throws IOException {
        File directory = new File(outputImageDirectory);
        if (!directory.exists()) {
            directory.mkdir();
        }
        try ( PDDocument document = PDDocument.load(inputStream)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            return renderPages(pdfRenderer, title, document.getNumberOfPages());
        }
    }

    private String[] renderPages(PDFRenderer pdfRenderer, final String title, final int noPages) throws IOException {
        String[] files = new String[noPages];
        for (int idx = 0; idx < noPages; idx++) {
            files[idx] = outputImageDirectory + "/" + title + "_" + (idx + 1) + "." + IMAGE_EXT;
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(idx, 300, ImageType.BINARY);
            ImageIO.write(bufferedImage, IMAGE_EXT.toUpperCase(), new File(files[idx]));
        }
        return files;
    }
}
