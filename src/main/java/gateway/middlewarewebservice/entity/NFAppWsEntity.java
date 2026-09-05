package gateway.middlewarewebservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import pk.vaulsys.apigateway.protocols.webservice.base.entity.WebServiceEntity;
import pk.vaulsys.apigateway.protocols.webservice.base.model.TransactionPortal;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.model.*;
import pk.vaulsys.apigateway.util.Util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by RAZA MURTAZA BAIG on 1/27/2018.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@Entity
@Table(name = "WS_LOG_NF")
public class NFAppWsEntity extends WebServiceEntity {

    @Column(name = "MOBILENUMBER")
    private String mobilenumber;

    @Column(name = "NEWMOBILENUMBER")
    private String newmobilenumber;

    @Column(name = "cnic")
    private String cnic;

    @Column(name = "CUSTOMERNAME")
    private String customername;

    @Column(name = "CNICEXPIRY")
    private String cnicexpiry;

    @Column(name = "BANKCODE")
    private String bankcode;

    @Column(name = "DESTBANKCODE")
    private String destbankcode;

    @Column(name = "BANKNAME")
    private String bankname;

    @Column(name = "ACCOUNTNUMBER")
    private String accountnumber;

    @Column(name = "ACCOUNTCURRENCY")
    private String accountcurrency;

    @Column(name = "TXNREFNUM")
    private String tranrefnumber;

    //@Column(name = "CNICPICTUREFRONT")
    @Transient //Raza making transient for performance
    private String cnicpicturefront;


    //@Column(name = "CNICPICTUREBACK")
    @Transient //Raza making transient for performance
    private String cnicpictureback;

    //@Column(name = "CUSTOMERPICTURE")
    @Transient //Raza making transient for performance
    private String customerpicture; //selfie

    @Transient //Raza making transient for performance
    private String selfiepicture; //selfie

    @Column(name = "MOTHERNAME")
    private String mothername;

    @Column(name = "DATEOFBIRTH")
    private String dateofbirth;

    @Column(name = "TRANSDATETIME")
    private String transdatetime;

    //@Column(name = "PINDATA")
    @Transient
    private String pindata;

    //@Column(name = "OLDPINDATA")
    @Transient
    private String oldpindata;

    //@Column(name = "NEWPINDATA")
    @Transient
    private String newpindata;

    @Column(name = "ENCRYPTEDKEY")
    private String encryptkey;

    @Column(name = "AMOUNTTRAN")
    private String amounttransaction;

    @Column(name = "SRCAMOUNTCHARGE")
    private String srcchargeamount;

    @Column(name = "DESTAMOUNTCHARGE")
    private String destchargeamount;

    @Column(name = "DESTACCOUNT")
    private String destaccount;

    @Column(name = "DESTACCOUNTCURRENCY")
    private String destaccountcurrency;

    @Column(name = "OTP")
    private String otp;

    @Column(name = "BIOMETRICDATA")
    private String biometricdata;

    @Column(name = "RESPCODE")
    private String respcode;

    @Column(name = "PLACEOFBIRTH")
    private String placeofbirth;

    @Column(name = "FATHERNAME")
    private String fathername;

    @Column(name = "PROVINCE")
    private String province;

    @Column(name = "TELECOMSP")
    private String tsp; //telecommunication service provider (required for OTP by banks)

    @Column(name = "DESTBANKID")
    private String destbankid;

    @Column(name = "ORIG_DATA_ELEMENT")
    private String origdataelement;

    @Column(name = "COREBANKCODE")
    private String corebankcode;

    @Column(name = "COREACCOUNT")
    private String coreaccount;

    @Column(name = "COREACCOUNTCURRENCY")
    private String coreaccountcurrency;

    @Column(name = "CARDNUMBER")
    private String cardnumber;

    //@Column(name = "CARDPINDATA")
    @Transient
    private String cardpindata;

    @Column(name = "ENABLEFLAG")
    private String enableflag;

    @Column(name = "MERCHANTID")
    private String merchantid;

    @Column(name = "DAILYLIMIT")
    private String dailylimit;

    @Column(name = "MONTHLYLIMIT")
    private String monthlylimit;

    @Column(name = "YEARLYLIMIT")
    private String yearlylimit;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "STAN")
    private String stan;

    @Column(name = "RRN")
    private String rrn;

    @Column(name = "USERID")
    private String userid;

    @Column(name = "CHANNELID")
    private String channelid;

    @Column(name = "ALLOWED")
    private String allowed;

    @Column(name = "DELETETYPE")
    private String deletetype;

    @Column(name = "COMMENTS")
    private String comments;

    @Column(name = "ACCTID")
    private String acctid;

    @Column(name = "ACCT_ALIAS")
    private String acctalias;

    @Column(name = "ISPRIMARY")
    private String isprimary;

    @Column(name = "ACCTBALANCE")
    private String accountbalance;

    @Column(name = "ACCTLIMIT")
    private String acctLimit;

    @Column(name = "AVAILLIMIT")
    private String availLimit;

    @Column(name = "AVAILLIMITFREQ")
    private String availLimitfreq;

    @Column(name = "STATE")
    private String state;

    @Column(name = "REQUESTTIME")
    private String requesttime;

    @Column(name = "ACTIVATIONTIME")
    private String activationtime;

    @Column(name = "DESTUSERID")
    private String destuserid;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "CITY")
    private String city;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "ADVANCEFLAG")
    private String advanceflag;

    @Column(name = "SECONDARYNUMBER")
    private String secondarynumber;

    @Column(name = "USERTOKEN")
    private String accesstoken;

    @Column(name = "INOUTFILTER")
    private String inoutfilter;

    @Column(name = "TYPEFILTER")
    private String typefilter;

    @Column(name = "SEARCHTEXT")
    private String searchtext;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "BANKID")
    private String bankid;

    /*@Column(name = "GPSLATITUDE")
    private String gpslatitude;

    @Column(name = "GPSLONGITUDE")
    private String gpslongitude;*/

    @Column(name = "PARENTID")
    private String parentid;

    @Column(name = "MERCHANTNAME")
    private String merchantname;

    @Column(name = "MERCHANTCATEGORYID")
    private String categoryid;

    @Column(name = "TRUSTEDFLAG")
    private String trustedflag;

    @Column(name = "PHONENUMBER")
    private String phonenumber;

    @Column(name = "TRANSACTIONLIMIT")
    private String transactionlimit;

    @Column(name = "CATEGORYNAME")
    private String categoryname;

    @Column(name = "MERCHANTSTATE")
    private String merchantstate;

    @Column(name = "MERCHANTENABLED")
    private String merchantenabled;

    @Column(name = "MERCHANTBLOCKED")
    private String merchantblocked;

    @Column(name = "MINIMUMAMOUNT")
    private String minimumamount;

    @Column(name = "MAXIMUMAMOUNT")
    private String maximumamount;

    @Column(name = "SOURCECHARGETYPE")
    private String sourcechargetype;

    @Column(name = "DESTINATIONCHARGPE")
    private String destinationchargetype;

    @Column(name = "CONSUMERNO")
    private String consumerno;

    @Column(name = "UTILCOMPANYID")
    private String utilcompanyid;

    @Column(name = "CONSUMERDETAIL")
    private String consumerdetail;

    @Column(name = "BILLSTATUS")
    private String billstatus;

    @Column(name = "DUEDATE")
    private String duedate;

    @Column(name = "AMTWITHINDUEDATE")
    private String amtwithinduedate;

    @Column(name = "AMTAFTERDUEDATE")
    private String amtafterduedate;

    @Column(name = "BILLINGMONTH")
    private String billingmonth;

    @Column(name = "DATEPAID")
    private String datepaid;

    @Column(name = "AMOUNTPAID")
    private String amtpaid;

    @Column(name = "TRANAUTHID")
    private String tranauthid;

    @Column(name = "RESERVED")
    private String reserved;

    @Column(name = "IDENTIFICATIONNO")
    private String identificationno;

    @Column(name = "PING")
    private String ping;

