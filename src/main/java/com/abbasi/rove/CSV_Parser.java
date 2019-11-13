package com.abbasi.rove;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class CSV_Parser {

    private File csv;
    private File lastTextCSV;



    CSV_Parser(String location, String lastTextCSVLocation){ csv = new File(location);
    lastTextCSV = new File(lastTextCSVLocation);
    }



    public HashMap<String, HashMap<Integer,Float>> loadLastPageText(){
        HashMap<String, HashMap<Integer,Float>> lastTextPaperList = new HashMap<>();
        try {


            FileReader reader = new FileReader(lastTextCSV);
            BufferedReader br = new BufferedReader(reader);

            String currentLine;

            while ((currentLine = br.readLine()) != null) {


                String[] commaBreak = currentLine.split(",");
                String paperName = currentLine.split("-")[0];
                String qNumber_String = commaBreak[0].split("-")[1];
                Integer qNumber = Integer.parseInt(qNumber_String);
                String lastTextYPos_String = commaBreak[1];
                lastTextYPos_String = lastTextYPos_String.split("-")[0];

                Float lastTextYPos = Float.parseFloat(lastTextYPos_String);

                HashMap<Integer, Float> list = lastTextPaperList.get(paperName) != null ? lastTextPaperList.get(paperName)
                        : new HashMap<Integer, Float>();

                if(Helper.exceptionPDF(paperName)){
                    lastTextYPos = 595 - lastTextYPos;
                }

                list.put(qNumber,lastTextYPos);
                lastTextPaperList.put(paperName,list);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return lastTextPaperList;
    }



    public HashMap<String, ArrayList<QuestionObj>> loadCSV() {
        HashMap<String, ArrayList<QuestionObj>> paperDic = new HashMap<>();
        try {
            FileReader reader = new FileReader(csv);
            BufferedReader br = new BufferedReader(reader);

            String currentLine;

            while((currentLine = br.readLine()) != null){
                //print(currentLine);

                String[] data = currentLine.split(",");
                String[] row_1 = data[0].split(".pdf");
                String name = row_1[0] + ".pdf";


                QuestionObj currentQuestion = extractInformation(currentLine);

                if(Helper.exceptionPDF(name)){
                    currentQuestion.yPos = 595 - currentQuestion.yPos;
                }


                ArrayList<QuestionObj> list = paperDic.get(name) != null ? paperDic.get(name)
                        : new ArrayList<QuestionObj>();


                QuestionObj previousObj = Helper.getPreviousObj(list);

                float questionISAContination = Helper.exceptionPDF(name) ? 70:  Constants.questionIsAContinuation;

                if (previousObj != null) {


                    if ((currentQuestion.pageNumber - previousObj.pageNumber) > 1) {
                        previousObj.continuation = currentQuestion;
                    } else if ((Helper.exceptionPDF(name) && currentQuestion.yPos > questionISAContination) || (!Helper.exceptionPDF(name) && currentQuestion.yPos < questionISAContination)) {
                        //System.out.println("Name: "+ name + " lastYPOS: "+ currentQuestion.yPos);
                        previousObj.continuation = currentQuestion;
                    }

                }

                list.add(currentQuestion);
                paperDic.put(name,list);

            }









        }catch (IOException e){
            e.printStackTrace();
        }
        return paperDic;
    }


    public HashMap<String, HashMap<Integer,Boolean>> rightSideMargin(){
        HashMap<String, HashMap<Integer,Boolean>> lastTextPaperList = new HashMap<>();
        try {


            FileReader reader = new FileReader(lastTextCSV);
            BufferedReader br = new BufferedReader(reader);

            String currentLine;

            while ((currentLine = br.readLine()) != null) {


                String[] commaBreak = currentLine.split(",");
                String paperName = currentLine.split("-")[0];
                String qNumber_String = commaBreak[0].split("-")[1];
                Integer qNumber = Integer.parseInt(qNumber_String);
                Boolean marginExists = commaBreak[1].split("-")[1].equals( "true");

//                System.out.println(marginExists);

                HashMap<Integer, Boolean> list = lastTextPaperList.get(paperName) != null ? lastTextPaperList.get(paperName)
                        : new HashMap<Integer, Boolean>();

                if(Helper.exceptionPDF(paperName)){
                    marginExists = false;
                }

                list.put(qNumber,marginExists);
                lastTextPaperList.put(paperName,list);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return lastTextPaperList;

    }



    private QuestionObj extractInformation(String line){
        String[] data = line.split(",");

        String[] row_1 = data[0].split(".pdf");
       // String name = row_1[0] + ".pdf";
        String q_no_String = row_1[1].replace("_","").replaceAll("[^\\d]", "" );
        int questionNumber = Integer.parseInt(q_no_String);
        //String xCoordinate_String = data[1].replace("\"","");
        //Float xCoordinate = Float.parseFloat(xCoordinate_String);

        String YCoordinate_String = data[2].replace("\"","");
        Float YCoordinate = Float.parseFloat(YCoordinate_String) + 7;




        String pageNumber_String = data[3].replace("\"","");
        int pageNumber = Integer.parseInt(pageNumber_String) ;
       // System.out.println(pageNumber + " " + questionNumber + " "+ YCoordinate + " "+ name);


        QuestionObj qp = new QuestionObj(YCoordinate,0,pageNumber);
        qp.question_number = questionNumber;

        return qp;

    }



}
