package com.itheima.controller;

import com.itheima.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/OrderDetail")
public class OrderDetailController {
    @Autowired
    private OrderDetailService orderDetailService;
}
