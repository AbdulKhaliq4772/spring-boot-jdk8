package gateway.middlewarewebservice.base;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SecurityParamsModel {

    private Long id;

    private String deviceid;

    private String firebasetoken;

    private String devicemodel;

    private String operatingsystem;

    private String screenresolution;

    private String gpslatitude;

    private String gpslongitude;

    private String rootedflag;

    private String baseintegrityflag;

    private String ctsprofileflag;


    public String getFirebasetoken() {
        return firebasetoken;
    }

    public void setFirebasetoken(String firebasetoken) {
        this.firebasetoken = firebasetoken;
    }

    public String getDevicemodel() {
        return devicemodel;
    }

    public void setDevicemodel(String devicemodel) {
        this.devicemodel = devicemodel;
    }

    public String getOperatingsystem() {
        return operatingsystem;
    }

    public void setOperatingsystem(String operatingsystem) {
        this.operatingsystem = operatingsystem;
    }

    public String getScreenresolution() {
        return screenresolution;
    }

    public void setScreenresolution(String screenresolution) {
        this.screenresolution = screenresolution;
    }

    public String getGpslatitude() {
        return gpslatitude;
    }

    public void setGpslatitude(String gpslatitude) {
        this.gpslatitude = gpslatitude;
    }

    public String getGpslongitude() {
        return gpslongitude;
    }

    public void setGpslongitude(String gpslongitude) {
        this.gpslongitude = gpslongitude;
    }

    public String getRootedflag() {
        return rootedflag;
    }

    public void setRootedflag(String rootedflag) {
        this.rootedflag = rootedflag;
    }

    public String getBaseintegrityflag() {
        return baseintegrityflag;
    }

    public void setBaseintegrityflag(String baseintegrityflag) {
        this.baseintegrityflag = baseintegrityflag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCtsprofileflag() {
        return ctsprofileflag;
    }

    public void setCtsprofileflag(String ctsprofileflag) {
        this.ctsprofileflag = ctsprofileflag;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }
}