//    @Column(name = "TRANTYPE")
//    private String trantype;

    @Column(name = "DESTUSERNAME")
    private String destusername;

    @Column(name = "AGENTID")
    private String agentid;

    @Column(name = "REFERENCENUMBER")
    private String referencenumber;

    @Column(name = "INVOICEID")
    private String invoiceid;

    @Column(name = "VERIFIEDFLAG")
    private String verifiedflag;

    @Column(name = "STARTDATE")
    private String startdate;

    @Column(name = "ENDDATE")
    private String enddate;

    @Column(name = "BANKTXNFLAG")
    private String banktxnflag;

    @Column(name = "BLOCKEDFLAG")
    private String blockedflag;

    @Column(name = "BILLERTXNID")
    private String billertxnid;

    @Column(name = "CREATIONDATE")
    private String creationdate;

    @Column(name = "CARDNOLASTDIGITS")
    private String cardNoLastDigits;

    @Transient
    private List<AccountLimit> accountlimits;

    @Transient
    private List<LinkedAccountObj> linkedaccounts;

    @Transient
    private List<ProvisionalWallet> provisionalwallets;

    @Transient
    private List<WalletAccount> accountlist;

    @Transient
    private List<TransactionPortal> transactions;

    @Transient
    private List<UserTransaction> usertransactions;

    //m.rehman: for NayaPay, for merchant transaction list
    @Transient
    private List<MerchantTransaction> merchantTransactions;

    //m.rehman: for NayaPay
    @Transient
    private TransactionPortal transactionDetail;

    @Column(name = "BANK_MNEMONIC")
    private String bankMnemonic;
    //m.rehman: for NayaPay, onelink ubps password <end>

    @Column(name = "AVAILABLE_BALANCE")
    //Raza adding for Meezan Response for BalanceInquiry
    private String availablebalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECURITY_PARAMS")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="WSLOGNF_SECURPARAM_FK")
    @Index(name="idx_WSLOGNF_SECURPARAM_FK")
    private NFSecurityParams nfsecurityparams;


    @Column(name = "SECRET_QUESTION_1")
    private String secretquestion1;

    @Column(name = "SECRET_QUET_ANS_1")
    private String secretquestionanswer1;

    @Column(name = "SECRET_QUESTION_2")
    private String secretquestion2;

    @Column(name = "SECRET_QUET_ANS_2")
    private String secretquestionanswer2;

    @Column(name = "CODFLAG")
    private String codflag;

    //m.rehman: for NayaPay, adding new fields for document 2.0 <start>
    @Column(name = "SETTLEMENT_DELAY")
    private String settlementdelay;

    @Column(name = "PAGE_COUNT")
    private String pagecount;

    @Column(name = "PAGE_SIZE")
    private String pagesize;

    @Column(name = "TOTAL_COUNT")
    private String totalcount;

    @Column(name = "BILLER_ID")
    private String billerid;

    @Column(name = "BILLER_NAME")
    private String billername;

    @Column(name = "PARTIAL_FLAG")
    private String partialflag;

    //m.rehman: for validating incomcing ip address also save in DB for logging
    @Column(name = "SOURCE_IP")
    private String incomingip;
    //m.rehman: for NayaPay, adding new fields for document 2.0 <end>

    @Column(name = "TEMPBLOCKFLAG")
    private String tempblockflag;

    @Column(name = "CARDEXPIRY")
    private String cardexpiry;

    @Column(name = "ACCTSTATUS")
    private String acctStatus;

    @Column(name = "TOKEN")
    private String token;

    //m.rehman: for biometric operation services <start>
    @Column(name = "TERMINALID")
    private String terminalid;

    @Column(name = "FINGERINDEX")
    private String fingerindex;

    @Column(name = "SESSIONID")
    private String sessionid;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "TEMPLATETYPE")
    private String templatetype;

    @Column(name = "AREA")
    private String area;

    @Column(name = "AMTTRANFEE")
    private String amttranfee;

    @Column(name = "TERMLOC")
    private String termloc;
    //m.rehman: for biometric operation services <end>

    @Column(name = "ORIGINALAPI")
    private String originalapi;

