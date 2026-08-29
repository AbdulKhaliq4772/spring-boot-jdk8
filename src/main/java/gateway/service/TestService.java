package gateway.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    private final Log log = LogFactory.getLog(TestService.class);

    public String ping() {
        log.debug("Running ping call");
        return "Test successful";
    }

}
