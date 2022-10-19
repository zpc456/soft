package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.mapper.DishMapper;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (int i = 0; i < flavors.size(); i++) {
            flavors.get(i).setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();
        Dish dish = this.getById(id);
        BeanUtils.copyProperties(dish, dishDto);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    public R<String> deleteWithFlavor(String ids) {
        String[] strings = ids.split(",");
        for (String string : strings) {
            Long id = Long.parseLong(string);
            DishDto dishDto = this.getByIdWithFlavor(id);
            for (DishFlavor flavor : dishDto.getFlavors()) {
                dishFlavorService.removeById(flavor);
            }
            this.removeById(id);
        }
        return R.success("删除成功");
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (int i = 0; i < flavors.size(); i++) {
            flavors.get(i).setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);
    }


}
