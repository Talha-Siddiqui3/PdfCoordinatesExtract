import csv
import os

from pdfminer.converter import PDFPageAggregator
from pdfminer.layout import LAParams, LTTextBox
from pdfminer.pdfdocument import PDFDocument
from pdfminer.pdfinterp import PDFPageInterpreter
from pdfminer.pdfinterp import PDFResourceManager
from pdfminer.pdfpage import PDFPage
# FOR LANDSACEP USE Y < 520 w16 onwards
from pdfminer.pdfparser import PDFParser
from pdfminer.pdftypes import resolve1


def textText(text):
    return_pos = 0
    try:
        if text[0].isdigit() and text[1].isdigit():
            if text[2] == "\n" and 3 < len(text):
                return_pos = 3
        elif text[0].isdigit() and 2 < len(text):
            if text[1] == "\n":
                return_pos = 2
    except:
        return_pos = -1
    return return_pos


questionToCoordinates = {}
questionPageToLastTextBoxCoordinateY = {}
eachPageExaminerUse = {}


def isLandscape(filename):
    if filename.__contains__("w16") or filename.__contains__("s17") or filename.__contains__(
            "w17") or filename.__contains__("s18") or filename.__contains__("w18") or filename.__contains__("s19") or filename.__contains__("w19"):
        return True
    else:
        return False


def storeIfGreater(parameters, y, valueToStore,currPageNo):
    if parameters not in questionToCoordinates.keys():
        questionToCoordinates[parameters] = valueToStore
    else:
        if not int(questionToCoordinates[parameters].split(",")[2]) < currPageNo:
            if y > float(questionToCoordinates[parameters].split(",")[1]):
                questionToCoordinates[parameters] = valueToStore


def storeIfLesser(filename, currY, currPageNo,text):
    # print(currY)
    if not text or text.__contains__("Cambridge International Examinations") or text.__contains__("UCLES") or text.__contains__("5070") or text.count("Turn over") or text.__contains__("BLANK PAGE") or text.__contains__("Permission  to  reproduce ") or text.__contains__("Page"):
        return
    if text.__contains__("Examiner's") or text.__contains__("Examiner’s") or text.__contains__("For Examiner’s Use") or text.__contains__("Examiner’s Use"):
        eachPageExaminerUse[filename + "-" + str(currPageNo)] = "true"
    else:
        if filename + "-" + str(currPageNo) not in eachPageExaminerUse.keys():
            eachPageExaminerUse[filename + "-" + str(currPageNo)] = "false"
    if filename + "-" + str(currPageNo) not in questionPageToLastTextBoxCoordinateY.keys():
        if filename.__contains__("ms"):
            currY -= 9
        questionPageToLastTextBoxCoordinateY[filename + "-" + str(currPageNo)] = str(currY) + "-" + eachPageExaminerUse[filename + "-" + str(currPageNo)]
    else:
        if currY < float(questionPageToLastTextBoxCoordinateY[filename + "-" + str(currPageNo)].split("-")[0]):
            if filename.__contains__("ms"):
                currY -= 9
            questionPageToLastTextBoxCoordinateY[filename + "-" + str(currPageNo)] = str(currY) + "-" + eachPageExaminerUse[filename + "-" + str(currPageNo)]


