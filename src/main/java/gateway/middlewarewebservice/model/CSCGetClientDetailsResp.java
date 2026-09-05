package gateway.middlewarewebservice.model;

// Added by Affan on 26-July-23

public class CSCGetClientDetailsResp
{
    private String clientNumber, birthDate, clientCountry, fathersName, firstName, idNumber,
            lastName, mobile1, mobile2, nationality, shortName, telPrivate, telWork;

    public CSCGetClientDetailsResp(){}

    public CSCGetClientDetailsResp(String clientNumber, String birthDate, String clientCountry, String fathersName, String firstName, String idNumber, String lastName, String mobile1, String mobile2, String nationality, String shortName, String telPrivate, String telWork) {
        this.clientNumber = clientNumber;
        this.birthDate = birthDate;
        this.clientCountry = clientCountry;
        this.fathersName = fathersName;
        this.firstName = firstName;
        this.idNumber = idNumber;
        this.lastName = lastName;
        this.mobile1 = mobile1;
        this.mobile2 = mobile2;
        this.nationality = nationality;
        this.shortName = shortName;
        this.telPrivate = telPrivate;
        this.telWork = telWork;
    }

    public String getClientNumber() {
        return clientNumber;
    }

    public void setClientNumber(String clientNumber) {
        this.clientNumber = clientNumber;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getClientCountry() {
        return clientCountry;
    }

    public void setClientCountry(String clientCountry) {
        this.clientCountry = clientCountry;
    }

    public String getFathersName() {
        return fathersName;
    }

    public void setFathersName(String fathersName) {
        this.fathersName = fathersName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobile1() {
        return mobile1;
    }

    public void setMobile1(String mobile1) {
        this.mobile1 = mobile1;
    }

    public String getMobile2() {
        return mobile2;
    }

    public void setMobile2(String mobile2) {
        this.mobile2 = mobile2;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getTelPrivate() {
        return telPrivate;
    }

    public void setTelPrivate(String telPrivate) {
        this.telPrivate = telPrivate;
    }

    public String getTelWork() {
        return telWork;
    }

    public void setTelWork(String telWork) {
        this.telWork = telWork;
    }
}
