package gateway.middlewarewebservice.model;

public class EXTCharge {

    private SRCDetail srcdetail;

    private DESTDetail destdetail;


    public SRCDetail getSrcdetail() {
        return srcdetail;
    }

    public void setSrcdetail(SRCDetail srcdetail) {
        this.srcdetail = srcdetail;
    }

    public DESTDetail getDestdetail() {
        return destdetail;
    }

    public void setDestdetail(DESTDetail destdetail) {
        this.destdetail = destdetail;
    }
}