#root = "C:\\Users\\talha\\Desktop\\chem crop images\\qp\\"
#root = "C:\\Users\\talha\Desktop\\chem crop images\\ms\\Broken\\"
#root = "C:\\Users\\talha\\Desktop\\missing ms\\check\\"
#root = "C:\\Users\\talha\\Desktop\\abcd\\"
#root = "C:\\Users\\talha\\Desktop\\abcdss\\Chemistry5070\\"
root = "C:\\Users\\talha\\Desktop\\aalo123\\Chemistry5070\\"
for filename in os.listdir(root):
    if filename.__contains__("ms") or filename.count("qp"):
        x_limit = 60
        y_limit = 782
        fp = open(root + filename, 'rb')
        if filename.__contains__("ms"):
            if isLandscape(filename):
                y_limit = 525
                x_limit = 86
            else:
                y_limit = 786
                x_limit = 86
                # ONLY FOR EXCEPTION:
            if filename.__contains__("w04") or filename.__contains__("w05") or filename.__contains__("w06") or filename.__contains__("s05") or filename.__contains__("s17_ms_21"):
                x_limit = 103
        elif filename.__contains__("qp"):
            x_limit = 60
            y_limit = 786

        rsrcmgr = PDFResourceManager()
        laparams = LAParams()
        device = PDFPageAggregator(rsrcmgr, laparams=laparams)
        interpreter = PDFPageInterpreter(rsrcmgr, device)
        parser = PDFParser(fp)
        document = PDFDocument(parser)
        pagesCount = resolve1(document.catalog['Pages'])['Count']
        pages = PDFPage.get_pages(fp)
        if filename.__contains__("qp"):
            pagesCount = pagesCount - 1
        for index, page in enumerate(pages):
            pageNo = index + 1
            # print('Processing next page...')
            if pageNo > 1 and pageNo <= pagesCount:
                interpreter.process_page(page)
                layout = device.get_result()
                for lobj in layout:
                    if isinstance(lobj, LTTextBox):
                        x, y, ydown, text = lobj.bbox[0], lobj.bbox[3], lobj.bbox[1], lobj.get_text().lstrip()
                        pos = textText(text)
                        storeIfLesser(filename, ydown, pageNo, text)
                        print('At %r is text:%s' % ((x, y), text))
                        #print(pos)
                        #print(text[pos])
                        if pos != -1 and x < x_limit and y < y_limit:
                            if isLandscape(filename) and filename.__contains__("ms"):
                                y += 11

                            try:
                                if (text[pos].isdigit() or text[pos] == "A" or text[pos] == "B") and text[
                                    pos + 1].isdigit() and text[pos + 2].isdigit():
                                    # print(text)
                                    print("Question no is: " + text[pos] + text[pos + 1] + text[
                                        pos + 2] + " Coordinates are: " + str(x) + "," + str(y))
                                    storeIfGreater(filename + "_" + text[pos] + text[pos + 1] + text[pos + 2], y,
                                                   str(x) + "," + str(y) + "," + str(pageNo), pageNo)
                                    # questionToCoordinates[filename+"_"+text[pos] + text[pos + 1] + text[pos + 2]] = str(x) + "," + str(y) + "," + str(pageNo)
                                elif (text[pos] == "A" or text[pos] == "B") and text[pos + 1].isdigit():
                                    # print(text)
                                    print("Question no is: " + text[pos] + text[pos + 1] + " Coordinates are: " + str(
                                        x) + "," + str(y))
                                    storeIfGreater(filename + "_" + text[pos] + text[pos + 1], y,
                                                   str(x) + "," + str(y) + "," + str(pageNo), pageNo)
                                    # questionToCoordinates[filename+"_"+text[pos] + text[pos + 1]] = str(x) + "," + str(y) + "," + str(pageNo)
                                elif text[pos].isdigit() and text[
                                    pos + 1].isdigit():  # and text[pos + 2] == " " and text[pos + 3] == " ":
                                    # print(text)
                                    print("Question no is: " + text[pos] + text[pos + 1] + " Coordinates are: " + str(
                                        x) + "," + str(y))
                                    storeIfGreater(filename + "_" + text[pos] + text[pos + 1], y,
                                                   str(x) + "," + str(y) + "," + str(pageNo), pageNo)
                                    # questionToCoordinates[filename+"_"+text[pos] + text[pos + 1]] = str(x) + "," + str(y) + "," + str(pageNo)
                                elif text[pos].isdigit():  # and text[pos + 1] == " " and text[pos + 2] == " ":
                                    # print(text)
                                    print("Question no is: " + text[pos] + " Coordinates are: " + str(x) + "," + str(y))
                                    storeIfGreater(filename + "_" + text[pos], y,
                                                   str(x) + "," + str(y) + "," + str(pageNo), pageNo)
                                    # questionToCoordinates[filename+"_"+text[pos]] = str(x) + "," + str(y) + "," + str(pageNo)
                            except:
                                pass

# print(questionPageToLastTextBoxCoordinateY)
# for currQuestionToCoordinate in questionToCoordinates:
#     currQuestionPageNo = questionToCoordinates[currQuestionToCoordinate].split(",")[2]
#     questionToCoordinates[currQuestionToCoordinate] = questionToCoordinates[currQuestionToCoordinate] + "," + questionPageToLastTextBoxCoordinateY[currQuestionPageNo]


with open('C:\\Users\\Talha\\Desktop\\crop_data.csv', 'w', newline='') as f:
    # print(questionToCoordinates)
    w = csv.writer(f)
    w.writerows(questionToCoordinates.items())

with open('C:\\Users\\Talha\\Desktop\\crop_data_pagesLastText.csv', 'w', newline='') as f:
    w = csv.writer(f)
    w.writerows(questionPageToLastTextBoxCoordinateY.items())
