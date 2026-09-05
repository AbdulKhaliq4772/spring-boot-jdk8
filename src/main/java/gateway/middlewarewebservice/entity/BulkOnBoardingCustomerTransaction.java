package gateway.middlewarewebservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pk.vaulsys.apigateway.persistence.IEntity;

@Entity
@Table(name = "BULK_ONBOARD_CUST_TRANSACTION")
public class BulkOnBoardingCustomerTransaction implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator = "BULKONBOARDCUST_TRAN_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "BULKONBOARDCUST_TRAN_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "BULKONBOARDCUST_TRAN_ID_SEQ")
            })
    private Long id;

    @Transient
    private String bulkonboardcustsummaryid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BULK_ONBOARDCUST_SUMMARY", referencedColumnName = "ID")
    private BulkOnBoardingCustomerSummary bulkonboardcustsummary;

    @Column(name = "STAN")
    private String stan;

    @Column(name = "RRN")
    private String rrn;

    @Column(name = "TRANSDATETIME")
    private String transdatetime;

    @Column(name = "RESPCODE")
    private String respcode;


    @Column(name = "SERVICENAME")
    private String servicename = null;

    //mandatory fields
    @Column(name = "FIRST_NAME")
    private String firstname;

    @Column(name = "LAST_NAME")
    private String lastname;

    @Column(name = "ID_NUMBER")
    private String idnumber;

    @Column(name = "ID_TYPE")
    private String idtype;

    @Column(name = "ID_EXPIRY")
    private String idexpiry;

    @Column(name = "DATE_OF_BIRTH")
    private String dateofbirth;
    //mandatory fields

    @Column(name = "MIDDLE_NAME")
    private String middlename;

    @Column(name = "MOBILE_NUMBER")
    private String mobilenumber;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "EMAIL_ADDRESS")
    private String emailaddress;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "CNIC_PICTURE_FRONT", length = 4000)
    private String cnicpicturefront;


    @Column(name = "PIN_DATA")
    private String pindata;

    @Column(name = "ENCRYPT_KEY", length = 4000)
    private String encryptkey;

    @Column(name = "SECRET_QUESTION1")
    private String secretquestion1;

    @Column(name = "SECRET_QUESTION2")
    private String secretquestion2;

    @Column(name = "SECRET_ANSWER1")
    private String secretquestionanswer1;

    @Column(name = "SECRET_ANSWER2")
    private String secretquestionanswer2;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "NATIONALITY")
    private String nationality;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "PROVINCE")
    private String province;

    @Column(name = "CITY")
    private String city;

    @Column(name = "MUNICIPALITY")
    private String municipality;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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

    public String getTransdatetime() {
        return transdatetime;
    }

    public void setTransdatetime(String transdatetime) {
        this.transdatetime = transdatetime;
    }

    public String getRespcode() {
        return respcode;
    }

    public void setRespcode(String respcode) {
        this.respcode = respcode;
    }


    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public String getBulkonboardcustsummaryid() {
        return bulkonboardcustsummaryid;
    }

    public void setBulkonboardcustsummaryid(String bulkonboardcustsummaryid) {
        this.bulkonboardcustsummaryid = bulkonboardcustsummaryid;
    }

    public BulkOnBoardingCustomerSummary getBulkonboardcustsummary() {
        return bulkonboardcustsummary;
    }

    public void setBulkonboardcustsummary(BulkOnBoardingCustomerSummary bulkonboardcustsummary) {
        this.bulkonboardcustsummary = bulkonboardcustsummary;
    }

    public String getIdnumber() {
        return idnumber;
    }

    public void setIdnumber(String idnumber) {
        this.idnumber = idnumber;
    }

    public String getIdtype() {
        return idtype;
    }

    public void setIdtype(String idtype) {
        this.idtype = idtype;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getIdexpiry() {
        return idexpiry;
    }

    public void setIdexpiry(String idexpiry) {
        this.idexpiry = idexpiry;
    }

    public String getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(String dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmailaddress() {
        return emailaddress;
    }

    public void setEmailaddress(String emailaddress) {
        this.emailaddress = emailaddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCnicpicturefront() {
        return cnicpicturefront;
    }

    public void setCnicpicturefront(String cnicpicturefront) {
        this.cnicpicturefront = cnicpicturefront;
    }

    public String getPindata() {
        return pindata;
    }

    public void setPindata(String pindata) {
        this.pindata = pindata;
    }

    public String getEncryptkey() {
        return encryptkey;
    }

    public void setEncryptkey(String encryptkey) {
        this.encryptkey = encryptkey;
    }

    public String getSecretquestion1() {
        return secretquestion1;
    }

    public void setSecretquestion1(String secretquestion1) {
        this.secretquestion1 = secretquestion1;
    }

    public String getSecretquestion2() {
        return secretquestion2;
    }

    public void setSecretquestion2(String secretquestion2) {
        this.secretquestion2 = secretquestion2;
    }

    public String getSecretquestionanswer1() {
        return secretquestionanswer1;
    }

    public void setSecretquestionanswer1(String secretquestionanswer1) {
        this.secretquestionanswer1 = secretquestionanswer1;
    }

    public String getSecretquestionanswer2() {
        return secretquestionanswer2;
    }

    public void setSecretquestionanswer2(String secretquestionanswer2) {
        this.secretquestionanswer2 = secretquestionanswer2;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }
}
