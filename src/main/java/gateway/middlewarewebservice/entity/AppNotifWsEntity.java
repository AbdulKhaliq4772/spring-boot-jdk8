package gateway.middlewarewebservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import pk.vaulsys.apigateway.protocols.webservice.base.entity.WebServiceEntity;
import pk.vaulsys.apigateway.protocols.webservice.base.model.TransactionPortal;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.model.*;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WSEncryptionUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Raza on 10-Sep-25. //Segrating to keep App Payload Light weight i.e. App + MultiLlanguage... using same WS_LOG though
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "WS_LOG")
public class AppNotifWsEntity extends WebServiceEntity {

    private static final Logger logger = LogManager.getLogger(AppNotifWsEntity.class);

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

    @Column(name = "DESTBANKCODE")
    private String destbankcode;

    @Column(name = "BANKNAME")
    private String bankname;

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

    //Muhammad Hamza: Adding for Mobile Banking Feature -- Start
    @Transient
    private Boolean isemailverified;

    @Column(name = "BANKCODE")
    private String bankcode;

    @Column(name = "ACCOUNTNUMBER")
    private String accountnumber;

    @Column(name = "ACCOUNTCURRENCY")
    private String accountcurrency;

    @Transient
    private List<AccountOppositionLevel> customeropposition;

    @Transient
    private List<AccountOppositionLevel> accountopposition;

    @Transient
    private List<String> listofemailaddress;

    @Transient
    private String biccode;

    @Transient
    private String accountkey;

    @Transient
    private String accountholdername;

    @Transient
    private String accountholderaddress;

    @Transient
    private Boolean iscmsaccoutlinked;

    @Transient
    private String rmemail;

    @Transient
    private String rmemailsource;

    @Transient
    private String clientid;

    @Transient
    private String subject;

    @Transient
    private List<ContactRMSubjectObj> subjectlist;

    @Transient
    private String contactpreference;

    @Transient
    private List<TransactionReasons> transactionReasons;

    @Transient
    private String transactiontype;

    @Transient
    private String sourceaccount;

    @Transient
    private String sourcetitle;

    @Transient
    private String beneficiarytype;

    @Transient
    private String beneficiaryname;

    @Transient
    private String destaccounttitle;

    @Transient
    private String destaccountnumber;

    @Transient
    private String beneficiaryid;

    @Transient
    private String beneficiarycurrency;

    @Transient
    private String beneficiarybankcode;

    @Transient
    private List<SupportedTransactionTypes> transactiontypeslist;

    @Transient
    private List<BankDetailsList> bankdetailslist;

    @Transient
    private String ccemailaddress;
    //Muhammad Hamza: Adding for Mobile Banking Feature -- End


    @Column(name = "EVENTID")
    private String eventid;

    @Column(name = "BOOKID")
    private String bookid;


    //    @Transient
    @Column(name = "TICKETTYPEID")
    private String tickettypeid;


    @Column(name = "QUANTITY")
    private String quantity;

    //Muhammad Hamza: for MNO-Wallet to Wallet -- Start

    @Transient
    private List<MNOList> mnoslist;


    //Muhammad Hamza: for MNO-Wallet to Wallet -- END
    @Transient
    private String qrcode;

    //Muhammad Hamza: for E-Ticketing Integration -- END

    //Money Gram for IMT -- Start
    @Column(name = "TRANSACTION_SESSIONID")
    private String transactionsessionid;

    //@Column(name = "receiveragentid")
    @Transient
    private String receiveragentid;

    @Transient
    private String deliveryoption;

    @Transient
    private String secretquestionflag;

    //Money Gram for IMT -- End

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
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
    @ForeignKey(name = "WSLOG_SECURPARAM_FK")
    @Index(name = "idx_WSLOG_SECURPARAM_FK")
    private SecurityParams securityparams;


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

    //@Column(name = "DECRYPTEDOTP")
    //private String decryptedotp;

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

    @Transient
    private String updateemailaddress;
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

    @Column(name = "DESTEMAILADDRESS")
    private String destemailaddress;

    @Column(name = "DESTDATEOFBIRTH")
    private String destdateofbirth;

    @Column(name = "OTPCHANNEL") //Hamza adding for OTP via Email
    private String otpchannel;

    @Transient
    private String cardid;

    @Transient
    @JsonIgnore
    private String transactionname;

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
    private List<MerchantDashboardDataObj> merchantDashboardDataObjListForChild;

    @Transient
    private List<MerchantDashboardDataObj> merchantDashboardDataObjListForMerchant;

    @Transient
    private List<AppContact> contactlist;

    @Transient
    private List<BillPackageObj> billpackages;

    @Transient
    private List<CSCBalanceResp> cscBalanceResp; // Added by Affan on 26-July-23

    @Transient
    private List<CSCGetAmountDueResp> cscgetamountdueresp; // Added by Affan on 26-July-23

    @Transient
    private ClearedTransactionList cscTransactionList; // Added by Affan on 26-July-23

    @Transient
    private List<CSCGetClientDetailsResp> cscClientDetails; // Added by Affan on 26-July-23

    @Transient
    private CSCLoadCardResp cscLoadCard; // Added by Affan on 26-July-23

    @Transient
    private List<S2MCards> s2mcardslist;

    @Transient
    private List<TranObj> tranObjList;

    @Transient
    private S2MBalance s2mBalance;

    @Transient
    private String cardcolor;

    @Transient
    private List<XlateNotifiObj> xlatenotifiobjlist;

    public String getCardcolor() {
        return cardcolor;
    }

    public void setCardcolor(String cardcolor) {
        this.cardcolor = cardcolor;
    }

    public AppNotifWsEntity() {
        super();
    }

    // Added by Affan on 26-July-23

    @Override
    public CSCLoadCardResp getCscLoadCard() {
        return cscLoadCard;
    }

    @Override
    public void setCscLoadCard(CSCLoadCardResp cscLoadCard) {
        this.cscLoadCard = cscLoadCard;
    }

    @Override
    public List<CSCGetClientDetailsResp> getCscGetClientDetailsResp() {
        return cscClientDetails;
    }

    @Override
    public void setCscGetClientDetailsResp(List<CSCGetClientDetailsResp> cscClientDetails) {
        this.cscClientDetails = cscClientDetails;
    }

    @Override
    public List<CSCGetAmountDueResp> getCscgetamountdueresp() {
        return cscgetamountdueresp;
    }

    @Override
    public void setCscgetamountdueresp(List<CSCGetAmountDueResp> cscgetamountdueresp) {
        this.cscgetamountdueresp = cscgetamountdueresp;
    }

