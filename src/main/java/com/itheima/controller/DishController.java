package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping()
    public R<String> save(@RequestBody DishDto dishDto) {
        String key = "dish_" + dishDto.getCategoryId()+ "_" + dishDto.getStatus();
        redisTemplate.delete(key);
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = new ArrayList<>();
        for (Dish record : records) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(record, dishDto);

            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (null != category) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
                list.add(dishDto);
            }
        }

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    @PutMapping()
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        String key = "dish_" + dishDto.getCategoryId()+ "_" + dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("修改菜品成功!");
    }

    @DeleteMapping
    public R<String> delete(String ids) {
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return dishService.deleteWithFlavor(ids);
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, String ids) {
        String[] strings = ids.split(",");
        for (String string : strings) {
            Long id = Long.parseLong(string);
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        if (status == 0) return R.success("停售成功");
        else return R.success("启售成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;
        String key = "dish_" + dish.getCategoryId()+ "_" + dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList != null) return R.success(dishDtoList);
        dishDtoList = new ArrayList<>();
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(queryWrapper);
        for (Dish dish1 : dishList) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish1, dishDto);
            Long categoryId = dish1.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            Long dish1Id = dish1.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dish1Id);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            dishDtoList.add(dishDto);
        }
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}
