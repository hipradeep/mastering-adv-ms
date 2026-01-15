package com.cdac.hpuat.issuetopatient.util;


import com.cdac.hpuat.issuetopatient.exception.PropertyCopyException;
import jakarta.persistence.EmbeddedId;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class BeanUtil {
    private BeanUtil() {
    }

    public static void copyProperties(Object source, Object target) {
         BeanUtils.copyProperties(source, target);
    }

    public static <S, T> T copyProperties(S source, Class<T> targetType) {
        if (source == null) {
            throw new IllegalArgumentException("Source must not be null");
        }

        T target;
        try {
            target = targetType.getDeclaredConstructor().newInstance();
             BeanUtils.copyProperties(source, target);
        } catch (Exception e) {
            throw new PropertyCopyException("Failed to create target instance or copy properties", e);
        }
        return target;
    }

    public static <S, T> List<T> copyListProperties(List<S> sourceList, Class<T> targetType) {
        if (sourceList == null) {
            throw new IllegalArgumentException("Source list must not be null");
        }

        return sourceList.stream()
                .map(source -> copyProperties(source, targetType))
                .toList();
    }

    public static <S, T> T copyPropertiesForEmb(S source, Class<T> targetType) {
        if (source == null) {
            throw new IllegalArgumentException("Source must not be null");
        }
        try {
            T target = targetType.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            copyEmbeddedId(source, target);
            return target;
        } catch (Exception e) {
            throw new PropertyCopyException("Failed to create target instance or copy properties", e);
        }
    }

    private static <S, T> void copyEmbeddedId(S source, T target) throws IllegalAccessException {
        for (Field field : source.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(EmbeddedId.class))
                continue;
            field.setAccessible(true);
            Object embeddedId = field.get(source);
            if (embeddedId != null) {
                applyEmbeddedFields(embeddedId, target);
            }
        }
    }

    private static <T> void applyEmbeddedFields(Object embeddedId, T target) throws IllegalAccessException {
        for (Field idField : embeddedId.getClass().getDeclaredFields()) {
            idField.setAccessible(true);
            Object value = idField.get(embeddedId);
            setIfWritable(target, idField.getName(), value);
        }
    }

    private static <T> void setIfWritable(T target, String fieldName, Object value) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, target.getClass());
            Method setter = pd.getWriteMethod();
            if (setter != null)
                setter.invoke(target, value);
        } catch (Exception e) {
            throw new PropertyCopyException("Failed to set field: " + fieldName, e);
        }
    }

    public static <S, T> List<T> copyListPropertiesForEmd(List<S> sourceList, Class<T> targetType) {
        if (sourceList == null) {
            throw new IllegalArgumentException("Source list must not be null");
        }

        return sourceList.stream()
                .map(source -> copyPropertiesForEmb(source, targetType))
                .toList();
    }
}