    @Override
    public ClearedTransactionList getCscTransactionList() {
        return cscTransactionList;
    }

    @Override
    public void setCscTransactionList(ClearedTransactionList cscTransactionList) {
        this.cscTransactionList = cscTransactionList;
    }

    @Override
    public List<CSCBalanceResp> getCscBalanceResp() {
        return cscBalanceResp;
    }

    @Override
    public void setCscBalanceResp(List<CSCBalanceResp> cscBalanceResp) {
        this.cscBalanceResp = cscBalanceResp;
    }
    // Added by Affan on 26-July-23

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
        if (!Util.hasText(this.getRespcodedesc())) { //Raza this check will prevent response code from re-fetching. However, if description is sent from request, it will not allow it to override..
            Util.setRespCodeDescription(this);
        }
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

    public AppNotifWsEntity copy() {
        AppNotifWsEntity newAppWsEntity = new AppNotifWsEntity();
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
        newAppWsEntity.setIsemailverified(this.getIsemailverified());
        newAppWsEntity.setTitle(this.getTitle());
        newAppWsEntity.setBody(this.getBody());
        newAppWsEntity.setFrtitle(this.getFrtitle());
        newAppWsEntity.setFrbody(this.getFrbody());
        newAppWsEntity.setOriginalapi(this.getOriginalapi());
        newAppWsEntity.setCategoryname(this.getCategoryname());
        newAppWsEntity.setDestdateofbirth(this.getDestdateofbirth());

        //Muhammad Hamza: for mobile banking
        newAppWsEntity.setIsemailverified(this.getIsemailverified());
        newAppWsEntity.setCustomeropposition(this.getCustomeropposition());
        newAppWsEntity.setAccountopposition(this.getAccountopposition());
        newAppWsEntity.setBranchcode(this.getBranchcode());
        newAppWsEntity.setBranchname(this.getBranchname());
        newAppWsEntity.setBiccode(this.getBiccode());
        newAppWsEntity.setAccountkey(this.getAccountkey());
        newAppWsEntity.setAccountholdername(this.getAccountholdername());
        newAppWsEntity.setAccountholderaddress(this.getAccountholderaddress());
        newAppWsEntity.setIscmsaccoutlinked(this.getIscmsaccoutlinked());
        newAppWsEntity.setListofemailaddress(this.getListofemailaddress());
        newAppWsEntity.setRmemail(this.getRmemail());
        newAppWsEntity.setRmemailsource(this.getRmemailsource());
        newAppWsEntity.setClientid(this.getClientid());
        newAppWsEntity.setSubject(this.getSubject());
        newAppWsEntity.setContactpreference(this.getContactpreference());
        newAppWsEntity.setSubjectlist(this.getSubjectlist());
        newAppWsEntity.setTransactionReasons(this.getTransactionReasons());
        newAppWsEntity.setTransactiontype(this.getTransactiontype());
        newAppWsEntity.setSourcetitle(this.getSourcetitle());
        newAppWsEntity.setSourceaccount(this.getSourceaccount());
        newAppWsEntity.setDestaccountnumber(this.getDestaccountnumber());
        newAppWsEntity.setDestaccounttitle(this.getDestaccounttitle());
        newAppWsEntity.setBeneficiaryname(this.getBeneficiaryname());
        newAppWsEntity.setBeneficiaryname(this.getBeneficiaryname());
        newAppWsEntity.setBeneficiaryid(this.getBeneficiaryid());
        newAppWsEntity.setTransactiontypeslist(this.getTransactiontypeslist());
        newAppWsEntity.setCcemailaddress(this.getCcemailaddress());
        newAppWsEntity.setBankdetailslist(this.getBankdetailslist());
        newAppWsEntity.setOtpchannel(this.getOtpchannel());

        //Muhammad Hamza: for E-Ticketing Integration -- Start
        newAppWsEntity.setEventid(this.getEventid());
        newAppWsEntity.setBookid(this.getBookid());
        newAppWsEntity.setQuantity(this.getQuantity());
        newAppWsEntity.setTickettypeid(this.getTickettypeid());
        newAppWsEntity.setQrcode(this.getQrcode());
        newAppWsEntity.setUpdateemailaddress(this.getUpdateemailaddress());

        //VCN Muhammad Umer: 06092024
        newAppWsEntity.setCardcolor(this.getCardcolor());

        //Muhammad Hamza: MNO -Wallet to Wallet -- Start
        newAppWsEntity.setMnoslist(this.getMnoslist());

        //Money Gram for IMT -- Start
        newAppWsEntity.setTransactionsessionid(this.getTransactionsessionid());
        newAppWsEntity.setReceiveragentid(this.getReceiveragentid());
        newAppWsEntity.setDeliveryoption(this.getDeliveryoption());
        newAppWsEntity.setSecretquestionflag(this.getSecretquestionflag());

        newAppWsEntity.setCardid(this.getCardid());
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
    public SecurityParams getSecurityparams() {
        if (securityparams == null) {   //Waleed Doing For OTP For Update Mobile Number
            securityparams = new SecurityParams();
        }
        return this.securityparams;
    }

    @Override
    public void setSecurityparams(SecurityParams securityparams) {
        this.securityparams = securityparams;
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

    //@Override
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

    public List<MerchantDashboardDataObj> getMerchantDashboardDataObjListForChild() {
        return merchantDashboardDataObjListForChild;
    }

    public void setMerchantDashboardDataObjListForChild(List<MerchantDashboardDataObj> merchantDashboardDataObjListForChild) {
        this.merchantDashboardDataObjListForChild = merchantDashboardDataObjListForChild;
    }

    @Override
    public String getDestemailaddress() {
        return destemailaddress;
    }

    @Override
    public void setDestemailaddress(String destemailaddress) {
        this.destemailaddress = destemailaddress;
    }


    public AppNotifWsEntity copyForResponse() { //Raza adding to optimize response for App due to security OTP leak -- 23-11-2022
        AppNotifWsEntity newAppWsEntity = new AppNotifWsEntity();

        newAppWsEntity.setId(null);
        //newAppWsEntity.setServicename(this.getServicename());
        newAppWsEntity.setTransactionname(this.getServicename());
        newAppWsEntity.setMobilenumber(this.getMobilenumber());
        newAppWsEntity.setNewmobilenumber(this.getNewmobilenumber());
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
        //newAppWsEntity.setPindata(this.getPindata());
        //newAppWsEntity.setOldpindata(this.getOldpindata());
        //newAppWsEntity.setNewpindata(this.getNewpindata());
        //newAppWsEntity.setEncryptkey(this.getEncryptkey());
        newAppWsEntity.setAmounttransaction(this.getAmounttransaction());
        newAppWsEntity.setSrcchargeamount(this.getSrcchargeamount());
        newAppWsEntity.setDestchargeamount(this.getDestchargeamount());
        newAppWsEntity.setDestaccount(this.getDestaccount());
        newAppWsEntity.setDestaccountcurrency(this.getDestaccountcurrency());
        //newAppWsEntity.setOtp(this.getOtp());
        //newAppWsEntity.setBiometricdata(this.getBiometricdata());
        newAppWsEntity.setStatus(this.getStatus());
        //logger.info("Setting ResCode & Desc...");
        newAppWsEntity.setRespcodedesc(this.getRespcodedesc()); //Raza setting ResponseCode Desc from original object, no need to re-fetch and set
        newAppWsEntity.setRespcode(this.getRespcode());
        //logger.info("ResCode Desc [" + newAppWsEntity.getRespcodedesc() + "]...");
        newAppWsEntity.setPlaceofbirth(this.getPlaceofbirth());
        newAppWsEntity.setFathername(this.getFathername());
        newAppWsEntity.setProvince(this.getProvince());
        //newAppWsEntity.setTsp(this.getTsp());
        newAppWsEntity.setDestbankid(this.getDestbankid());
        newAppWsEntity.setOrigdataelement(this.getOrigdataelement());
        newAppWsEntity.setAtmid(this.getAtmid());
        newAppWsEntity.setCorebankcode(this.getCorebankcode());
        newAppWsEntity.setCoreaccount(this.getCoreaccount());
        newAppWsEntity.setCoreaccountcurrency(this.getCoreaccountcurrency());
        newAppWsEntity.setCardnumber(this.getCardnumber());
        //newAppWsEntity.setCardpindata(this.getCardpindata());
        newAppWsEntity.setEnableflag(this.getEnableflag());
        newAppWsEntity.setMerchantid(this.getMerchantid());
        newAppWsEntity.setDailylimit(this.getDailylimit());
        newAppWsEntity.setMonthlylimit(this.getMonthlylimit());
        newAppWsEntity.setYearlylimit(this.getYearlylimit());
        newAppWsEntity.setStatus(this.getStatus());
        newAppWsEntity.setStan(this.getStan());
        newAppWsEntity.setRrn(this.getRrn());
        newAppWsEntity.setUserid(this.getUserid());
        //newAppWsEntity.setChannelid(this.getChannelid());
        newAppWsEntity.setAllowed(this.getAllowed());
        newAppWsEntity.setDeletetype(this.getDeletetype());
        newAppWsEntity.setComments(this.getComments());
        newAppWsEntity.setAcctid(this.getAcctid());
        newAppWsEntity.setAcctalias(this.getAcctalias());
        newAppWsEntity.setIsprimary(this.getIsprimary());
        newAppWsEntity.setAccountbalance(this.getAccountbalance());
        newAppWsEntity.setAcctlimit(this.getAcctlimit());
        newAppWsEntity.setAvailLimit(this.getAvailLimit());
        newAppWsEntity.setAvailLimitfreq(this.getAvailLimitfreq());
        newAppWsEntity.setState(this.getState());
        newAppWsEntity.setRequesttime(this.getRequesttime());
        newAppWsEntity.setActivationtime(this.getActivationtime());
        newAppWsEntity.setDestuserid(this.getDestuserid());
        newAppWsEntity.setAddress(this.getAddress());
        newAppWsEntity.setCity(this.getCity());
        newAppWsEntity.setCountry(this.getCountry());
        newAppWsEntity.setAdvanceflag(this.getAdvanceflag());
        newAppWsEntity.setSecondarynumber(this.getSecondarynumber());
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
        //newAppWsEntity.setPing(this.getPing());
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
        newAppWsEntity.setBillertxnid(this.getBillertxnid());
        newAppWsEntity.setCreationdate(this.getCreationdate());
        newAppWsEntity.setBankMnemonic(this.getBankMnemonic());
        newAppWsEntity.setAvailablebalance(this.getAvailablebalance());
        newAppWsEntity.setAcqbin(this.getAcqbin());
        newAppWsEntity.setBranchcode(this.getBranchcode());
        newAppWsEntity.setBranchname(this.getBranchname());
        newAppWsEntity.setSlipnumber(this.getSlipnumber());
        newAppWsEntity.setTellerid(this.getTellerid());
        newAppWsEntity.setDepositamount(this.getDepositamount());
        newAppWsEntity.setResponsedetail(this.getResponsedetail());
        newAppWsEntity.setCardexpiry(this.getCardexpiry());
        newAppWsEntity.setAcctStatus(this.getAcctStatus());
        newAppWsEntity.setPosconditioncode(this.getPosconditioncode());
        newAppWsEntity.setSettlementamount(this.getSettlementamount());
        newAppWsEntity.setSettlementcurrency(this.getSettlementcurrency());
        newAppWsEntity.setRelationshipid(this.getRelationshipid());
        newAppWsEntity.setName(this.getName());
        newAppWsEntity.setBillerrespcode(this.getBillerrespcode());
        newAppWsEntity.setBillerrespcodedesc(this.getBillerrespcodedesc());
        newAppWsEntity.setTotalamount(this.getTotalamount());
        newAppWsEntity.setBankcharges(this.getBankcharges());
        newAppWsEntity.setBanktaxamount(this.getBanktaxamount());
        newAppWsEntity.setTaxamount(this.getTaxamount());
        newAppWsEntity.setCredittranrefnumber(this.getCredittranrefnumber());
        newAppWsEntity.setCredittrandatetime(this.getCredittrandatetime());
        //newAppWsEntity.setChequeissbankcode(this.getChequeissbankcode());
        //newAppWsEntity.setChequeissbankname(this.getChequeissbankname());
        //newAppWsEntity.setChequeissbranchname(this.getChequeissbranchname());
        //newAppWsEntity.setInstrumentnumber(this.getInstrumentnumber());
        //newAppWsEntity.setChequepic(this.getChequepic());
        //newAppWsEntity.setEtachequeclearing(this.getEtachequeclearing());
        //newAppWsEntity.setChequebouncecode(this.getChequebouncecode());
        //newAppWsEntity.setChequebouncedesc(this.getChequebouncedesc());
        //newAppWsEntity.setChequetrandatetime(this.getChequetrandatetime());
        newAppWsEntity.setTrancurrency(this.getTrancurrency());
        newAppWsEntity.setIdentificationparameter(this.getIdentificationparameter());
        //newAppWsEntity.setSettledate(this.getSettledate());
        //newAppWsEntity.setCbillamount(this.getCbillamount());
        //newAppWsEntity.setCbillcurrency(this.getCbillcurrency());
        //newAppWsEntity.setCbillrate(this.getCbillrate());
        //newAppWsEntity.setDatesettlement(this.getDatesettlement());
        //newAppWsEntity.setPosentrymode(this.getPosentrymode());
        //newAppWsEntity.setCardsequenceno(this.getCardsequenceno());
        newAppWsEntity.setAmttranfee(this.getAmttranfee());
        if (Util.hasText(this.getServicename()) && this.getServicename().equals("GetUserKYCQuestionList")) {
//            newAppWsEntity.setSecretquestion1(this.getSecretquestion1());
//            newAppWsEntity.setSecretquestionanswer1(this.getSecretquestionanswer1());
//            newAppWsEntity.setSecretquestion2(this.getSecretquestion2());
//            newAppWsEntity.setSecretquestionanswer2(this.getSecretquestionanswer2());
        }
        newAppWsEntity.setCodflag(this.getCodflag());
        //newAppWsEntity.setSelfiepicture(this.getSelfiepicture());
        newAppWsEntity.setCreditaccountnumber(this.getCreditaccountnumber());
        newAppWsEntity.setAccountlimits(this.getAccountlimits());
        newAppWsEntity.setLinkedaccounts(this.getLinkedaccounts());
        //newAppWsEntity.setProvisionalwallets(this.getProvisionalwallets());
        newAppWsEntity.setAccountlist(this.getAccountlist());
        newAppWsEntity.setTransactions(this.getTransactions());
        newAppWsEntity.setUsertransactions(this.getUsertransactions());
        //newAppWsEntity.setMerchantTransactions(this.getMerchantTransactions());
        newAppWsEntity.setTransactionDetail(this.getTransactionDetail());
        newAppWsEntity.setCurrency(this.getCurrency());
        newAppWsEntity.setCustomerid(this.getCustomerid());
        newAppWsEntity.setBank(this.getBank());
        newAppWsEntity.setDestbank(this.getDestbank());
        newAppWsEntity.setCorebank(this.getCorebank());
        newAppWsEntity.setDestcurrency(this.getDestcurrency());
        newAppWsEntity.setCorecurrency(this.getCorecurrency());
        newAppWsEntity.setPassword(this.getPassword());
        newAppWsEntity.setDestination(this.getDestination());
        newAppWsEntity.setTrack2Data(this.getTrack2Data());
        newAppWsEntity.setTrack3Data(this.getTrack3Data());
        newAppWsEntity.setTrack1Data(this.getTrack1Data());
        newAppWsEntity.setIcccarddata(this.getIcccarddata());
        newAppWsEntity.setSettlementdelay(this.getSettlementdelay());
        newAppWsEntity.setEodenabled(this.getEodenabled());
        newAppWsEntity.setPagecount(this.getPagecount());
        newAppWsEntity.setPagesize(this.getPagesize());
        newAppWsEntity.setTotalcount(this.getTotalcount());
        newAppWsEntity.setBillerid(this.getBillerid());
        newAppWsEntity.setBillername(this.getBillername());
        newAppWsEntity.setPartialflag(this.getPartialflag());
        newAppWsEntity.setTempblockflag(this.getTempblockflag());
        newAppWsEntity.setIncomingip(this.getIncomingip());
        //newAppWsEntity.setCardNoLastDigits(this.getCardNoLastDigits());
        newAppWsEntity.setToken(this.getToken());
        //newAppWsEntity.setSettledate(this.getSettledate());
        newAppWsEntity.setTerminalid(this.getTerminalid());
        newAppWsEntity.setTermloc(this.getTermloc());
        newAppWsEntity.setActioncode(this.getActioncode());
        newAppWsEntity.setSessionid(this.getSessionid());
        newAppWsEntity.setFingerindex(this.getFingerindex());
        newAppWsEntity.setTemplatetype(this.getTemplatetype());
        newAppWsEntity.setArea(this.getArea());
        newAppWsEntity.setMessage(this.getMessage());
        newAppWsEntity.setReserved2(this.getReserved2());
        newAppWsEntity.setReserved3(this.getReserved3());
        newAppWsEntity.setAgentloc(this.getAgentloc());
        //newAppWsEntity.setNadratxnid(this.getNadratxnid());
        newAppWsEntity.setSelfflag(this.getSelfflag());
        newAppWsEntity.setDecryptedotp(this.getDecryptedotp());
        //newAppWsEntity.setCiphereddata(this.getCiphereddata());
        newAppWsEntity.setDebugtag(this.getDebugtag());
        newAppWsEntity.setOriginalapi(this.getOriginalapi());
        newAppWsEntity.setAcctlevel(this.getAcctlevel());
        newAppWsEntity.setTrackingid(this.getTrackingid());
        newAppWsEntity.setBatchid(this.getBatchid());
        newAppWsEntity.setPosinvoiceref(this.getPosinvoiceref());
        newAppWsEntity.setDisputeflag(this.getDisputeflag());
        newAppWsEntity.setHeadofficeflag(this.getHeadofficeflag());
        newAppWsEntity.setHaswalletflag(this.getHaswalletflag());
        newAppWsEntity.setSettbankcode(this.getSettbankcode());
        newAppWsEntity.setBlocktype(this.getBlocktype());
        newAppWsEntity.setLockstate(this.getLockstate());
        newAppWsEntity.setFinancialflag(this.getFinancialflag());
        newAppWsEntity.setEmailaddress(this.getEmailaddress());
        newAppWsEntity.setFirstname(this.getFirstname());
        newAppWsEntity.setMiddlename(this.getMiddlename());
        newAppWsEntity.setNationality(this.getNationality());
        newAppWsEntity.setLastname(this.getLastname());
        newAppWsEntity.setDestmobilenumber(this.getDestmobilenumber());
        newAppWsEntity.setMerchantamount(this.getMerchantamount());
        newAppWsEntity.setContactlist(this.getContactlist());
        newAppWsEntity.setAgenttype(this.getAgenttype());
        newAppWsEntity.setIdentificationtype(this.getIdentificationtype());
        newAppWsEntity.setBusinessname(this.getBusinessname());
        newAppWsEntity.setAlternatenumber(this.getAlternatenumber());
        newAppWsEntity.setAlternateemail(this.getAlternateemail());
        newAppWsEntity.setCreatoruser(this.getCreatoruser());
        newAppWsEntity.setLastupdateuser(this.getLastupdateuser());
        newAppWsEntity.setLastupdatedate(this.getLastupdatedate());
        newAppWsEntity.setCreatedby(this.getCreatedby());
        newAppWsEntity.setCloseflag(this.getCloseflag());
        newAppWsEntity.setCdfaccountnumber(this.getCdfaccountnumber());
        newAppWsEntity.setUsdaccountnumber(this.getUsdaccountnumber());
        newAppWsEntity.setCdfacctid(this.getCdfacctid());
        newAppWsEntity.setUsdacctid(this.getUsdacctid());
        newAppWsEntity.setNewusername(this.getNewusername());
        newAppWsEntity.setNewpassword(this.getNewpassword());
        newAppWsEntity.setWallettype(this.getWallettype());
        newAppWsEntity.setCommusdacctid(this.getCommusdacctid());
        newAppWsEntity.setCommcdfacctid(this.getCommcdfacctid());
        newAppWsEntity.setDestagentid(this.getDestagentid());
        //newAppWsEntity.setDocumentname(this.getDocumentname());
        //newAppWsEntity.setDocumentpic(this.getDocumentpic());
        newAppWsEntity.setUploaddatetime(this.getUploaddatetime());
        newAppWsEntity.setPermissions(this.getPermissions());
        newAppWsEntity.setMemo(this.getMemo());
        newAppWsEntity.setQrid(this.getQrid());
        newAppWsEntity.setAdvertisement1(this.getAdvertisement2());
        newAppWsEntity.setAdvertisement3(this.getAdvertisement3());
        newAppWsEntity.setCashoutlist(this.getCashoutlist());
        if (Util.hasText(this.getVersion()) && this.getVersion().equals("true")) {

            if (this.getServicename().equals("CashOutRequest") || this.getServicename().equals("GetCashOutList")) {
                newAppWsEntity.setCashoutpin(Util.hasText(this.getCashoutpin()) ? WSEncryptionUtil.EncryptAppCashOutPin(this.getCashoutpin()) : this.getCashoutpin());
            } else {
                newAppWsEntity.setCashoutpin(Util.hasText(this.getCashoutpin()) ? WSEncryptionUtil.EncryptAppAgentCashOutPin(this.getCashoutpin()) : this.getCashoutpin());
            }

            if (this.getServicename().equals("CashOutRequest") || this.getServicename().equals("GetCashOutList")
                    || this.getServicename().equals("EnvoiCashRequest") || this.getServicename().equals("GetEnvoiCashList")) { //Raza using this check 2wise
                if (newAppWsEntity.getCashoutlist() != null && newAppWsEntity.getCashoutlist().size() > 0) {
                    for (CashOut c : newAppWsEntity.getCashoutlist()) {
                        c.setCashoutpin(Util.hasText(c.getCashoutpin()) ? WSEncryptionUtil.EncryptAppCashOutPin(c.getCashoutpin()) : c.getCashoutpin());
                    }
                }
            } else {
                if (newAppWsEntity.getCashoutlist() != null && newAppWsEntity.getCashoutlist().size() > 0) {
                    for (CashOut c : newAppWsEntity.getCashoutlist()) {
                        c.setCashoutpin(Util.hasText(c.getCashoutpin()) ? WSEncryptionUtil.EncryptAppAgentCashOutPin(c.getCashoutpin()) : c.getCashoutpin());
                    }
                }
            }

        } else {
            newAppWsEntity.setCashoutpin(this.getCashoutpin());
        }
        newAppWsEntity.setCurrencyrate(this.getCurrencyrate());
        newAppWsEntity.setRequestmoneyref(this.getRequestmoneyref());
        newAppWsEntity.setExpiry(this.getExpiry());
        newAppWsEntity.setAuthorizationnumber(this.getAuthorizationnumber());
        newAppWsEntity.setBillamount(this.getBillamount());
        newAppWsEntity.setWalletcurrency(this.getWalletcurrency());
        newAppWsEntity.setWalletstatus(this.getWalletstatus());
        newAppWsEntity.setProduct(this.getProduct());
        newAppWsEntity.setReason(this.getReason());
        newAppWsEntity.setDestacctid(this.getDestacctid());
        newAppWsEntity.setAmountcbill(this.getAmountcbill());
        newAppWsEntity.setMerchanttype(this.getMerchanttype());
        newAppWsEntity.setDestmerchantid(this.getDestmerchantid());
        newAppWsEntity.setPoolaccountnumber(this.getPoolaccountnumber());
        newAppWsEntity.setPoolaccountcurrency(this.getPoolaccountcurrency());
        newAppWsEntity.setPaymentmethod(this.getPaymentmethod());
        newAppWsEntity.setRespfilter(this.getRespfilter());
        newAppWsEntity.setRating(this.getRating());
        newAppWsEntity.setRatingfilter(this.getRatingfilter());
        newAppWsEntity.setAgentfilter(this.getAgentfilter());
        newAppWsEntity.setDestagentfilter(this.getDestagentfilter());
        newAppWsEntity.setMerchantfilter(this.getMerchantfilter());
        newAppWsEntity.setDestmerchantfilter(this.getDestmerchantfilter());
        newAppWsEntity.setAmountcommissionusd(this.getAmountcommissionusd());
        newAppWsEntity.setAmountcommissioncdf(this.getAmountcommissioncdf());
        newAppWsEntity.setFilename(this.getFilename());
        newAppWsEntity.setNewfilename(this.getNewfilename());
        newAppWsEntity.setBillerpassword(this.getBillerpassword());
        newAppWsEntity.setDestbusinessname(this.getDestbusinessname());
        newAppWsEntity.setDestaddress(this.getDestaddress());
        newAppWsEntity.setPackagecode(this.getPackagecode());
        newAppWsEntity.setBillerid(this.getBillerid());
        newAppWsEntity.setDestfirstname(this.getDestfirstname());
        newAppWsEntity.setDestlastname(this.getDestlastname());
        newAppWsEntity.setDestcountry(this.getDestcountry());
        newAppWsEntity.setBillerusername(this.getBillerusername());
        newAppWsEntity.setApiname(this.getApiname());
        newAppWsEntity.setFrapiname(this.getFrapiname());
        newAppWsEntity.setPayerid(this.getPayertype());
        newAppWsEntity.setQuotationextid(this.getQuotationextid());
        newAppWsEntity.setQuotationid(this.getQuotationid());
        newAppWsEntity.setTransactionid(this.getTransactionid());
        newAppWsEntity.setTransactionextid(this.getTransactionextid());
        newAppWsEntity.setIban(this.getIban());
        newAppWsEntity.setSwiftbiccode(this.getSwiftbiccode());
        newAppWsEntity.setIfscode(this.getIfscode());
        newAppWsEntity.setClabe(this.getClabe());
        newAppWsEntity.setCbu(this.getCbu());
        newAppWsEntity.setCbualias(this.getCbualias());
        newAppWsEntity.setBikcode(this.getBikcode());
        newAppWsEntity.setAbaroutingnumber(this.getAbaroutingnumber());
        newAppWsEntity.setBsbnumber(this.getBsbnumber());
        newAppWsEntity.setRoutingcode(this.getRoutingcode());
        newAppWsEntity.setEntityttid(this.getEntityttid());
        newAppWsEntity.setAccounttype(this.getAccounttype());
        newAppWsEntity.setFwdchannelid(this.getFwdchannelid());
        newAppWsEntity.setLanguage(this.getLanguage());
        newAppWsEntity.setTitle(this.getTitle());
        newAppWsEntity.setBody(this.getBody());
        newAppWsEntity.setFrtitle(this.getFrtitle());
        newAppWsEntity.setFrbody(this.getFrbody());
        newAppWsEntity.setKycstatus(this.getKycstatus());
        newAppWsEntity.setNotiflanguage(this.getNotiflanguage());
        newAppWsEntity.setDestlanguage(this.getDestlanguage());
        newAppWsEntity.setDuration(this.getDuration());
        newAppWsEntity.setSubscriberno(this.getSubscriberno());
        newAppWsEntity.setGender(this.getGender());
        newAppWsEntity.setMunicipality(this.getMunicipality());
        newAppWsEntity.setTermsandcondition(this.getTermsandcondition());
        newAppWsEntity.setMonthlyincome(this.getMonthlyincome());
        newAppWsEntity.setMonthlyexpenditure(this.getMonthlyexpenditure());
        newAppWsEntity.setOccupation(this.getOccupation());
        newAppWsEntity.setCustcomments(this.getCustcomments());
        newAppWsEntity.setRelationshipcode(this.getRelationshipcode());
        newAppWsEntity.setMig_cdfbalance(this.getMig_cdfbalance());
        newAppWsEntity.setMig_usdbalance(this.getMig_usdbalance());
        newAppWsEntity.setIs_merchant(this.getIs_merchant());
        newAppWsEntity.setAliaslist(this.getAliaslist());
        newAppWsEntity.setRequestmoneylist(this.getRequestmoneylist());
        newAppWsEntity.setPendingremitloglist(this.getPendingremitloglist());
        newAppWsEntity.setMerchantDashboardDataObjList(this.getMerchantDashboardDataObjList());
        newAppWsEntity.setMerchantDashboardDataObjListForMerchant(this.getMerchantDashboardDataObjListForMerchant());
        newAppWsEntity.setMerchantDashboardDataObjListForChild(this.getMerchantDashboardDataObjListForChild());

        newAppWsEntity.setWalletsAndLinkedAccounts(this.getWalletsAndLinkedAccounts());

        newAppWsEntity.setTransactionportallist(this.getTransactionportallist());
        newAppWsEntity.setSecurityparams(this.getSecurityparams());
        newAppWsEntity.setNfsecurityparams(this.getNfsecurityparams());
        newAppWsEntity.setCombineddashboardgraph(this.getCombineddashboardgraph());
        newAppWsEntity.setLstcombineddashboardgraph(this.getLstcombineddashboardgraph());
        newAppWsEntity.setWalletlist(this.getWalletlist());
        newAppWsEntity.setBulkpaymentsummary(this.getBulkpaymentsummary());
        newAppWsEntity.setBulkpaymentsummarylist(this.getBulkpaymentsummarylist());
        newAppWsEntity.setBulkpaymenttransactionlist(this.getBulkpaymenttransactionlist());
        newAppWsEntity.setBillpackages(this.getBillpackages());
        newAppWsEntity.setAppgraphlist(this.getAppgraphlist());
        newAppWsEntity.setSecretquestionslist(this.getSecretquestionslist());
        newAppWsEntity.setNotificationlist(this.getNotificationlist());
        newAppWsEntity.setInviteslist(this.getInviteslist());
        newAppWsEntity.setCountrylist(this.getCountrylist());
        newAppWsEntity.setIncomelist(this.getIncomelist());
        newAppWsEntity.setMonthlyexpenditurelist(this.getMonthlyexpenditurelist());
        newAppWsEntity.setOccupationlist(this.getOccupationlist());
        newAppWsEntity.setStatelist(this.getStatelist());
        newAppWsEntity.setCitylist(this.getCitylist());
        newAppWsEntity.setMunicipalitylist(this.getMunicipalitylist());
        newAppWsEntity.setCallcodelist(this.getCallcodelist());
        newAppWsEntity.setCurrencylist(this.getCurrencylist());
        newAppWsEntity.setPayerslist(this.getPayerslist());
        newAppWsEntity.setBeneficiarylist(this.getBeneficiarylist());
        newAppWsEntity.setChargedetails(this.getChargedetails());
        //newAppWsEntity.setOtplist(this.getOtplist());
        newAppWsEntity.setS2mcardslist(this.getS2mcardslist());
        newAppWsEntity.setTranObjList(this.getTranObjList());
        newAppWsEntity.setS2mBalance(this.getS2mBalance());
        newAppWsEntity.setAmpldepositsummary(this.getAmpldepositsummary());
        newAppWsEntity.setAmpldeposittransactions(this.getAmpldeposittransactions());
        newAppWsEntity.setAmpldepositsummarylist(this.getAmpldepositsummarylist());
        newAppWsEntity.setResplist(this.getResplist());
        newAppWsEntity.setImtreasonlist(this.getImtreasonlist());
        newAppWsEntity.setCard(this.getCard());
        newAppWsEntity.setCardlist(this.getCardlist());
        newAppWsEntity.setUnreadapprovalcount(this.getUnreadapprovalcount());
        newAppWsEntity.setMerchantcode(this.getMerchantcode());
        newAppWsEntity.setDestmerchantcode(this.getDestmerchantcode());
        newAppWsEntity.setDestmerchantname(this.getDestmerchantname());
        newAppWsEntity.setSdccode(this.getSdccode());
        newAppWsEntity.setSdcid(this.getSdcid());
        newAppWsEntity.setOrderid(this.getOrderid());
        newAppWsEntity.setRedirecthtml(this.getRedirecthtml());
        newAppWsEntity.setCardsecuritycode(this.getCardsecuritycode());
        newAppWsEntity.setThreedsenabled(this.getThreedsenabled());
        newAppWsEntity.setThreedsversion(this.getThreedsversion());
        newAppWsEntity.setThreedsacceptedversion(this.getThreedsacceptedversion());
        newAppWsEntity.setMaxloginattempts(this.getMaxloginattempts());
        newAppWsEntity.setScheme(this.getScheme());
        newAppWsEntity.setBrand(this.getBrand());
        newAppWsEntity.setDestemailaddress(this.getDestemailaddress());
        newAppWsEntity.setVersion(this.getVersion());
        newAppWsEntity.setExchangerate(this.getExchangerate());
        if (this.getForexrates() != null) {
            logger.info("ForexRates Obj found, setting....");
        }
        newAppWsEntity.setForexrates(this.getForexrates());
        newAppWsEntity.setBeneficiariesdata(this.getBeneficiariesdata());
        newAppWsEntity.setBeneficiaryobj(this.getBeneficiaryobj());
        newAppWsEntity.setDevices(this.getDevices());
        newAppWsEntity.setIccidstatus(this.getIccidstatus());
        newAppWsEntity.setDevicebindstatus(this.getDevicebindstatus());

        //Muhammad Hamza: for mobile banking
        newAppWsEntity.setIsemailverified(this.getIsemailverified());
        newAppWsEntity.setBankcode(this.getBankcode());
        newAppWsEntity.setCustomeropposition(this.getCustomeropposition());
        newAppWsEntity.setAccountopposition(this.getAccountopposition());
        newAppWsEntity.setBiccode(this.getBiccode());
        newAppWsEntity.setAccountkey(this.getAccountkey());
        newAppWsEntity.setAccountholdername(this.getAccountholdername());
        newAppWsEntity.setAccountholderaddress(this.getAccountholderaddress());
        newAppWsEntity.setIscmsaccoutlinked(this.getIscmsaccoutlinked());
        newAppWsEntity.setListofemailaddress(this.getListofemailaddress());
        newAppWsEntity.setRmemail(this.getRmemail());
        newAppWsEntity.setRmemailsource(this.getRmemailsource());
        newAppWsEntity.setClientid(this.getClientid());
        newAppWsEntity.setSubject(this.getSubject());
        newAppWsEntity.setContactpreference(this.getContactpreference());
        newAppWsEntity.setSubjectlist(this.getSubjectlist());
        newAppWsEntity.setTransactionReasons(this.getTransactionReasons());
        newAppWsEntity.setTransactiontype(this.getTransactiontype());
        newAppWsEntity.setSourcetitle(this.getSourcetitle());
        newAppWsEntity.setSourceaccount(this.getSourceaccount());
        newAppWsEntity.setDestaccountnumber(this.getDestaccountnumber());
        newAppWsEntity.setDestaccounttitle(this.getDestaccounttitle());
        newAppWsEntity.setBeneficiaryname(this.getBeneficiarytype());
        newAppWsEntity.setBeneficiaryname(this.getBeneficiaryname());
        newAppWsEntity.setBeneficiaryid(this.getBeneficiaryid());
        newAppWsEntity.setTransactiontypeslist(this.getTransactiontypeslist());
        newAppWsEntity.setCcemailaddress(this.getCcemailaddress());
        newAppWsEntity.setBankdetailslist(this.getBankdetailslist());
        newAppWsEntity.setOtpchannel(this.getOtpchannel());

        //Muhammad Hamza: for E-Ticketing Integration -- Start
        newAppWsEntity.setEventid(this.getEventid());
        newAppWsEntity.setBookid(this.getBookid());
        newAppWsEntity.setQuantity(this.getQuantity());
        newAppWsEntity.setTickettypeid(this.getTickettypeid());
        newAppWsEntity.setQrcode(this.getQrcode());
        newAppWsEntity.setUpdateemailaddress(this.getUpdateemailaddress());

        //VCN Muhammad Umer: 06092024
        newAppWsEntity.setCardcolor(this.getCardcolor());

        //Muhammad Hamza: MNO -Wallet to Wallet -- Start
        newAppWsEntity.setMnoslist(this.getMnoslist());

        //Money Gram for IMT -- Start
        newAppWsEntity.setTransactionsessionid(this.getTransactionsessionid());
        newAppWsEntity.setReceiveragentid(this.getReceiveragentid());
        newAppWsEntity.setDeliveryoption(this.getDeliveryoption());
        newAppWsEntity.setSecretquestionflag(this.getSecretquestionflag());

        newAppWsEntity.setApiversion(this.getApiversion());

        newAppWsEntity.setCardid(this.getCardid());

        return newAppWsEntity;
    }

    public void setDataForApp() {
        super.setServicename(this.getServicename());
    }

    @JsonIgnore
    public String getTransactionname() {
        return transactionname;
    }

    public void setTransactionname(String transactionname) {
        this.transactionname = transactionname;
    }

    public List<S2MCards> getS2mcardslist() {
        return s2mcardslist;
    }

    public void setS2mcardslist(List<S2MCards> s2mcardslist) {
        this.s2mcardslist = s2mcardslist;
    }


    public List<TranObj> getTranObjList() {
        return tranObjList;
    }

    public void setTranObjList(List<TranObj> tranObjList) {
        this.tranObjList = tranObjList;
    }

    public S2MBalance getS2mBalance() {
        return s2mBalance;
    }

    public void setS2mBalance(S2MBalance s2mBalance) {
        this.s2mBalance = s2mBalance;
    }

    @Override
    public String getDestdateofbirth() {
        return destdateofbirth;
    }

    @Override
    public void setDestdateofbirth(String destdateofbirth) {
        this.destdateofbirth = destdateofbirth;
    }

    public Boolean getIsemailverified() {
        return isemailverified;
    }

    public void setIsemailverified(Boolean isemailverified) {
        this.isemailverified = isemailverified;
    }


    @Override
    public String getBiccode() {
        return biccode;
    }

    @Override
    public void setBiccode(String biccode) {
        this.biccode = biccode;
    }

    @Override
    public String getAccountkey() {
        return accountkey;
    }

    @Override
    public void setAccountkey(String accountkey) {
        this.accountkey = accountkey;
    }

    @Override
    public String getAccountholdername() {
        return accountholdername;
    }

    @Override
    public void setAccountholdername(String accountholdername) {
        this.accountholdername = accountholdername;
    }

    @Override
    public String getAccountholderaddress() {
        return accountholderaddress;
    }

    @Override
    public void setAccountholderaddress(String accountholderaddress) {
        this.accountholderaddress = accountholderaddress;
    }

    public Boolean getIscmsaccoutlinked() {
        return iscmsaccoutlinked;
    }

    public void setIscmsaccoutlinked(Boolean iscmsaccoutlinked) {
        this.iscmsaccoutlinked = iscmsaccoutlinked;
    }

    public List<String> getListofemailaddress() {
        return listofemailaddress;
    }

    public void setListofemailaddress(List<String> listofemailaddress) {
        this.listofemailaddress = listofemailaddress;
    }


    public String getRmemail() {
        return rmemail;
    }

    public void setRmemail(String rmemail) {
        this.rmemail = rmemail;
    }

    public String getRmemailsource() {
        return rmemailsource;
    }

    public void setRmemailsource(String rmemailsource) {
        this.rmemailsource = rmemailsource;
    }

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContactpreference() {
        return contactpreference;
    }

    public void setContactpreference(String contactpreference) {
        this.contactpreference = contactpreference;
    }


    @Override
    public List<ContactRMSubjectObj> getSubjectlist() {
        return subjectlist;
    }

    @Override
    public void setSubjectlist(List<ContactRMSubjectObj> subjectlist) {
        this.subjectlist = subjectlist;
    }

    @Override
    public List<TransactionReasons> getTransactionReasons() {
        return transactionReasons;
    }

    @Override
    public void setTransactionReasons(List<TransactionReasons> transactionReasons) {
        this.transactionReasons = transactionReasons;
    }

    public List<AccountOppositionLevel> getCustomeropposition() {
        return customeropposition;
    }

    public void setCustomeropposition(List<AccountOppositionLevel> customeropposition) {
        this.customeropposition = customeropposition;
    }

    public List<AccountOppositionLevel> getAccountopposition() {
        return accountopposition;
    }

    public void setAccountopposition(List<AccountOppositionLevel> accountopposition) {
        this.accountopposition = accountopposition;
    }

    public String getTransactiontype() {
        return transactiontype;
    }

    public void setTransactiontype(String transactiontype) {
        this.transactiontype = transactiontype;
    }

    @Override
    public String getSourceaccount() {
        return sourceaccount;
    }

    @Override
    public void setSourceaccount(String sourceaccount) {
        this.sourceaccount = sourceaccount;
    }

    @Override
    public String getSourcetitle() {
        return sourcetitle;
    }

    @Override
    public void setSourcetitle(String sourcetitle) {
        this.sourcetitle = sourcetitle;
    }

    @Override
    public String getBeneficiarytype() {
        return beneficiarytype;
    }

    @Override
    public void setBeneficiarytype(String beneficiarytype) {
        this.beneficiarytype = beneficiarytype;
    }


    @Override
    public String getDestaccounttitle() {
        return destaccounttitle;
    }

    @Override
    public void setDestaccounttitle(String destaccounttitle) {
        this.destaccounttitle = destaccounttitle;
    }

    @Override
    public String getDestaccountnumber() {
        return destaccountnumber;
    }

    @Override
    public void setDestaccountnumber(String destaccountnumber) {
        this.destaccountnumber = destaccountnumber;
    }

    @Override
    public String getBeneficiaryid() {
        return beneficiaryid;
    }

    @Override
    public void setBeneficiaryid(String beneficiaryid) {
        this.beneficiaryid = beneficiaryid;
    }

    @Override
    public String getOtpchannel() {
        return otpchannel;
    }

    @Override
    public void setOtpchannel(String otpchannel) {
        this.otpchannel = otpchannel;
    }

    @Override
    public List<SupportedTransactionTypes> getTransactiontypeslist() {
        return transactiontypeslist;
    }

    @Override
    public void setTransactiontypeslist(List<SupportedTransactionTypes> transactiontypeslist) {
        this.transactiontypeslist = transactiontypeslist;
    }

    public String getCcemailaddress() {
        return ccemailaddress;
    }

    public void setCcemailaddress(String ccemailaddress) {
        this.ccemailaddress = ccemailaddress;
    }

    public List<BankDetailsList> getBankdetailslist() {
        return bankdetailslist;
    }

    public void setBankdetailslist(List<BankDetailsList> bankdetailslist) {
        this.bankdetailslist = bankdetailslist;
    }

    @Override
    public String getEventid() {
        return eventid;
    }

    @Override
    public void setEventid(String eventid) {
        this.eventid = eventid;
    }

    @Override
    public String getBookid() {
        return bookid;
    }

    @Override
    public void setBookid(String bookid) {
        this.bookid = bookid;
    }

    @Override
    public String getBeneficiaryname() {
        return beneficiaryname;
    }

    @Override
    public void setBeneficiaryname(String beneficiaryname) {
        this.beneficiaryname = beneficiaryname;
    }

    public String getTickettypeid() {
        return tickettypeid;
    }

    public void setTickettypeid(String tickettypeid) {
        this.tickettypeid = tickettypeid;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    @Override
    public List<MNOList> getMnoslist() {
        return mnoslist;
    }

    @Override
    public void setMnoslist(List<MNOList> mnoslist) {
        this.mnoslist = mnoslist;
    }

    public String getUpdateemailaddress() {
        return updateemailaddress;
    }

    public void setUpdateemailaddress(String updateemailaddress) {
        this.updateemailaddress = updateemailaddress;
    }

    public String getCardid() {
        return cardid;
    }

    public void setCardid(String cardid) {
        this.cardid = cardid;
    }

    @Override
    public String getTransactionsessionid() {
        return transactionsessionid;
    }

    @Override
    public void setTransactionsessionid(String transactionsessionid) {
        this.transactionsessionid = transactionsessionid;
    }

    public String getReceiveragentid() {
        return receiveragentid;
    }

    public void setReceiveragentid(String receiveragentid) {
        this.receiveragentid = receiveragentid;
    }

    public String getDeliveryoption() {
        return deliveryoption;
    }

    public void setDeliveryoption(String deliveryoption) {
        this.deliveryoption = deliveryoption;
    }

    public String getSecretquestionflag() {
        return secretquestionflag;
    }

    public void setSecretquestionflag(String secretquestionflag) {
        this.secretquestionflag = secretquestionflag;
    }

    public List<XlateNotifiObj> getXlatenotifiobjlist() {
        return xlatenotifiobjlist;
    }

    public void setXlatenotifiobjlist(List<XlateNotifiObj> xlatenotifiobjlist) {
        this.xlatenotifiobjlist = xlatenotifiobjlist;
    }
}
