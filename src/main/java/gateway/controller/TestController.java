package gateway.controller;

import gateway.response.BaseResponse;
import gateway.service.TestService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final Log log = LogFactory.getLog(TestController.class);

    @Autowired
    private TestService testService;

    @RequestMapping(
            value = {"/test", "/", "/ping"},
            method = RequestMethod.GET
    )
    public BaseResponse<String> ping() {
        log.debug("Running ping call");
        String data = testService.ping();

        return BaseResponse.<String>builder()
                .data(data)
                .build();
    }
}
