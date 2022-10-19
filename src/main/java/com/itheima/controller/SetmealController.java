package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("新增成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> setmealDtoList = new ArrayList<>();
        setmealService.page(pageInfo, queryWrapper);
        for (Setmeal record : records) {
            SetmealDto setmealDto = new SetmealDto();
            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                BeanUtils.copyProperties(record, setmealDto);
                setmealDto.setCategoryName(categoryName);
                setmealDtoList.add(setmealDto);
            }
        }
        setmealDtoPage.setRecords(setmealDtoList);
        return R.success(setmealDtoPage);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "setmealCache", key = "#id")
    public R<SetmealDto> getById(@PathVariable Long id) {
        SetmealDto setmealDto = new SetmealDto();
        Setmeal setmeal = setmealService.getById(id);
        BeanUtils.copyProperties(setmeal, setmealDto);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishList);
        return R.success(setmealDto);
    }

    @PutMapping()
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateById(setmealDto);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (int i = 0; i < setmealDishes.size(); i++) {
            setmealDishes.get(i).setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
        return R.success("修改菜品成功!");
    }

    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        setmealService.removeWithDish(ids);
        // log.info("\n\n\n\n" + ids + "\n\n\n\n\n\n");
        return R.success("删除成功");
    }

    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> updateStatus(@PathVariable int status, @RequestParam List<Long> ids) {
        log.info("status -->{}, list-->{}", status, ids);
        setmealService.updateStatus(status, ids);
        return R.success("修改成功");
    }

    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return R.success(setmealList);
    }
}
