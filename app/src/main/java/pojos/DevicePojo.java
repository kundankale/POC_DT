package pojos;

/**
 * Created by xcaluser on 25/4/17.
 */

public class DevicePojo {

    public String serviceName,host , os;
    int port;



    public void setServiceName(String mServiceName){

        this.serviceName = mServiceName;
    }
    public String getServiceName(){

        return serviceName;
    }

    public void setHost(String mhost){

        this.host = mhost;
    }
    public String getHost(){

        return host;
    }

    public void setPort(int mPort){

        this.port = mPort;
    }
    public int getPort(){

        return port;
    }

    public void setOs(String Os){

        this.os = Os;
    }
    public String getOs(){

        return os;
    }

}