//    @Column(name = "DECRYPTEDOTP")
//    private String decryptedotp;

    @Column(name = "CIPHEREDDATA")
    private String ciphereddata;

    @Column(name = "TRANCURRENCY")
    private String trancurrency; //Raza also add column in DB

    @Column(name = "NEWUSERNAME")
    private String newusername;

    @Column(name = "NEWPASSWORD")
    private String newpassword;

    @Transient
    private boolean isdebitlinkacct;

    @Transient
    private String selfdefinedata;

	@Transient
    private String ismerchantonline;

    @Transient
    private String iswalletaccount;

    @Transient
    private String walletaccount;

    @Column(name = "MERCHANT_AMT")
    private String merchantamount;

    @Column(name = "EMAILADDRESS")
    private String emailaddress;

    @Column(name = "FIRSTNAME")
    private String firstname;

    @Column(name = "MIDDLENAME")
    private String middlename;

    @Column(name = "NATIONALITY")
    private String nationality;

    @Column(name = "LASTNAME")
    private String lastname;

    @Column(name = "DESTMOBILENUMBER")
    private String destmobilenumber;

    @Column(name = "QR_ID")
    private String qrid;

    //@Column(name = "ADVERTISEMENT1")
    @Transient //Raza making transient for performance
    private String advertisement1;

    //@Column(name = "ADVERTISEMENT2")
    @Transient //Raza making transient for performance
    private String advertisement2;

    //@Column(name = "ADVERTISEMENT3")
    @Transient //Raza making transient for performance
    private String advertisement3;

    @Column(name = "CASHOUTPIN")
    private String cashoutpin;

    @Column(name = "CURRENCYRATE")
    private String currencyrate;

    @Column(name = "REQUESTMONEYREF")
    private String requestmoneyref;

    @Column(name = "EXPIRY")
    private String expiry;

    @Column(name = "AUTHORIZATIONNUMBER")
    private String authorizationnumber;

    @Column(name = "BILLAMOUNT")
    private String billamount;

    @Column(name = "WALLETCURRENCY")
    private String walletcurrency;

    @Column(name = "WALLETSTATUS")
    private String walletstatus;

    @Column(name = "PRODUCT")
    private String product;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "DESTACCTID")
    private String destacctid;

    @Column(name = "DESTCURRENCY")
    private String destcurrency;

    @Column(name = "AMOUNTCBILL")
    private String amountcbill;

    @Column(name = "PACKAGECODE")
    private String packagecode;

    @Column(name = "BILLID")
    private String billid;

    @Column(name = "DESTFIRSTNAME")
    private String destfirstname;

    @Column(name = "DESTLASTNAME")
    private String destlastname;

    @Column(name = "DESTCOUNTRY")
    private String destcountry;

    @Column(name = "BILLERUSERNAME")
    private String billerusername;

    @Column(name = "PAYERID")
    private String payerid;

    @Column(name = "PAYERTYPE")
    private String payertype;

    @Column(name = "QUOTATIONEXTID")
    private String quotationextid;

    @Column(name = "QUOTATIONID")
    private String quotationid;

    @Column(name = "TRANSACTIONID")
    private String transactionid;

    @Column(name = "TRANSACTIONEXTID")
    private String transactionextid;

    @Column(name = "IBAN")
    private String iban;

    @Column(name = "SWIFT_BIC_CODE")
    private String swiftbiccode;

    @Column(name = "IFS_CODE")
    private String ifscode;

    @Column(name = "FWD_CHANNELID")
    private String fwdchannelid;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "TERMSCONDITION")
    private String termsandcondition;

    @Column(name = "MUNICIPALITY")
    private String municipality;

    @Column(name = "CLABE")
    private String clabe;

    @Column(name = "CBU")
    private String cbu;

    @Column(name = "CBU_ALIAS")
    private String cbualias;

    @Column(name = "BIKCODE")
    private String bikcode;

    @Column(name = "ABAROUTINGNUMBER")
    private String abaroutingnumber;

    @Column(name = "BSBNUMBER")
    private String bsbnumber;

    @Column(name = "ROUTINGCODE")
    private String routingcode;

    @Column(name = "ENTITYTTID")
    private String entityttid;

    @Column(name = "ACCOUNTTYPE")
    private String accounttype;

    @Column(name = "CREDITACCOUNTNUMBER")
    private String creditaccountnumber;

    @Transient
    private List<CustomerAlias> aliaslist;

    @Transient
    private List<RequestMoney> requestmoneylist;

    @Transient
    private List<CashOut> cashoutlist;

    @Transient
    private List<PendingRemitLogObj> pendingremitloglist;

    @Transient
    private List<MerchantDashboardDataObj> merchantDashboardDataObjList;

    @Transient
    private List<MerchantDashboardDataObj> merchantDashboardDataObjListForMerchant;

    @Transient
    private List<AppContact> contactlist;

    @Transient
    private List<BillPackageObj> billpackages;


    public NFAppWsEntity()
    {
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getBankcode() {
        return bankcode;
    }

    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public String getTranrefnumber() {
        return tranrefnumber;
    }

    public void setTranrefnumber(String tranrefnumber) {
        this.tranrefnumber = tranrefnumber;
    }


    public String getCustomerpicture() {
        return customerpicture;
    }

    public void setCustomerpicture(String customerpicture) {
        this.customerpicture = customerpicture;
    }

    public String getMothername() {
        return mothername;
    }

    public void setMothername(String mothername) {
        this.mothername = mothername;
    }

    public String getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(String dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public String getTransdatetime() {
        return transdatetime;
    }

    public void setTransdatetime(String transdatetime) {
        this.transdatetime = transdatetime;
    }

    public String getPindata() {
        return pindata;
    }

    public void setPindata(String pindata) {
        this.pindata = pindata;
    }

    public String getOldpindata() {
        return oldpindata;
    }

    public void setOldpindata(String oldpindata) {
        this.oldpindata = oldpindata;
    }

    public String getNewpindata() {
        return newpindata;
    }

    public void setNewpindata(String newpindata) {
        this.newpindata = newpindata;
    }

    public String getEncryptkey() {
        return encryptkey;
    }

    public void setEncryptkey(String encryptkey) {
        this.encryptkey = encryptkey;
    }

    public String getAmounttransaction() {
        return amounttransaction;
    }

    public void setAmounttransaction(String amounttransaction) {
        this.amounttransaction = amounttransaction;
    }

    public String getDestaccount() {
        return destaccount;
    }

    public void setDestaccount(String destaccount) {
        this.destaccount = destaccount;
    }

    public String getDestaccountcurrency() {
        return destaccountcurrency;
    }

    public void setDestaccountcurrency(String destaccountcurrency) {
        this.destaccountcurrency = destaccountcurrency;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getRespcode() {
        return respcode;
    }

    public void setRespcode(String respcode) {
        this.respcode = respcode;
        Util.setRespCodeDescription(this);
    }


    public String getAccountcurrency() {
        return accountcurrency;
    }

    public void setAccountcurrency(String accountcurrency) {
        this.accountcurrency = accountcurrency;
    }

    public String getCnicpicturefront() {
        return cnicpicturefront;
    }

    public void setCnicpicturefront(String cnicpictureFront) {
        this.cnicpicturefront = cnicpictureFront;
    }

    public String getCnicpictureback() {
        return cnicpictureback;
    }

    public void setCnicpictureback(String cnicpictureBack) {
        this.cnicpictureback = cnicpictureBack;
    }

    public String getPlaceofbirth() {
        return placeofbirth;
    }

    public void setPlaceofbirth(String placeofbirth) {
        this.placeofbirth = placeofbirth;
    }

    public String getCnicexpiry() {
        return cnicexpiry;
    }

    public void setCnicexpiry(String cnicexpiry) {
        this.cnicexpiry = cnicexpiry;
    }

    public String getFathername() {
        return fathername;
    }

    public void setFathername(String fathername) {
        this.fathername = fathername;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getTsp() {
        return tsp;
    }

    public void setTsp(String tsp) {
        this.tsp = tsp;
    }

    public String getDestbankcode() {
        return destbankcode;
    }

    public void setDestbankcode(String destbankcode) {
        this.destbankcode = destbankcode;
    }

    public String getDestbankid() {
        return destbankid;
    }

    public void setDestbankid(String destCnic) {
        this.destbankid = destCnic;
    }

    public String getSrcchargeamount() {
        return srcchargeamount;
    }

    public void setSrcchargeamount(String srcchargeamount) {
        this.srcchargeamount = srcchargeamount;
    }

    public String getDestchargeamount() {
        return destchargeamount;
    }

    public void setDestchargeamount(String destchargeamount) {
        this.destchargeamount = destchargeamount;
    }

    public String getOrigdataelement() {
        return origdataelement;
    }

    public void setOrigdataelement(String origdataelement) {
        this.origdataelement = origdataelement;
    }

    public String getCustomername() {
        return customername;
    }

    public void setCustomername(String customername) {
        this.customername = customername;
    }

    public String getCorebankcode() {
        return corebankcode;
    }

    public void setCorebankcode(String corebankcode) {
        this.corebankcode = corebankcode;
    }

    public String getCoreaccount() {
        return coreaccount;
    }

    public void setCoreaccount(String coreaccount) {
        this.coreaccount = coreaccount;
    }

    public String getCoreaccountcurrency() {
        return coreaccountcurrency;
    }

    public void setCoreaccountcurrency(String coreaccountcurrency) {
        this.coreaccountcurrency = coreaccountcurrency;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getCardpindata() {
        return cardpindata;
    }

    public void setCardpindata(String cardpindata) {
        this.cardpindata = cardpindata;
    }

    public String getEnableflag() {
        return enableflag;
    }

    public void setEnableflag(String cardenableflag) {
        this.enableflag = cardenableflag;
    }

    public String getMerchantid() {
        return merchantid;
    }

    public void setMerchantid(String merchantid) {
        this.merchantid = merchantid;
    }

    public String getDailylimit() {
        return dailylimit;
    }

    public void setDailylimit(String dailylimit) {
        this.dailylimit = dailylimit;
    }

    public String getMonthlylimit() {
        return monthlylimit;
    }

    public void setMonthlylimit(String monthlylimit) {
        this.monthlylimit = monthlylimit;
    }

    public String getYearlylimit() {
        return yearlylimit;
    }

    public void setYearlylimit(String yearlylimit) {
        this.yearlylimit = yearlylimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public String getAllowed() {
        return allowed;
    }

    public void setAllowed(String allowed) {
        this.allowed = allowed;
    }

    public String getDeletetype() {
        return deletetype;
    }

    public void setDeletetype(String deletetype) {
        this.deletetype = deletetype;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAcctid() {
        return acctid;
    }

    public void setAcctid(String acctid) {
        this.acctid = acctid;
    }

    public String getAcctalias() {
        return acctalias;
    }

    public void setAcctalias(String acctalias) {
        this.acctalias = acctalias;
    }

    public String getIsprimary() {
        return isprimary;
    }

    public void setIsprimary(String isprimary) {
        this.isprimary = isprimary;
    }



    public String getAcctlimit() {
        return getAcctLimit();
    }

    public void setAcctlimit(String acctlimit) {
        this.setAcctLimit(acctlimit);
    }

    public String getAvaillimit() {
        return getAvailLimit();
    }

    public void setAvaillimit(String availlimit) {
        this.setAvailLimit(availlimit);
    }

    public String getAvaillimitfreq() {
        return getAvailLimitfreq();
    }

    public void setAvaillimitfreq(String availlimitfreq) {
        this.setAvailLimitfreq(availlimitfreq);
    }

    public List<AccountLimit> getAccountlimits() {
        return accountlimits;
    }

    public void setAccountlimits(List<AccountLimit> accountlimits) {
        this.accountlimits = accountlimits;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRequesttime() {
        return requesttime;
    }

    public void setRequesttime(String requesttime) {
        this.requesttime = requesttime;
    }

    public String getActivationtime() {
        return activationtime;
    }

    public void setActivationtime(String activationtime) {
        this.activationtime = activationtime;
    }

    public String getDestuserid() {
        return destuserid;
    }

    public void setDestuserid(String destuserid) {
        this.destuserid = destuserid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAdvanceflag() {
        return advanceflag;
    }

    public void setAdvanceflag(String advanceflag) {
        this.advanceflag = advanceflag;
    }

    public String getSecondarynumber() {
        return secondarynumber;
    }

    public void setSecondarynumber(String secondarynumber) {
        this.secondarynumber = secondarynumber;
    }

    public List<LinkedAccountObj> getLinkedaccounts() {
        return linkedaccounts;
    }

    public void setLinkedaccounts(List<LinkedAccountObj> linkedaccounts) {
        this.linkedaccounts = linkedaccounts;
    }

    public List<ProvisionalWallet> getProvisionalwallets() {
        return provisionalwallets;
    }

    public void setProvisionalwallets(List<ProvisionalWallet> provisionalwallets) {
        this.provisionalwallets = provisionalwallets;
    }

    public List<WalletAccount> getAccountlist() {
        return accountlist;
    }

    public void setAccountlist(List<WalletAccount> accountlist) {
        this.accountlist = accountlist;
    }


    public List<TransactionPortal> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionPortal> transactions) {
        this.transactions = transactions;
    }

    public String getAccesstoken() {
        return accesstoken;
    }

    public void setAccesstoken(String usertoken) {
        this.accesstoken = usertoken;
    }

    public String getInoutfilter() {
        return inoutfilter;
    }

    public void setInoutfilter(String inoutfilter) {
        this.inoutfilter = inoutfilter;
    }

    public String getTypefilter() {
        return typefilter;
    }

    public void setTypefilter(String typefilter) {
        this.typefilter = typefilter;
    }

    public String getSearchtext() {
        return searchtext;
    }

    public void setSearchtext(String searchtext) {
        this.searchtext = searchtext;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBankid() {
        return bankid;
    }

    public void setBankid(String bankid) {
        this.bankid = bankid;
    }

//    public String getGpslatitude() {
//        return gpslatitude;
//    }
//
//    public void setGpslatitude(String gpslatitude) {
//        this.gpslatitude = gpslatitude;
//    }
//
//    public String getGpslongitude() {
//        return gpslongitude;
//    }
//
//    public void setGpslongitude(String gpslongitude) {
//        this.gpslongitude = gpslongitude;
//    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentId) {
        this.parentid = parentId;
    }

    public String getMerchantname() {
        return merchantname;
    }

    public void setMerchantname(String merchantname) {
        this.merchantname = merchantname;
    }

    public String getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(String categoryId) {
        this.categoryid = categoryId;
    }

    public String getTrustedflag() {
        return trustedflag;
    }

    public void setTrustedflag(String trustedFlag) {
        this.trustedflag = trustedFlag;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phoneNumber) {
        this.phonenumber = phoneNumber;
    }

    public String getTransactionlimit() {
        return transactionlimit;
    }

    public void setTransactionlimit(String transactionLimit) {
        this.transactionlimit = transactionLimit;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryName) {
        this.categoryname = categoryName;
    }

    public String getMerchantstate() {
        return merchantstate;
    }

    public void setMerchantstate(String merchantState) {
        this.merchantstate = merchantState;
    }

    public String getMerchantenabled() {
        return merchantenabled;
    }

    public void setMerchantenabled(String merchantEnabled) {
        this.merchantenabled = merchantEnabled;
    }

    public String getMerchantblocked() {
        return merchantblocked;
    }

    public void setMerchantblocked(String merchantBlocked) {
        this.merchantblocked = merchantBlocked;
    }

    public String getMinimumamount() {
        return minimumamount;
    }

    public void setMinimumamount(String minimumAmount) {
        this.minimumamount = minimumAmount;
    }

    public String getMaximumamount() {
        return maximumamount;
    }

    public void setMaximumamount(String maximumAmount) {
        this.maximumamount = maximumAmount;
    }

    public String getSourcechargetype() {
        return sourcechargetype;
    }

    public void setSourcechargetype(String sourceChargeType) {
        this.sourcechargetype = sourceChargeType;
    }

    public String getDestinationchargetype() {
        return destinationchargetype;
    }

    public void setDestinationchargetype(String destinationChargeType) {
        this.destinationchargetype = destinationChargeType;
    }

    public String getConsumerno() {
        return consumerno;
    }

    public void setConsumerno(String consumerNo) {
        this.consumerno = consumerNo;
    }

    public String getUtilcompanyid() {
        return utilcompanyid;
    }

    public void setUtilcompanyid(String utilCompanyId) {
        this.utilcompanyid = utilCompanyId;
    }

    public String getConsumerdetail() {
        return consumerdetail;
    }

    public void setConsumerdetail(String consumerDetail) {
        this.consumerdetail = consumerDetail;
    }

    public String getBillstatus() {
        return billstatus;
    }

    public void setBillstatus(String billStatus) {
        this.billstatus = billStatus;
    }

    public String getDuedate() {
        return duedate;
    }

    public void setDuedate(String dueDate) {
        this.duedate = dueDate;
    }

    public String getAmtwithinduedate() {
        return amtwithinduedate;
    }

    public void setAmtwithinduedate(String amtWithinDueDate) {
        this.amtwithinduedate = amtWithinDueDate;
    }

    public String getAmtafterduedate() {
        return amtafterduedate;
    }

    public void setAmtafterduedate(String amtAfterDueDate) {
        this.amtafterduedate = amtAfterDueDate;
    }

    public String getBillingmonth() {
        return billingmonth;
    }

    public void setBillingmonth(String billingMonth) {
        this.billingmonth = billingMonth;
    }

    public String getDatepaid() {
        return datepaid;
    }

    public void setDatepaid(String datePaid) {
        this.datepaid = datePaid;
    }

    public String getAmtpaid() {
        return amtpaid;
    }

    public void setAmtpaid(String amtPaid) {
        this.amtpaid = amtPaid;
    }

    public String getTranauthid() {
        return tranauthid;
    }

    public void setTranauthid(String tranAuthId) {
        this.tranauthid = tranAuthId;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public String getIdentificationno() {
        return identificationno;
    }

    public void setIdentificationno(String identificationNo) {
        this.identificationno = identificationNo;
    }

    public String getPing() {
        return ping;
    }

    public void setPing(String ping) {
        this.ping = ping;
    }

//    public String getTrantype() {
//        return trantype;
//    }
//
//    public void setTrantype(String bankTranType) {
//        this.trantype = bankTranType;
//    }

    public String getDestusername() {
        return destusername;
    }

    public void setDestusername(String destusername) {
        this.destusername = destusername;
    }

    public String getAgentid() {
        return agentid;
    }

    public void setAgentid(String agentid) {
        this.agentid = agentid;
    }

    public String getReferencenumber() {
        return referencenumber;
    }

    public void setReferencenumber(String referencenumber) {
        this.referencenumber = referencenumber;
    }

    public String getInvoiceid() {
        return invoiceid;
    }

    public void setInvoiceid(String invoiceid) {
        this.invoiceid = invoiceid;
    }

    public String getVerifiedflag() {
        return verifiedflag;
    }

    public void setVerifiedflag(String verifiedflag) {
        this.verifiedflag = verifiedflag;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getBanktxnflag() {
        return banktxnflag;
    }

    public void setBanktxnflag(String banktxnflag) {
        this.banktxnflag = banktxnflag;
    }

    public String getBlockedflag() {
        return blockedflag;
    }

    public void setBlockedflag(String blockedflag) {
        this.blockedflag = blockedflag;
    }

    public String getBillertxnid() {
        return billertxnid;
    }

    public void setBillertxnid(String billertxnid) {
        this.billertxnid = billertxnid;
    }

    //m.rehman: for NayaPay, for merchant transaction list
    public List<MerchantTransaction> getMerchantTransactions() {
        return merchantTransactions;
    }

    public void setMerchantTransactions(List<MerchantTransaction> merchantTransactions) {
        this.merchantTransactions = merchantTransactions;
    }

    public List<UserTransaction> getUsertransactions() {
        return usertransactions;
    }

    public void setUsertransactions(List<UserTransaction> usertransactions) {
        this.usertransactions = usertransactions;
    }

    public String getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(String creationdate) {
        this.creationdate = creationdate;
    }

    public TransactionPortal getTransactionDetail() {
        return transactionDetail;
    }

    public void setTransactionDetail(TransactionPortal transactionDetail) {
        this.transactionDetail = transactionDetail;
    }

    public String getBankMnemonic() {
        return bankMnemonic;
    }

    public void setBankMnemonic(String bankMnemonic) {
        this.bankMnemonic = bankMnemonic;
    }

    public NFAppWsEntity copy() {
        NFAppWsEntity newAppWsEntity = new NFAppWsEntity();
        newAppWsEntity.setServicename(this.getServicename());
        newAppWsEntity.setMobilenumber(this.getMobilenumber());
        newAppWsEntity.setCnic(this.getCnic());
        newAppWsEntity.setCustomername(this.getCustomername());
        newAppWsEntity.setCnicexpiry(this.getCnicexpiry());
        newAppWsEntity.setBankcode(this.getBankcode());
        newAppWsEntity.setDestbankcode(this.getDestbankcode());
        newAppWsEntity.setBankname(this.getBankname());
        newAppWsEntity.setAccountnumber(this.getAccountnumber());
        newAppWsEntity.setAccountcurrency(this.getAccountcurrency());
        newAppWsEntity.setTranrefnumber(this.getTranrefnumber());
        newAppWsEntity.setCnicpicturefront(this.getCnicpicturefront());
        newAppWsEntity.setCnicpictureback(this.getCnicpictureback());
        newAppWsEntity.setCustomerpicture(this.getCustomerpicture());
        newAppWsEntity.setMothername(this.getMothername());
        newAppWsEntity.setDateofbirth(this.getDateofbirth());
        newAppWsEntity.setTransdatetime(this.getTransdatetime());
        newAppWsEntity.setPindata(this.getPindata());
        newAppWsEntity.setOldpindata(this.getOldpindata());
        newAppWsEntity.setNewpindata(this.getNewpindata());
        newAppWsEntity.setEncryptkey(this.getEncryptkey());
        newAppWsEntity.setAmounttransaction(this.getAmounttransaction());
        newAppWsEntity.setSrcchargeamount(this.getSrcchargeamount());
        newAppWsEntity.setDestchargeamount(this.getDestchargeamount());
        newAppWsEntity.setDestaccount(this.getDestaccount());
        newAppWsEntity.setDestaccountcurrency(this.getDestaccountcurrency());
        newAppWsEntity.setOtp(this.getOtp());
        newAppWsEntity.setBiometricdata(this.getBiometricdata());
        newAppWsEntity.setRespcode(this.getRespcode());
        newAppWsEntity.setPlaceofbirth(this.getPlaceofbirth());
        newAppWsEntity.setFathername(this.getFathername());
        newAppWsEntity.setProvince(this.getProvince());
        newAppWsEntity.setTsp(this.getTsp());
        newAppWsEntity.setDestbankid(this.getDestbankid());
        newAppWsEntity.setOrigdataelement(this.getOrigdataelement());
        newAppWsEntity.setAtmid(this.getAtmid());
        newAppWsEntity.setCorebankcode(this.getCorebankcode());
        newAppWsEntity.setCoreaccount(this.getCoreaccount());
        newAppWsEntity.setCoreaccountcurrency(this.getCoreaccountcurrency());
        newAppWsEntity.setCardnumber(this.getCardnumber());
        newAppWsEntity.setCardpindata(this.getCardpindata());
        newAppWsEntity.setEnableflag(this.getEnableflag());
        newAppWsEntity.setMerchantid(this.getMerchantid());
        newAppWsEntity.setDailylimit(this.getDailylimit());
        newAppWsEntity.setMonthlylimit(this.getMonthlylimit());
        newAppWsEntity.setYearlylimit(this.getYearlylimit());
        newAppWsEntity.setStatus(this.getStatus());
        newAppWsEntity.setStan(this.getStan());
        newAppWsEntity.setRrn(this.getRrn());
        newAppWsEntity.setUserid(this.getUserid());
        newAppWsEntity.setChannelid(this.getChannelid());
        newAppWsEntity.setAllowed(this.getAllowed());
        newAppWsEntity.setDeletetype(this.getDeletetype());
        newAppWsEntity.setComments(this.getComments());
        newAppWsEntity.setAcctid(this.getAcctid());
        newAppWsEntity.setAcctalias(this.getAcctalias());
        newAppWsEntity.setIsprimary(this.getIsprimary());
        newAppWsEntity.setAccountbalance(this.getAccountbalance());
        newAppWsEntity.setAcctlimit(this.getAcctlimit());
        newAppWsEntity.setAvaillimit(this.getAvaillimit());
        newAppWsEntity.setAvaillimitfreq(this.getAvaillimitfreq());
        newAppWsEntity.setState(this.getState());
        newAppWsEntity.setRequesttime(this.getRequesttime());
        newAppWsEntity.setActivationtime(this.getActivationtime());
        newAppWsEntity.setDestuserid(this.getDestuserid());
        newAppWsEntity.setAddress(this.getAddress());
        newAppWsEntity.setCity(this.getCity());
        newAppWsEntity.setCountry(this.getCountry());
        newAppWsEntity.setAdvanceflag(this.getAdvanceflag());
        newAppWsEntity.setSecondarynumber(this.getSecondarynumber());
        newAppWsEntity.setAccesstoken(this.getAccesstoken());
        newAppWsEntity.setInoutfilter(this.getInoutfilter());
        newAppWsEntity.setTypefilter(this.getTypefilter());
        newAppWsEntity.setSearchtext(this.getSearchtext());
        newAppWsEntity.setUsername(this.getUsername());
        newAppWsEntity.setBankid(this.getBankid());
        newAppWsEntity.setParentid(this.getParentid());
        newAppWsEntity.setMerchantname(this.getMerchantname());
        newAppWsEntity.setCategoryid(this.getCategoryid());
        newAppWsEntity.setTrustedflag(this.getTrustedflag());
        newAppWsEntity.setPhonenumber(this.getPhonenumber());
        newAppWsEntity.setTransactionlimit(this.getTransactionlimit());
        newAppWsEntity.setCategoryname(this.getCategoryname());
        newAppWsEntity.setMerchantstate(this.getMerchantstate());
        newAppWsEntity.setMerchantenabled(this.getMerchantenabled());
        newAppWsEntity.setMerchantblocked(this.getMerchantblocked());
        newAppWsEntity.setMinimumamount(this.getMinimumamount());
        newAppWsEntity.setMaximumamount(this.getMaximumamount());
        newAppWsEntity.setSourcechargetype(this.getSourcechargetype());
        newAppWsEntity.setDestinationchargetype(this.getDestinationchargetype());
        newAppWsEntity.setConsumerno(this.getConsumerno());
        newAppWsEntity.setUtilcompanyid(this.getUtilcompanyid());
        newAppWsEntity.setConsumerdetail(this.getConsumerdetail());
        newAppWsEntity.setBillstatus(this.getBillstatus());
        newAppWsEntity.setDuedate(this.getDuedate());
        newAppWsEntity.setAmtwithinduedate(this.getAmtwithinduedate());
        newAppWsEntity.setAmtafterduedate(this.getAmtafterduedate());
        newAppWsEntity.setBillingmonth(this.getBillingmonth());
        newAppWsEntity.setDatepaid(this.getDatepaid());
        newAppWsEntity.setAmtpaid(this.getAmtpaid());
        newAppWsEntity.setTranauthid(this.getTranauthid());
        newAppWsEntity.setReserved(this.getReserved());
        newAppWsEntity.setIdentificationno(this.getIdentificationno());
        newAppWsEntity.setPing(this.getPing());
        newAppWsEntity.setTrantype(this.getTrantype());
        newAppWsEntity.setDestusername(this.getDestusername());
        newAppWsEntity.setAgentid(this.getAgentid());
        newAppWsEntity.setReferencenumber(this.getReferencenumber());
        newAppWsEntity.setInvoiceid(this.getInvoiceid());
        newAppWsEntity.setVerifiedflag(this.getVerifiedflag());
        newAppWsEntity.setStartdate(this.getStartdate());
        newAppWsEntity.setEnddate(this.getEnddate());
        newAppWsEntity.setBanktxnflag(this.getBanktxnflag());
        newAppWsEntity.setBlockedflag(this.getBlockedflag());
        newAppWsEntity.setMerchantenabled(this.getMerchantenabled());
        newAppWsEntity.setBillertxnid(this.getBillertxnid());
        newAppWsEntity.setBillerrespcode(this.getBillerrespcode());
        newAppWsEntity.setBillerrespcodedesc(this.getBillerrespcodedesc());
        newAppWsEntity.setCreationdate(this.getCreationdate());
        newAppWsEntity.setAccountlimits(this.getAccountlimits());
        newAppWsEntity.setLinkedaccounts(this.getLinkedaccounts());
        newAppWsEntity.setProvisionalwallets(this.getProvisionalwallets());
        newAppWsEntity.setAccountlist(this.getAccountlist());
        newAppWsEntity.setTransactions(this.getTransactions());
        newAppWsEntity.setUsertransactions(this.getUsertransactions());
        newAppWsEntity.setMerchantenabled(this.getMerchantenabled());
        newAppWsEntity.setMerchantTransactions(this.getMerchantTransactions());
        newAppWsEntity.setTransactionDetail(this.getTransactionDetail());
        newAppWsEntity.setCurrency(this.getCurrency());
        newAppWsEntity.setBank(this.getBank());
        newAppWsEntity.setDestbank(this.getDestbank());
        newAppWsEntity.setCorebank(this.getCorebank());
        newAppWsEntity.setDestcurrency(this.getDestcurrency());
        newAppWsEntity.setCorecurrency(this.getCorecurrency());
        newAppWsEntity.setAvailablebalance(this.getAvailablebalance());
        newAppWsEntity.setBillerid(this.getBillerid());
        newAppWsEntity.setBillername(this.getBillername());
        newAppWsEntity.setConsumerno(this.getConsumerno());
        newAppWsEntity.setAcctStatus(this.getAcctStatus());
        newAppWsEntity.setToken(this.getToken());
        newAppWsEntity.setTerminalid(this.getTerminalid());
        newAppWsEntity.setFingerindex(this.getFingerindex());
        newAppWsEntity.setSessionid(this.getSessionid());
        newAppWsEntity.setMessage(this.getMessage());
        newAppWsEntity.setReserved2(this.getReserved2());
        newAppWsEntity.setReserved3(this.getReserved3());
        newAppWsEntity.setRespcodedesc(this.getRespcodedesc());
        newAppWsEntity.setBankcode(this.getBankcode());
        newAppWsEntity.setDecryptedotp(this.getDecryptedotp());
        newAppWsEntity.setCiphereddata(this.getCiphereddata());
        newAppWsEntity.setAmttranfee(this.getAmttranfee());
        newAppWsEntity.setLastname(this.getLastname());
        newAppWsEntity.setFirstname(this.getFirstname());
        newAppWsEntity.setEmailaddress(this.getEmailaddress());
        newAppWsEntity.setDestacctid(this.getDestacctid());
        newAppWsEntity.setAmountcbill(this.getAmountcbill());
        newAppWsEntity.setId(null);

        return newAppWsEntity;
    }

    public String getAvailablebalance() {
        return availablebalance;
    }

    public void setAvailablebalance(String availablebalance) {
        this.availablebalance = availablebalance;
    }

    @Override
    public String getSecretquestion1() {
        return secretquestion1;
    }

    @Override
    public void setSecretquestion1(String secretquestion1) {
        this.secretquestion1 = secretquestion1;
    }

    @Override
    public String getSecretquestionanswer1() {
        return secretquestionanswer1;
    }

    @Override
    public void setSecretquestionanswer1(String secretquestionanswer1) {
        this.secretquestionanswer1 = secretquestionanswer1;
    }

    @Override
    public String getSecretquestion2() {
        return secretquestion2;
    }

    @Override
    public void setSecretquestion2(String secretquestion2) {
        this.secretquestion2 = secretquestion2;
    }

    @Override
    public String getSecretquestionanswer2() {
        return secretquestionanswer2;
    }

    @Override
    public void setSecretquestionanswer2(String secretquestionanswer2) {
        this.secretquestionanswer2 = secretquestionanswer2;
    }

    @Override
    public NFSecurityParams getNfsecurityparams() {
        return nfsecurityparams;
    }

    @Override
    public void setNfsecurityparams(NFSecurityParams securityparams) {
        this.nfsecurityparams = securityparams;
    }

    public String getCodflag() {
        return codflag;
    }

    public void setCodflag(String codflag) {
        this.codflag = codflag;
    }

    @Override
    public String getTotalcount() {
        return totalcount;
    }

    @Override
    public void setTotalcount(String totalcount) {
        this.totalcount = totalcount;
    }

    @Override
    public String getSettlementdelay() {
        return settlementdelay;
    }

    @Override
    public void setSettlementdelay(String settlementdelay) {
        this.settlementdelay = settlementdelay;
    }

    @Override
    public String getPagecount() {
        return pagecount;
    }

    @Override
    public void setPagecount(String pagecount) {
        this.pagecount = pagecount;
    }

    @Override
    public String getPagesize() {
        return pagesize;
    }

    @Override
    public void setPagesize(String pagesize) {
        this.pagesize = pagesize;
    }

    @Override
    public String getBillerid() {
        return billerid;
    }

    @Override
    public void setBillerid(String billerid) {
        this.billerid = billerid;
    }

    @Override
    public String getBillername() {
        return billername;
    }

    @Override
    public void setBillername(String billername) {
        this.billername = billername;
    }

    @Override
    public String getPartialflag() {
        return partialflag;
    }

    @Override
    public void setPartialflag(String partialflag) {
        this.partialflag = partialflag;
    }

    //m.rehman: for validating incoming ip address
    public String getIncomingip() {
        return incomingip;
    }

    public void setIncomingip(String incomingip) {
        this.incomingip = incomingip;
    }

    public String getTempblockflag() {
        return tempblockflag;
    }

    public void setTempblockflag(String tempblockflag) {
        this.tempblockflag = tempblockflag;
    }

    @Override
    public String getCardexpiry() {
        return cardexpiry;
    }

    @Override
    public void setCardexpiry(String cardexpiry) {
        this.cardexpiry = cardexpiry;
    }

    public String getCardNoLastDigits() {
        return cardNoLastDigits;
    }

    public void setCardNoLastDigits(String cardNoLastDigits) {
        this.cardNoLastDigits = cardNoLastDigits;
    }

    public String getAcctStatus() {
        return acctStatus;
    }

    public void setAcctStatus(String isDLinkAcctExist) {
        this.acctStatus = isDLinkAcctExist;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTerminalid() {
        return terminalid;
    }

    public void setTerminalid(String terminalid) {
        this.terminalid = terminalid;
    }

    public String getFingerindex() {
        return fingerindex;
    }

    public void setFingerindex(String fingerindex) {
        this.fingerindex = fingerindex;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    public String getTemplatetype() {
        return templatetype;
    }

    public void setTemplatetype(String templatetype) {
        this.templatetype = templatetype;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAmttranfee() {
        return amttranfee;
    }

    public void setAmttranfee(String amttranfee) {
        this.amttranfee = amttranfee;
    }

    public String getTermloc() {
        return termloc;
    }

    public void setTermloc(String termloc) {
        this.termloc = termloc;
    }


    public boolean getIsdebitlinkacct() {
        return isIsdebitlinkacct();
    }

    public void setIsdebitlinkacct(boolean isdebitlinkacct) {
        this.isdebitlinkacct = isdebitlinkacct;
    }

    public String getOriginalapi() {
        return originalapi;
    }

    public void setOriginalapi(String origialapi) {
        this.originalapi = origialapi;
    }

    public String getIsmerchantonline() {
        return ismerchantonline;
    }

    public void setIsmerchantonline(String ismerchantonline) {
        this.ismerchantonline = ismerchantonline;
    }

    public String getIswalletaccount() {
        return iswalletaccount;
    }
    public void setIswalletaccount(String iswalletaccount) {
        this.iswalletaccount = iswalletaccount;
    }

    public String getWalletaccount() {
        return walletaccount;
    }

    public void setWalletaccount(String walletaccount) {
        this.walletaccount = walletaccount;
    }

    public String getMerchantamount() {
        return merchantamount;
    }

    public void setMerchantamount(String merchantamount) {
        this.merchantamount = merchantamount;
    }
	
    public String getSelfdefinedata() {
        return selfdefinedata;
    }

    public void setSelfdefinedata(String selfdefinedata) {
        this.selfdefinedata = selfdefinedata;
    }

//    @Override
//    public String getDecryptedotp() {
//        return decryptedotp;
//    }

//    @Override
//    public void setDecryptedotp(String decryptedotp) {
//        this.decryptedotp = decryptedotp;
//    }

    @Override
    public String getCiphereddata() {
        return ciphereddata;
    }

    @Override
    public void setCiphereddata(String ciphereddata) {
        this.ciphereddata = ciphereddata;
    }

    @Override
    public String getTrancurrency() {
        return trancurrency;
    }

    @Override
    public void setTrancurrency(String trancurrency) {
        this.trancurrency = trancurrency;
    }

    public String getEmailaddress() {
        return emailaddress;
    }

    public void setEmailaddress(String email) {
        this.emailaddress = email;
    }


    @Override
    public String getFirstname() {
        return firstname;
    }

    @Override
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @Override
    public String getLastname() {
        return lastname;
    }

    @Override
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getDestmobilenumber() {
        return destmobilenumber;
    }

    public void setDestmobilenumber(String destmobilenumber) {
        this.destmobilenumber = destmobilenumber;
    }

    public List<AppContact> getContactlist() {
        return contactlist;
    }

    public void setContactlist(List<AppContact> contactlist) {
        this.contactlist = contactlist;
    }

    public String getAcctLimit() {
        return acctLimit;
    }

    public void setAcctLimit(String acctLimit) {
        this.acctLimit = acctLimit;
    }

    public String getAvailLimit() {
        return availLimit;
    }

    public void setAvailLimit(String availLimit) {
        this.availLimit = availLimit;
    }

    public String getAvailLimitfreq() {
        return availLimitfreq;
    }

    public void setAvailLimitfreq(String availLimitfreq) {
        this.availLimitfreq = availLimitfreq;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isIsdebitlinkacct() {
        return isdebitlinkacct;
    }

    @Override
    public String getBiometricdata() {
        return biometricdata;
    }

    @Override
    public void setBiometricdata(String biometricdata) {
        this.biometricdata = biometricdata;
    }

    @Override
    public String getNewusername() {
        return newusername;
    }

    @Override
    public void setNewusername(String newusername) {
        this.newusername = newusername;
    }

    @Override
    public String getNewpassword() {
        return newpassword;
    }

    @Override
    public void setNewpassword(String newpassword) {
        this.newpassword = newpassword;
    }

    @Override
    public String getQrid() {
        return qrid;
    }

    @Override
    public void setQrid(String qrid) {
        this.qrid = qrid;
    }

    @Override
    public String getAdvertisement1() {
        return advertisement1;
    }

    @Override
    public void setAdvertisement1(String advertisement1) {
        this.advertisement1 = advertisement1;
    }

    @Override
    public String getAdvertisement2() {
        return advertisement2;
    }

    @Override
    public void setAdvertisement2(String advertisement2) {
        this.advertisement2 = advertisement2;
    }

    @Override
    public String getAdvertisement3() {
        return advertisement3;
    }

    @Override
    public void setAdvertisement3(String advertisement3) {
        this.advertisement3 = advertisement3;
    }

    @Override
    public String getCashoutpin() {
        return cashoutpin;
    }

    @Override
    public void setCashoutpin(String cashoutpin) {
        this.cashoutpin = cashoutpin;
    }

    @Override
    public String getCurrencyrate() {
        return currencyrate;
    }

    @Override
    public void setCurrencyrate(String currencyrate) {
        this.currencyrate = currencyrate;
    }

    @Override
    public String getRequestmoneyref() {
        return requestmoneyref;
    }

    @Override
    public void setRequestmoneyref(String requestmoneyref) {
        this.requestmoneyref = requestmoneyref;
    }

    @Override
    public String getExpiry() {
        return expiry;
    }

    @Override
    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    @Override
    public String getAuthorizationnumber() {
        return authorizationnumber;
    }

    @Override
    public void setAuthorizationnumber(String authorizationnumber) {
        this.authorizationnumber = authorizationnumber;
    }

    @Override
    public String getBillamount() {
        return billamount;
    }

    @Override
    public void setBillamount(String billamount) {
        this.billamount = billamount;
    }

    @Override
    public String getWalletcurrency() {
        return walletcurrency;
    }

    @Override
    public void setWalletcurrency(String walletcurrency) {
        this.walletcurrency = walletcurrency;
    }

    @Override
    public String getWalletstatus() {
        return walletstatus;
    }

    @Override
    public void setWalletstatus(String walletstatus) {
        this.walletstatus = walletstatus;
    }

    @Override
    public List<CustomerAlias> getAliaslist() {
        return aliaslist;
    }

    @Override
    public void setAliaslist(List<CustomerAlias> aliaslist) {
        this.aliaslist = aliaslist;
    }

    @Override
    public List<RequestMoney> getRequestmoneylist() {
        return requestmoneylist;
    }

    @Override
    public void setRequestmoneylist(List<RequestMoney> requestmoneylist) {
        this.requestmoneylist = requestmoneylist;
    }

    @Override
    public List<CashOut> getCashoutlist() {
        return cashoutlist;
    }

    @Override
    public void setCashoutlist(List<CashOut> cashoutlist) {
        this.cashoutlist = cashoutlist;
    }

    @Override
    public String getProduct() {
        return product;
    }

    @Override
    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String getAccountbalance() {
        return accountbalance;
    }

    @Override
    public void setAccountbalance(String accountbalance) {
        this.accountbalance = accountbalance;
    }

    @Override
    public String getDestacctid() {
        return destacctid;
    }

    @Override
    public void setDestacctid(String destacctid) {
        this.destacctid = destacctid;
    }

    @Override
    public String getDestcurrency() {
        return destcurrency;
    }

    @Override
    public void setDestcurrency(String destcurrency) {
        this.destcurrency = destcurrency;
    }

    @Override
    public String getAmountcbill() {
        return amountcbill;
    }

    @Override
    public void setAmountcbill(String amountcbill) {
        this.amountcbill = amountcbill;
    }

    @Override
    public List<BillPackageObj> getBillpackages() {
        return billpackages;
    }

    @Override
    public void setBillpackages(List<BillPackageObj> billpackages) {
        this.billpackages = billpackages;
    }

    @Override
    public String getPackagecode() {
        return packagecode;
    }

    @Override
    public void setPackagecode(String packagecode) {
        this.packagecode = packagecode;
    }

    @Override
    public String getBillid() {
        return billid;
    }

    @Override
    public void setBillid(String billid) {
        this.billid = billid;
    }

    @Override
    public String getDestfirstname() {
        return destfirstname;
    }

    @Override
    public void setDestfirstname(String destfirstname) {
        this.destfirstname = destfirstname;
    }

    @Override
    public String getDestlastname() {
        return destlastname;
    }

    @Override
    public void setDestlastname(String destlastname) {
        this.destlastname = destlastname;
    }

    @Override
    public String getDestcountry() {
        return destcountry;
    }

    @Override
    public void setDestcountry(String destcountry) {
        this.destcountry = destcountry;
    }

    @Override
    public String getBillerusername() {
        return billerusername;
    }

    @Override
    public void setBillerusername(String billerusername) {
        this.billerusername = billerusername;
    }

    @Override
    public String getPayerid() {
        return payerid;
    }

    @Override
    public void setPayerid(String payerid) {
        this.payerid = payerid;
    }

    @Override
    public String getQuotationextid() {
        return quotationextid;
    }

    @Override
    public void setQuotationextid(String quotationextid) {
        this.quotationextid = quotationextid;
    }

    @Override
    public String getQuotationid() {
        return quotationid;
    }

    @Override
    public void setQuotationid(String quotationid) {
        this.quotationid = quotationid;
    }

    @Override
    public String getTransactionid() {
        return transactionid;
    }

    @Override
    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }

    @Override
    public String getTransactionextid() {
        return transactionextid;
    }

    @Override
    public void setTransactionextid(String transactionextid) {
        this.transactionextid = transactionextid;
    }

    @Override
    public String getIban() {
        return iban;
    }

    @Override
    public void setIban(String iban) {
        this.iban = iban;
    }

    @Override
    public String getSwiftbiccode() {
        return swiftbiccode;
    }

    @Override
    public void setSwiftbiccode(String swiftbiccode) {
        this.swiftbiccode = swiftbiccode;
    }

    @Override
    public String getIfscode() {
        return ifscode;
    }

    @Override
    public void setIfscode(String ifscode) {
        this.ifscode = ifscode;
    }

    @Override
    public String getFwdchannelid() {
        return fwdchannelid;
    }

    @Override
    public void setFwdchannelid(String fwdchannelid) {
        this.fwdchannelid = fwdchannelid;
    }

    @Override
    public String getGender() {
        return gender;
    }

    @Override
    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String getTermsandcondition() {
        return termsandcondition;
    }

    @Override
    public void setTermsandcondition(String termsandcondition) {
        this.termsandcondition = termsandcondition;
    }

    @Override
    public String getMiddlename() {
        return middlename;
    }

    @Override
    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }
	
    @Override
    public String getMunicipality() {
        return municipality;
    }

    @Override
    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public List<PendingRemitLogObj> getPendingremitloglist() {
        return pendingremitloglist;
    }

    public void setPendingremitloglist(List<PendingRemitLogObj> pendingremitloglist) {
        this.pendingremitloglist = pendingremitloglist;
    }

    public String getPayertype() {
        return payertype;
    }

    public void setPayertype(String payertype) {
        this.payertype = payertype;
    }

    public List<MerchantDashboardDataObj> getMerchantDashboardDataObjList() {
        return merchantDashboardDataObjList;
    }

    public void setMerchantDashboardDataObjList(List<MerchantDashboardDataObj> merchantDashboardDataObjList) {
        this.merchantDashboardDataObjList = merchantDashboardDataObjList;
    }

    @Override
    public List<MerchantDashboardDataObj> getMerchantDashboardDataObjListForMerchant() {
        return merchantDashboardDataObjListForMerchant;
    }

    @Override
    public void setMerchantDashboardDataObjListForMerchant(List<MerchantDashboardDataObj> merchantDashboardDataObjListForMerchant) {
        this.merchantDashboardDataObjListForMerchant = merchantDashboardDataObjListForMerchant;
    }

    @Override
    public String getClabe() {
        return clabe;
    }

    @Override
    public void setClabe(String clabe) {
        this.clabe = clabe;
    }

    @Override
    public String getCbu() {
        return cbu;
    }

    @Override
    public void setCbu(String cbu) {
        this.cbu = cbu;
    }

    @Override
    public String getCbualias() {
        return cbualias;
    }

    @Override
    public void setCbualias(String cbualias) {
        this.cbualias = cbualias;
    }

    @Override
    public String getBikcode() {
        return bikcode;
    }

    @Override
    public void setBikcode(String bikcode) {
        this.bikcode = bikcode;
    }

    @Override
    public String getAbaroutingnumber() {
        return abaroutingnumber;
    }

    @Override
    public void setAbaroutingnumber(String abaroutingnumber) {
        this.abaroutingnumber = abaroutingnumber;
    }

    @Override
    public String getBsbnumber() {
        return bsbnumber;
    }

    @Override
    public void setBsbnumber(String bsbnumber) {
        this.bsbnumber = bsbnumber;
    }

    @Override
    public String getRoutingcode() {
        return routingcode;
    }

    @Override
    public void setRoutingcode(String routingcode) {
        this.routingcode = routingcode;
    }

    @Override
    public String getEntityttid() {
        return entityttid;
    }

    @Override
    public void setEntityttid(String entityttid) {
        this.entityttid = entityttid;
    }

    @Override
    public String getAccounttype() {
        return accounttype;
    }

    @Override
    public void setAccounttype(String accounttype) {
        this.accounttype = accounttype;
    }

    @Override
    public String getCreditaccountnumber() {
        return creditaccountnumber;
    }

    @Override
    public void setCreditaccountnumber(String creditaccountnumber) {
        this.creditaccountnumber = creditaccountnumber;
    }

    @Override
    public String getNewmobilenumber() {
        return newmobilenumber;
    }

    @Override
    public void setNewmobilenumber(String newmobilenumber) {
        this.newmobilenumber = newmobilenumber;
    }
}
