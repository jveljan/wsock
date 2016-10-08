package org.wsock.spring;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * Created by joco on 08.10.16.
 */
@Controller
public class JsExposeController {
    @RequestMapping(value = "${org.wsock.js:/wsock.js}", method = RequestMethod.GET)
    @ResponseBody
    public Resource jsFile() throws IOException {
        return new ClassPathResource("/wsock.js", JsExposeController.class);
    }
}
