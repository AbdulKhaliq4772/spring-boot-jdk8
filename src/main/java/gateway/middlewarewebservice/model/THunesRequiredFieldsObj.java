package gateway.middlewarewebservice.model;
import com.fasterxml.jackson.annotation.JsonInclude;




@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class THunesRequiredFieldsObj {

    private String field;

    private String label;

    private String frlabel;

    private String index;


    private String requirement;
    private String format;
    private String min;
    private String max;
    private String enumvalue;
    private String dynamic;
    private String visibility ;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFrlabel() {
        return frlabel;
    }

    public void setFrlabel(String frlabel) {
        this.frlabel = frlabel;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getEnumvalue() {
        return enumvalue;
    }

    public void setEnumvalue(String enumvalue) {
        this.enumvalue = enumvalue;
    }

    public String getDynamic() {
        return dynamic;
    }

    public void setDynamic(String dynamic) {
        this.dynamic = dynamic;
    }


    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {

            this.visibility = visibility;

    }
}
