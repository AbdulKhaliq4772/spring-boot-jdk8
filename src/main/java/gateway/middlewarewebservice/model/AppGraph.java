package gateway.middlewarewebservice.model;

public class AppGraph {

    private String month; //Jan. , Feb. , Mar. , Apr. , May. , Jun. , Jul. , Aug. , Sep. , Oct. , Nov. , Dec.

    private String creditamount;

    private String debitamount;


    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getCreditamount() {
        return creditamount;
    }

    public void setCreditamount(String creditamount) {
        this.creditamount = creditamount;
    }

    public String getDebitamount() {
        return debitamount;
    }

    public void setDebitamount(String debitamount) {
        this.debitamount = debitamount;
    }

    public static String GetMonthNamebyNumber(Integer monthnumber)
    {
        if(monthnumber < 0)
        {
            monthnumber = monthnumber + 13;
        }


        String month = "";
        switch (monthnumber)
        {
            case 1:
            {
                month = "Jan.";
                break;
            }
            case 2:
            {
                month = "Feb.";
                break;
            }
            case 3:
            {
                month = "Mar.";
                break;
            }
            case 4:
            {
                month = "Apr.";
                break;
            }
            case 5:
            {
                month = "May.";
                break;
            }
            case 6:
            {
                month = "Jun.";
                break;
            }
            case 7:
            {
                month = "Jul.";
                break;
            }
            case 8:
            {
                month = "Aug.";
                break;
            }
            case 9:
            {
                month = "Sep.";
                break;
            }
            case 10:
            {
                month = "Oct.";
                break;
            }
            case 11:
            {
                month = "Nov.";
                break;
            }
            case 12:
            {
                month = "Dec.";
                break;
            }
            default:
            {
                break;
            }
        }
        return month;
    }
}
