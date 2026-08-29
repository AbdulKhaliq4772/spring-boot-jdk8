package gateway.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse <T> {
    @Default private String status = "00";
    @Default private String message = "Success";
    @Default private Boolean isSuccess = true;
    @Default private T data = null;
}
