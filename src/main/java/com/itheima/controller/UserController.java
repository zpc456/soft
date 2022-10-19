package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.R;
import com.itheima.entity.User;
import com.itheima.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R<User> login(@RequestBody User user, HttpSession session) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, user.getPhone());
        User one = userService.getOne(queryWrapper);
        if (one == null) {
            one = new User();
            one.setPhone(user.getPhone());
            one.setStatus(1);
            userService.save(one);
            session.setAttribute("user", one.getId());
            return R.success(one);
        } else {
            session.setAttribute("user", one.getId());
            return R.success(user);
        }
    }

    @PostMapping("/loginout")
    public R<String> logintout() {
        return R.success("退出成功");
    }
}
