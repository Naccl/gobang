package top.naccl.gobang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.gobang.rabbitmq.MQSender;

/**
 * @program: gobang
 * @author: ChenJin
 * @create: 2021-10-29 10:04
 * @description: TODO
 */
@RestController
public class TestController {
    @Autowired
    private MQSender mqSender;

    @GetMapping("/test")
    public String test() {
        return "1";
    }
}
