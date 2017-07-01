package pojos;

/**
 * Created by xcaluser on 22/6/17.
 */

public class ContactPojo {

    public String contactName;
    public int contactLength;


    public void setContactName(String name){

        contactName = name;
    }
    public String getContactName(){

        return contactName;
    }

    public void setContactLength(int length){

        contactLength= length;
    }
    public int getContactLength(){

        return contactLength;
    }
}
