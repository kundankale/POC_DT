package pojos;

/**
 * Created by xcaluser on 5/6/17.
 */

public class DocumentPojo {


    private String filename;
    private String filepath;
    private String fileSize;
    private boolean isSelected;

    public String getfilename() {
        return filename;
    }

    public void setFilename(String textONEs) {
        this.filename = textONEs;
    }

    public String getfilesize() {
        return fileSize;
    }

    public void setFilesize(String textONEs) {
        this.fileSize = textONEs;
    }


    public String getFilePath() {
        return filepath;
    }

    public void setFilePath(String ONEs) {
        this.filepath = ONEs;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
