package com.cdac.hpuat.inventory.util;

import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.stream.Collectors;

public class BeanUtil {

    private BeanUtil() {
    }

    public static <S, T> T copyProperties(S source, Class<T> targetClass) {
        if (source == null)
            return null;
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }
    }

    public static <S, T> List<T> copyListProperties(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null)
            return null;
        return sourceList.stream()
                .map(source -> copyProperties(source, targetClass))
                .collect(Collectors.toList());
    }
}
