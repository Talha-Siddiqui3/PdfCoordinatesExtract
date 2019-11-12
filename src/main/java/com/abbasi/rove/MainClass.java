package com.abbasi.rove;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static com.abbasi.rove.Helper.print;
import static com.abbasi.rove.Helper.toImage;

public class MainClass {





    public static void main(String[] args) throws IOException {

//        File savedImage = new File("/Volumes/MyDrive/TestCropper/testImage123.pdf");
//        Helper.cropPdfTester(595-502, 595-289, 0, 4,
//                savedImage,
//                "/Volumes/MyDrive/TestCropper/Latest/Chemistry5070/5070_w18_ms_22.pdf");
//        toImage(savedImage);

//

        Scanner scanner = new Scanner(System.in);
        print("Please enter the main location containing the folder of past papers");
     String mainLocation = scanner.nextLine();
       /* print("Please enter the name of the subject");
    String subjectName = scanner.nextLine();*/

     // String mainLocation = "/Volumes/MyDrive/TestCropper/Latest";
       String subjectName = "Chemistry5070";
//
       CropQuestions(subjectName,mainLocation);
//        File pdf = new File("/Volumes/MyDrive/TestCropper/Testpdf.pdf");
//        PDDocument doc = PDDocument.load(pdf);
//        Helper.rotatePDF(doc);
    }

    static void splitPDF(){

        Scanner scanner = new Scanner(System.in);
        print("Enter binder dir");
        String binder = scanner.nextLine().trim();
        print("Enter pds location");
        String pdfLocation = scanner.nextLine().trim();


        Misc misc = new Misc(binder,pdfLocation);
        misc.splitBinder();
    }


    static void CropQuestions(String subjectName, String mainLocation){



        HashMap<String, HashMap<Integer,Float>> lastTextYPosList = new HashMap<>();
        HashMap<String, ArrayList<QuestionObj>> paper_list = new HashMap<>();
        HashMap<String, HashMap<Integer,Boolean>> rightSide = new HashMap<>();


        CSV_Parser csv_parser = new CSV_Parser(mainLocation+"/crop_data.csv", mainLocation+"/crop_data_pagesLastText.csv");
        lastTextYPosList = csv_parser.loadLastPageText();
        paper_list = csv_parser.loadCSV();
        rightSide = csv_parser.rightSideMargin();


        File source = new File(getAllPdfsInfolder(new File(mainLocation,subjectName),paper_list));

        for (String paper_name : paper_list.keySet()){
            HashMap<Integer, Float> yPosList = lastTextYPosList.get(paper_name);
            ArrayList<QuestionObj> questions_List = paper_list.get(paper_name);
            HashMap<Integer,Boolean> rightSideMargin = rightSide.get(paper_name);

            File source_file = new File(source,paper_name);

            Cropper cropper = new Cropper(source_file.getPath(),"CROPPED",yPosList,questions_List,rightSideMargin);
            cropper.beginCropping();


        }



        Helper.groupFiles(new File(mainLocation + File.separator + subjectName,"CROPPED"),mainLocation,"CroppedImages-"+subjectName);

    }



    private static String getAllPdfsInfolder(File src, HashMap paper_list){
       String tempFolder = "CROPPED_TEMP";
    try {

        File newLocation = new File(src, tempFolder);
        newLocation.mkdir();


        for (File file : src.listFiles()) {


            String name = file.getName();
            File destination = new File(newLocation, name);
            // System.out.println(destination);

            if (paper_list.get(name) != null) {
                System.out.println(file.getName());
                Helper.FormatPaper(file, destination);
                // pdfs.add(destination);
            }


        }

    }catch (IOException e){
        e.printStackTrace();
    }

        return new File(src,tempFolder).getPath();
    }


}
