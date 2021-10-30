package top.naccl.gobang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.gobang.rabbitmq.MQSender;

/**
 * @program: Gobang
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

    public static void main(String[] args) {
        aa aa = new aa();
    }
}


interface a{
    void d();
}


interface b extends a{
    void c();
}


class aa implements a{
    @Override
    public void d() {
        System.out.println("aa");
    }
}


class dd implements b{

    @Override
    public void d() {
        System.out.println("AAA");
    }

    @Override
    public void c() {
        System.out.println("bb");
    }
}