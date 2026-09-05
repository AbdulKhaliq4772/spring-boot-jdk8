package gateway.middlewarewebservice.entity;


import pk.vaulsys.apigateway.persistence.IEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name = "WS_SECURITY_PARAMS_NF")
public class NFSecurityParams implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator="WS_SECURPARAMNF_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "WS_SECURPARAMNF_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "WS_SECURPARAMNF_SEQ")
            })
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

    @Override
    public Long getId() {
        return id;
    }

    @Override
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
