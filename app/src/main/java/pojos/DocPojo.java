package pojos;

/**
 * Created by xcaluser on 1/7/17.
 */

public class DocPojo {
    public String documentName;
    public int documentLength;


    public void setDocumentName(String name){

        documentName = name;
    }
    public String getDocumentName(){

        return documentName;
    }

    public void setDocumentLength(int length){

        documentLength = length;
    }
    public int getDocumentLength(){

        return documentLength;
    }
}
