package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 23-Nov-18.
 */
@XmlRootElement
public class DeviceObj {

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

    private String imei;

    private String imsi;

    private String iccid;

    private String uuid;

    private String status;


    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

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

    public String getCtsprofileflag() {
        return ctsprofileflag;
    }

    public void setCtsprofileflag(String ctsprofileflag) {
        this.ctsprofileflag = ctsprofileflag;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
