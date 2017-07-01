package Utils;

/**
 * Created by rushd on 29/09/2016.
 */

public class CreateList {

    private String image_title;
    private Integer image_id;
    private String image_path;
    private boolean issleected;

    public String getImage_title() {
        return image_title;
    }
    public boolean getselected() {
        return issleected;
    }
    public String getImage_path() {
        return image_path;
    }
    public void setImage_title(String android_version_name) {
        this.image_title = android_version_name;
    }

    public Integer getImage_ID() {
        return image_id;
    }

    public void setImage_ID(Integer android_image_url) {
        this.image_id = android_image_url;
    }
    public void setselected(boolean flag) {
        this.issleected=flag;
    }
    public void setImage_Path(String path) {
        this.image_path = path;
    }
}