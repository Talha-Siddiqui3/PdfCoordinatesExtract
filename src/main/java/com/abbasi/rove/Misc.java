package com.abbasi.rove;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType3Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Misc {


    String locationBinder;
    String mainPDFFolder;

    HashMap<String, Integer> pdfPageCounts = new HashMap<>();
    ArrayList<String> pdfNames = new ArrayList();
    int currentPDF = 0;


    Misc(String binder, String pdfFolder){
        locationBinder = binder;
        mainPDFFolder = pdfFolder;

    }


    int getNumberofPages(File pdf) throws IOException{
        int pageCount = 0;
        PDDocument doc = PDDocument.load(pdf);
        pageCount = doc.getNumberOfPages();
        doc.close();
        return pageCount;
    }

    void getPDFPageCounts(File dir) throws IOException{

        //for (File f: dir.listFiles()){}

        for(File pdf : dir.listFiles()){

            System.out.println(pdf.getName());
            if(!pdf.getName().contains(".pdf")){continue;}

            int pagecount = getNumberofPages(pdf);
            //System.out.println(pagecount);
            pdfPageCounts.put(pdf.getName(),pagecount);
            pdfNames.add(pdf.getName());
        }

    }
    void splitPDF(File file, int start, int end) throws IOException{
        PDDocument original = PDDocument.load(file);
        PDDocument newPDF = new PDDocument();

        for(int x = start; x < end; x++){
            newPDF.addPage(original.getPage(x));
        }

        File exportFolder = new File(mainPDFFolder,"Broken");
        if (!exportFolder.exists()){exportFolder.mkdirs();}
        newPDF.save(new File(exportFolder, pdfNames.get(currentPDF)));
        original.close();
        newPDF.close();

    }
    void splitBinder(){

        try {

            getPDFPageCounts(new File(mainPDFFolder));

            PDDocument doc = PDDocument.load(new File(locationBinder));

            int pageCount = 0;
            int previousPDF = 0;

            System.out.println(doc.getNumberOfPages());

            for(int x = 0; x< doc.getNumberOfPages(); x++){


                if(pageCount == pdfPageCounts.get(pdfNames.get(currentPDF))){

                    System.out.println("Current iteration: "+x +" Current Page: "+pageCount + " Previous: "+previousPDF);
                    splitPDF(new File(locationBinder),previousPDF,x);

                    pageCount = 1;
                    previousPDF = x;
                    currentPDF +=1;

                }else{
                    pageCount +=1;
                }

            }
            splitPDF(new File(locationBinder),previousPDF,doc.getNumberOfPages());


        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
