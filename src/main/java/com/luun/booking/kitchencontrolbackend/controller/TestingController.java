package com.luun.booking.kitchencontrolbackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestingController {

    @GetMapping(path = {"/", "/test"})
    public String testPage() {
        return "index";
    }

}
