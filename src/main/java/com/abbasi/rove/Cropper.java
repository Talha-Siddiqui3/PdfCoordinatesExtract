package com.abbasi.rove;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Cropper {


    private String source;
    private String currentPdfName;
    private String exportDirName;
    private String originalDir;
    private HashMap<Integer, Float> pagesEndyPosList;
    //private HashMap<Integer,Float> rightSideUselesMargin;
    private ArrayList<QuestionObj> pages;

    Cropper(String source, String exportDIR, HashMap<Integer, Float> yPosDic, ArrayList<QuestionObj> paper_list) {
        this.source = source;
        this.pages = paper_list;
        this.exportDirName = exportDIR;
        this.originalDir = new File(source).getParent();
        this.currentPdfName = new File(source).getName().replace(".pdf", "");
        this.pagesEndyPosList = yPosDic;
        //this.rightSideUselesMargin = rightMarginDic;

    }


    void beginCropping() {


        //Going over the questions on each page;
        for (int x = 0; x < pages.size(); x++) {
//
//            if (!MainClass.byParts){
            Helper.hideNumber(pages.get(x), source);
//            }
            CropQuestion(pages, x);
        }
//        }for(File pdf: pdfOfQuestions){
//           // MainClass.pdfToImage(pdf);
//           // pdf.delete();
//        }
    }


    private void CropQuestion(ArrayList<QuestionObj> list, int x) {

        QuestionObj obj = list.get(x);


        QuestionObj nextQuestion = Helper.getNext(list, x);

        System.out.println(source);


        String name = exportDirName;

        String locationOfExport = (new File(originalDir)).getParent() + "/" + name + "/";


        File exportFolder = new File(locationOfExport);
        if (!exportFolder.exists()) exportFolder.mkdirs();
        File exportFile = new File(exportFolder + "/" + currentPdfName + "-" + Integer.toString(x + 1) + ".pdf");


        float margin = obj.xPos;
//        if(rightSideUselesMargin.containsKey(obj.pageNumber)){
//            margin = rightSideUselesMargin.get(obj.pageNumber);}


        float bottomCropyPos = pagesEndyPosList.get(obj.pageNumber) != null ?
                pagesEndyPosList.get(obj.pageNumber) : -1;


        if (nextQuestion != null) {
            System.out.println(currentPdfName + " " + x);


            float nextQuestionMargin = obj.xPos;
//            float nextQuestionMargin = (rightSideUselesMargin.get(nextQuestion.pageNumber) != null)
//                    ? rightSideUselesMargin.get(nextQuestion.pageNumber): 0;


            //Case questions on the same page: Middle question
            if (nextQuestion.pageNumber == obj.pageNumber) {

                System.out.println("Question on the same page");

                //TODO: Change the filename
                cropPdf(obj.yPos, nextQuestion.yPos - 5, margin, obj.pageNumber, exportFile);


                Helper.toImage(exportFile);


                //Case question continues to the very next page
            } else if (nextQuestion.pageNumber - obj.pageNumber == 1 && obj.continuation != null) {


                System.out.println("Question on the next page");

                cropPdf(obj.yPos, bottomCropyPos, margin, obj.pageNumber, exportFile);


                File nextPageRemainder = new File(exportFolder + "/" + currentPdfName + "-" + Integer.toString(x + 1) + "Continued.pdf");

                System.out.println("1: " + nextPageRemainder);

                cropToTop(nextQuestion.yPos, nextQuestionMargin, nextQuestion.pageNumber, nextPageRemainder);

                File[] images = {exportFile, nextPageRemainder};
                Helper.toImage(images);


            }//Case where the question ends on this page.
            else if (nextQuestion.pageNumber - obj.pageNumber == 1 && obj.continuation == null) {

                System.out.println("Question ends on this page");

                cropPdf(obj.yPos, bottomCropyPos, margin, obj.pageNumber, exportFile);
                Helper.toImage(exportFile);

            }
            //There are parts in between.
            else if (nextQuestion.pageNumber - obj.pageNumber > 1) {
                cropPdf(obj.yPos, bottomCropyPos, margin, obj.pageNumber, exportFile);
                File exportFileContinue = new File(exportFolder + "/" + currentPdfName + "-" + Integer.toString(x + 1) + "Continued.pdf");


                System.out.println("2: " + exportFileContinue);
                System.out.println(obj.pageNumber - nextQuestion.pageNumber);

                cropRemaining(obj.pageNumber, nextQuestion.pageNumber, exportFileContinue);

                File[] images = {exportFile, exportFileContinue};
                Helper.toImage(images);


            } else {
                File exportFileContinue = new File(exportFolder + "/" + currentPdfName + "-" + Integer.toString(x + 1) + "Continued.pdf");

                System.out.println("3: " + exportFileContinue);

                File exportFileMiddle = new File(exportFolder + "/" + currentPdfName + "-" + Integer.toString(x + 1) + "Middle.pdf");
                cropPdf(obj.yPos, bottomCropyPos, margin, obj.pageNumber, exportFile);
                cropRemaining(obj.pageNumber, nextQuestion.pageNumber, exportFileMiddle);
                cropToTop(nextQuestion.yPos, nextQuestionMargin, nextQuestion.pageNumber, exportFileContinue);


                File[] images = {exportFile, exportFileMiddle, exportFileContinue};
                Helper.toImage(images);


            }


        } else {


            System.out.println(obj.yPos + " " + bottomCropyPos);
            cropPdf(obj.yPos, bottomCropyPos, margin, obj.pageNumber, exportFile);


            try {
                PDDocument doc = PDDocument.load(new File(source));
                if (doc.getNumberOfPages() == obj.pageNumber) {


                    System.out.println("Cropping: " + x + 1);

                    Helper.toImage(exportFile);
                    return;
                }


                System.out.println("Looping: " + x + 1);


                File exportFileContinue = new File(exportFolder + "/" + currentPdfName + "-" + Integer.toString(x + 1) + "Continued.pdf");
                System.out.println("4: " + exportFileContinue);
                cropRemaining(obj.pageNumber, 0, exportFileContinue);


                File[] images = {exportFile, exportFileContinue};
                Helper.toImage(images);

                doc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }


    private String[] rotatedPDFS = {"w_16", "w_17", "w_18", "w_19", "s_17", "s_18", "s_19"};


    private void cropPdf(float up, float down, float xPos, int pageNumber, File dest) {
        //Adjusting the page number for the pdfBox library.
        pageNumber -= 1;
        float newXPos = 35;


        try {
            PDDocument doc = PDDocument.load(new File(source));
            PDDocument newDoc = new PDDocument();
            PDPage page = doc.getPage(pageNumber);

            if (down == -1) {
                if (Helper.exceptionPDF(source)) {
                    down = 0;
                } else {
                    down = page.getBBox().getHeight();
                }

            }

            //down = (down == -1) ? page.getBBox().getHeight(): down;


            float crop_height = down - up;

            float width = (xPos != 0.0) ? xPos : doc.getPage(pageNumber).getBBox().getWidth() - xPos;


            if (Helper.exceptionPDF(currentPdfName)) {


                System.out.println("SWITCHING COORDINATES");
                float[] newValues = Helper.switchCoordinates(width, crop_height, newXPos, up);
                width = newValues[0];
                crop_height = newValues[1];
                newXPos = newValues[2];
                up = newValues[3];
            }

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


    private void cropToTop(float up, float xPos, int pageNumber, File dest) {

        cropPdf(up, -1, xPos, pageNumber, dest);
    }


    private void cropRemaining(int from, int to, File dest) {
        float minY;
        if(currentPdfName.contains("qp")){
            minY= Constants.minimumYPosQp;
        }
        else{   minY= Constants.minimumYPosMs;

        }

        from -= 1;
        to -= 1;
        float newXPos = 35;


        try {
            PDDocument doc = PDDocument.load(new File(source));
            PDDocument newDoc = new PDDocument();


            to = (to == -1) ? doc.getNumberOfPages() : to;


            for (int x = from + 1; x < to; x++) {

                System.out.println("Cropping");
                float xPos = 0;


                //   float yPos = doc.getPage(x).getCropBox().getLowerLeftY() + 15;

//                float down = (pagesEndyPosList.get(x) == null) ?
//                        Constants.minimumYPos : pagesEndyPosList.get(x);
//



                float down = Helper.exceptionPDF(currentPdfName) ? 73 : minY;
                float crop_height=0;
                float end = 0;

                //float crop_height = doc.getPage(x).getCropBox().getHeight();
                //float crop_height = end - down;

                float height = Helper.exceptionPDF(currentPdfName) ? doc.getPage(x).getCropBox().getWidth() : doc.getPage(x).getCropBox().getHeight();
                //float crop_height = Math.abs(height - down);
                //float crop_height =  end;



                if (Helper.exceptionPDF(currentPdfName)){
                    if (!pagesEndyPosList.containsKey(x + 1)) {
                        System.out.println("ERRROR" +currentPdfName+ "value: " +(x+1));
                        crop_height=Math.abs(height - down);
                    }
                    else{
                        end = pagesEndyPosList.get(x + 1);


                        crop_height = end;

                    }




                }else{
                    if (!pagesEndyPosList.containsKey(x + 1)) {
                   // System.out.println("ERRROR" +currentPdfName+ "value: " +(x+1));
                    crop_height=Math.abs(height - down);
                }
                else{
                    down = Math.abs(height-down);
                    end = pagesEndyPosList.get(x + 1);
                    crop_height =  end;
                  //  System.out.println("HEIGHT:" + height+"Down: "+down);
                    //System.out.println("IMPPPPP" + end + "PAGE NO:" + x);

                   // crop_height = (height - end)-60;
                    crop_height = (height - end)-minY;
                    down = end;

                }}

                //end = Math.abs(height - end);

                //down =





                float width = (xPos != 0.0) ? xPos : doc.getPage(x).getBBox().getWidth() - xPos;
                //  float crop_height = (down== -1) ? doc.getPage(x).getCropBox().getHeight() : doc.getPage(x).getCropBox().getHeight() - down;


                if (Helper.exceptionPDF(currentPdfName)) {

                    float[] newValues = Helper.switchCoordinates(width, crop_height, newXPos, down);
                    width = newValues[0];
                    crop_height = newValues[1];
                    newXPos = newValues[2];
                    down = newValues[3];
                }

                PDRectangle cropBox = new PDRectangle(newXPos, down, width, crop_height);
                doc.getPage(x).setCropBox(cropBox);
                newDoc.addPage(doc.getPage(x));
            }
            newDoc.save(dest.getAbsolutePath());
            doc.close();
            newDoc.close();
        } catch (IOException e) {
            System.out.println("Exception at @CroppingHelper/cropRemaining: " + e.getLocalizedMessage());
        }
    }


}
