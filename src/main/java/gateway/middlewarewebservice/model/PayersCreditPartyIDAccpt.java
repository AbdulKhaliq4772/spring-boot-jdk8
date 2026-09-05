package gateway.middlewarewebservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayersCreditPartyIDAccpt {

    private List<THunesRequiredFieldsObj> requiredfields;


    public List<THunesRequiredFieldsObj> getRequiredfields() {
        return requiredfields;
    }

    public void setRequiredfields(List<THunesRequiredFieldsObj> requiredfields) {
        //this.requiredfields = requiredfields;
        this.requiredfields = cleanRequiredFields(requiredfields);


//        if (requiredfields == null) {
//            this.requiredfields = null;
//        } else {
//            this.requiredfields = requiredfields.stream()
//                    .filter(this::hasAtLeastOneNonNullValue)
//                    .collect(Collectors.toList());
//        }
    }

//    private List<THunesRequiredFieldsObj> cleanRequiredFields(List<THunesRequiredFieldsObj> fields) {
//        if (fields == null) return null;
//        return fields.stream()
//                .filter(f ->
//                         f.getRequirement() != null || f.getFormat() != null ||
//                        f.getMin() != null || f.getMax() != null || f.getEnumvalue() != null ||
//                        f.getDynamic() != null || f.getVisibility() != null)
//                .collect(Collectors.toList());
//    }

    private List<THunesRequiredFieldsObj> cleanRequiredFields(List<THunesRequiredFieldsObj> fields) {
        if (fields == null) return null;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);  // Ignore null/empty values during serialization

        return fields.stream()
                .map(f -> {
                    try {
                        // Convert object to JSON string to remove null/empty values
                        String jsonString = objectMapper.writeValueAsString(f);
                        // Reconvert back to object
                        THunesRequiredFieldsObj cleanedObj = objectMapper.readValue(jsonString, THunesRequiredFieldsObj.class);

                        // Set visibility to "false" if it is null
                        if (cleanedObj.getVisibility() == null) {
                            cleanedObj.setVisibility("true");
                        }

                        return cleanedObj;
                    } catch (Exception e) {
                        e.printStackTrace();  // Handle error gracefully
                        // Also apply default visibility on fallback
                        if (f.getVisibility() == null) {
                            f.setVisibility("true");
                        }
                        return f;
                    }
                })
                .filter(f ->
                        (f.getField() != null && !f.getField().isEmpty()) ||
                                (f.getLabel() != null && !f.getLabel().isEmpty()) ||
                                (f.getFrlabel() != null && !f.getFrlabel().isEmpty()) ||
                                (f.getIndex() != null && !f.getIndex().isEmpty()) ||
                                (f.getRequirement() != null && !f.getRequirement().isEmpty()) ||
                                (f.getFormat() != null && !f.getFormat().isEmpty()) ||
                                (f.getMin() != null && !f.getMin().isEmpty()) ||
                                (f.getMax() != null && !f.getMax().isEmpty()) ||
                                (f.getEnumvalue() != null && !f.getEnumvalue().isEmpty()) ||
                                (f.getDynamic() != null && !f.getDynamic().isEmpty()) ||
                                (f.getVisibility() != null && !f.getVisibility().isEmpty())  // This will always be true now
                )
                .collect(Collectors.toList());
    }

}
