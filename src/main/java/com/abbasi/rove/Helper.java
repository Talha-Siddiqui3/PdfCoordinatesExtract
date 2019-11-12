package com.abbasi.rove;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Helper {


    static void print(Object text) {
        System.out.println(text);
    }

    static QuestionObj getPreviousObj(ArrayList<QuestionObj> questionObjs) {

        try {
            return questionObjs.get(questionObjs.size() - 1);
        } catch (IndexOutOfBoundsException e) {
            //print(questionObjs.size());
            return null;
        }

    }

    static QuestionObj getNext(ArrayList<QuestionObj> questionObjs, int x) {
        try {
            return questionObjs.get(x + 1);
        } catch (IndexOutOfBoundsException e) {
            print(questionObjs.size());
            return null;
        }
    }


    static void toImage(File[] files) {


        try {
            String parentDir = files[0].getParent();
            parentDir += "/" + files[0].getName().replace(".pdf", "");
            File exportDir = new File(parentDir);
            exportDir.mkdirs();

            for (File pdf : files) {

                final PDDocument document = PDDocument.load(pdf);
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                for (int page = 0; page < document.getNumberOfPages(); ++page) {
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);


                    String dest = "";
                    if (pdf.getName().contains("Continued")) {
                        dest = parentDir + "/" + pdf.getName().replace(".pdf", "") + page + ".png";

                    } else {
                        dest = parentDir + "/" + pdf.getName().replace(".pdf", "") + ".png";

                    }

                    File file = new File(dest);
                    ImageIO.write(bim, "png", file);
                }
                document.close();
                pdf.delete();

            }
            String exportName = files[0].getName().replace(".pdf", ".png");
            String exportFolder = files[0].getParent();

            File finalImage = new File(exportFolder + "/" + exportName);

            getAllImagesInFolder(exportDir, finalImage);


        } catch (IOException e) {
            System.out.println("Exception @/MainClass/pdfToImage: " + e.getLocalizedMessage());

        }

    }


    private static void getAllImagesInFolder(File src, File dest) throws IOException {
        ArrayList<File> images = new ArrayList<File>();
        for (File file : src.listFiles()) {
            String name = file.getName();
            if (name.contains(".png"))
                images.add(file);

        }

        //combineImages(images,dest);
        src.delete();
    }


    static void toImage(File src) {
        try {


            System.out.println("To image");
            String location = src.getParent();
            String nameOfFile = src.getName().replace(".pdf", ".png");
            String dest = location + "/" + nameOfFile;


            final PDDocument document = PDDocument.load(src);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150, ImageType.RGB);
                File file = new File(dest);


                ImageIO.write(bim, "png", file);
            }
            document.close();
            src.delete();
        } catch (IOException e) {
            System.out.println("Exception @/MainClass/pdfToImage: " + e.getLocalizedMessage());
        }
    }


    static void rotatePDF(File file) throws IOException {
        PDDocument doc = PDDocument.load(file);
        PDDocument newDocument = new PDDocument();
        for (PDPage page : doc.getPages()) {

            PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.PREPEND, false, false);
            Matrix matrix = Matrix.getRotateInstance(Math.toRadians(90), 0, 0);
            cs.transform(matrix);
            cs.close();

            PDRectangle cropBox = page.getCropBox();
            Rectangle rectangle = cropBox.transform(matrix).getBounds();
            PDRectangle newBox = new PDRectangle((float) rectangle.getX(), (float) rectangle.getY(), (float) rectangle.getWidth(), (float) rectangle.getHeight());
            page.setCropBox(newBox);
            page.setMediaBox(newBox);
            page.setTrimBox(newBox);
            newDocument.addPage(page);
        }
        newDocument.save(file);
        newDocument.close();
        doc.close();

    }


    static ArrayList<PDPage> reversePDFRemoval(PDDocument doc) throws IOException{
        ArrayList<PDPage> pages = new ArrayList<>();


        PDFTextStripper pdfStripper = new PDFTextStripper();


        Boolean dontKeepPage = true;


        for (int x = doc.getNumberOfPages() - 1; x >= 0; x--) {

            pdfStripper.setStartPage(x + 1);
            pdfStripper.setEndPage(x + 1);

            String parsedText = pdfStripper.getText(doc);

            if (dontKeepPage) {
                dontKeepPage = shitPage(parsedText);
                if (dontKeepPage){continue;}
                }

            pages.add(doc.getPage(x));

        }

          return pages;
        }


    static Boolean shitPage(String parsedText){
        String edited = parsedText.replace("\n", "")
                .replace("\r", "").replace("ForExaminer’sUse", "")
                .replaceAll("\\.", "").replace("[Turn over", "").trim();

        return  !(!parsedText.contains("BLANK PAGE") && !parsedText.contains("starts on the next page.")
                    && !parsedText.contains("READ THESE INSTRUCTIONS FIRST")
                    &&(edited.length() > 35));
    }

    static void FormatPaper(File preface, File dest) throws IOException {
        System.out.println("Formatting Paper: " + preface.getName());


        PDDocument doc = PDDocument.load(preface);

        PDDocument newDoc = new PDDocument();
        PDFTextStripper pdfStripper = new PDFTextStripper();

        ArrayList<PDPage> pageList = reversePDFRemoval(doc);


        for (int x = pageList.size();x>= 0; x--) {

            newDoc.addPage(pageList.get(x));
        }


        newDoc.save(dest);
        newDoc.close();
        doc.close();
    }

    static void hideNumber(QuestionObj qp, String locationOfPDfOriginal) {
        if(exceptionPDF(locationOfPDfOriginal)){
            return;
        }
        try {


            PDDocument doc = PDDocument.load(new File(locationOfPDfOriginal));
            PDPage page = doc.getPage(qp.pageNumber - 1);
            PDPageContentStream content = new PDPageContentStream(doc, page, true, false);

            float yPos = exceptionPDF(locationOfPDfOriginal) ? qp.yPos - 35 : 0;

            content.addRect(qp.xPos, yPos, 70, 30);

            content.setNonStrokingColor(Color.WHITE);
            content.fill();

            //String path = "/Volumes/MyDrive/test3.pdf";
            content.close();
            doc.save(new File(locationOfPDfOriginal));
            doc.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static void groupFiles(File location, String mainLocation, String exportDirName) {
        System.out.println(location);
        try {

            File baseFolder = new File(mainLocation, exportDirName);

            for (File files : location.listFiles()) {
                if (files.getName().equals(".DS_Store")) {
                    continue;
                }

                if (!files.isDirectory()) {
                    String nameOfFile = files.getName();
                    String modifyName = nameOfFile.split("-")[1];
                    String newFolderName = nameOfFile.split("-")[0];

                    //String originalPath = files.getParent();
                    String newFolderPath = baseFolder.getAbsolutePath() + File.separator + newFolderName;
                    new File(newFolderPath).mkdirs();

                    String newFileLocation = newFolderPath + File.separator + modifyName;
                    FileUtils.copyFile(files, new File(newFileLocation));


                } else {

                    String folderName = files.getName();
                    String newFolderName = folderName.split("-")[0];
                    String modifyName = folderName.split("-")[1];

//                    String originalPath = files.getParent();


                    String mainFolderPath = baseFolder.getAbsolutePath() + File.separator + newFolderName;
                    String newFoldersPath = mainFolderPath + File.separator + modifyName;
                    new File(newFoldersPath).mkdirs();
                    FileUtils.copyDirectory(files, new File(newFoldersPath));


                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    static void cropPdfTester(float up, float down, float xPos, int pageNumber, File dest, String source) {
        //Adjusting the page number for the pdfBox library.
        pageNumber -= 1;
        float newXPos = 35;


        try {
            PDDocument doc = PDDocument.load(new File(source));
            PDDocument newDoc = new PDDocument();
            PDPage page = doc.getPage(pageNumber);
            down = (down == -1) ? page.getBBox().getHeight() : down;


            float crop_height = down - up;

            float width = (xPos != 0.0) ? xPos : doc.getPage(pageNumber).getBBox().getWidth() - xPos;


            System.out.println("SWITCHING COORDINATES");
            float[] newValues = switchCoordinates(width, crop_height, newXPos, up);
            width = newValues[0];
            crop_height = newValues[1];
            newXPos = newValues[2];
            up = newValues[3];


            page.setCropBox(new PDRectangle(newXPos, up, width, crop_height));
            newDoc.addPage(page);
            newDoc.save(dest);
            doc.close();
            newDoc.close();

        } catch (IOException e) {
            System.out.println("An IOException occurred @Cropper/cropPdf: " + e.getLocalizedMessage());
            System.exit(61);
        }

    }

    static float[] switchCoordinates(float width, float height, float x, float y) {
        System.out.println(width + " " + height + " " + x + " " + y);

        float tempY = y;
        float temp_width = width + 200;


        y = x;
        x = tempY;
        width = height;
        height = temp_width;

        float[] returnValues = {width, height, x, y};
        System.out.println(width + " " + height + " " + x + " " + y);

        return returnValues;

    }


    static Boolean exceptionPDF(String currentPdfName) {
        return (currentPdfName.contains("ms") && (currentPdfName.contains("w16") || currentPdfName.contains("w17") || currentPdfName.contains("w18") || currentPdfName.contains("w19")
                || currentPdfName.contains("s17") || currentPdfName.contains("s18") || currentPdfName.contains("s19")));
    }
}
