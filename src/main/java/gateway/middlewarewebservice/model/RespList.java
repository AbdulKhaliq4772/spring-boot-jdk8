package gateway.middlewarewebservice.model;

public class RespList {

    private String rescode;

    private String resdesc;

    private String engmessage;

    private String framessage;

    private String title;


    public RespList(String code, String desc, String eng, String fra,String title)
    {
        this.rescode = code;
        this.resdesc = desc;
        this.engmessage = eng;
        this.framessage = fra;
        this.title = title;
    }

    public String getRescode() {
        return rescode;
    }

    public void setRescode(String rescode) {
        this.rescode = rescode;
    }

    public String getEngmessage() {
        return engmessage;
    }

    public void setEngmessage(String engmessage) {
        this.engmessage = engmessage;
    }

    public String getFramessage() {
        return framessage;
    }

    public void setFramessage(String framessage) {
        this.framessage = framessage;
    }

    public String getResdesc() {
        return resdesc;
    }

    public void setResdesc(String resdesc) {
        this.resdesc = resdesc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
