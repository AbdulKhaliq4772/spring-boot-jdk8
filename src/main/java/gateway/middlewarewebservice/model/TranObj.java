package gateway.middlewarewebservice.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.wfe.GlobalContext;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 14-Sep-20.
 */
@XmlRootElement
public class TranObj {
    private static Logger logger = LogManager.getLogger(TranObj.class);

    private String transamount;
    private String transdate;
    private String translabel;
    private String transnumber;
    //private String transsupport;
    private String transcurrency;
    private String transtype;


    public String getTransamount() {
        return transamount;
    }

    public void setTransamount(String transamount) {
        this.transamount = transamount;
    }

    public String getTransdate() {
        return transdate;
    }

    public void setTransdate(String transdate) {
        this.transdate = transdate;
    }

    public String getTranslabel() {
        return translabel;
    }

    public void setTranslabel(String translabel) {
        this.translabel = translabel;
    }

    public String getTransnumber() {
        return transnumber;
    }

    public void setTransnumber(String transnumber) {
        this.transnumber = transnumber;
    }

//    public String getTranssupport() {
//        return transsupport;
//    }
//
//    public void setTranssupport(String transsupport) {
//        this.transsupport = transsupport;
//    }

    public String getTranstype() {
        return transtype;
    }

    public void setTranstype(String transtype) {
        this.transtype = transtype;
    }

    public String getTranscurrency() {
        return transcurrency;
    }

    public void setTranscurrency(String transcurrency) {

        if(Util.hasText(transcurrency)){
            try {
                //logger.info("Step 1: "+this.transcurrency);
                this.transcurrency = Util.hasText(GlobalContext.getInstance().getCurrency(Integer.valueOf(transcurrency)).getName())
                        ? GlobalContext.getInstance().getCurrency(Integer.valueOf(transcurrency)).getName()
                        : transcurrency;
                //logger.info("Step 2: "+this.transcurrency);
            }catch (Exception e){
                this.transcurrency =  transcurrency;
                //logger.info("Step 3: "+this.transcurrency);

            }
        }else {
            this.transcurrency = transcurrency;
            //logger.info("Step 4: "+this.transcurrency);

        }
    }
}
