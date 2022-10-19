package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;

public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);

    public void updateWithFlavor(DishDto dishDto);

    public R<String> deleteWithFlavor(String ids);
}
