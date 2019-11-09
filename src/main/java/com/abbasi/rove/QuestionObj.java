package com.abbasi.rove;

import java.util.ArrayList;

public class QuestionObj implements Comparable<QuestionObj> {
    float yPos;
    float xPos;
    int pageNumber;
    int question_number = 0;
    QuestionObj continuation;


    public QuestionObj(float yPos, float xPos, int pageNumber) {
        this.yPos = yPos;
        this.xPos = xPos;
        this.pageNumber = pageNumber;
    }
    void printMe(){
        System.out.println("yPos: "+ yPos+" xPos: "+xPos+" PageNumber: "+pageNumber);
        System.out.println("Extra space");

    }
    boolean equals(QuestionObj qp1){
        return (qp1.yPos == yPos)&&(qp1.pageNumber == pageNumber);
    }

    public int compareTo(QuestionObj o) {
        return (o.yPos == this.yPos)&&(o.pageNumber == this.pageNumber)? 1:0;
    }

    @Override
    public boolean equals(Object obj) {
        QuestionObj o = (QuestionObj) obj;
        return (o.yPos == this.yPos)&&(o.pageNumber == this.pageNumber);
    }
}
