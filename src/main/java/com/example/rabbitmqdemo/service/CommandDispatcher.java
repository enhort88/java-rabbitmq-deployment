package com.example.rabbitmqdemo.service;

import com.example.rabbitmqdemo.meta.MethodBinding;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CommandDispatcher {

    private final Map<String, List<MethodBinding>> methodRegistry = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void register(String name, Object bean, Method method) {
        methodRegistry.computeIfAbsent(name, k -> new ArrayList<>())
                .add(new MethodBinding(bean, method));
        System.out.println("✅ Зарегистрирован метод: " + name + " (" + method.getParameterCount() + " параметров)");
    }

    public void dispatch(String methodName, Map<String, Object> params) {
        var candidates = methodRegistry.get(methodName);
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("🚫 Команда '" + methodName + "' не зарегистрирована.");
        }
        MethodBinding bestBinding = null;
        Object[] bestArgs = null;

        for (MethodBinding binding : candidates) {
            Method method = binding.method();
            Parameter[] parameters = method.getParameters();
            if (parameters.length == 0 && !params.isEmpty()) continue;

            Object[] args = new Object[parameters.length];
            boolean matched = true;

            for (int i = 0; i < parameters.length; i++) {
                Parameter p = parameters[i];
                String paramName = p.getName();

                if (!params.containsKey(paramName)) {
                    matched = false;
                    break;
                }

                try {
                    args[i] = convertValueStrict(params.get(paramName), p.getType());
                } catch (Exception e) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                if (bestBinding == null || method.getParameterCount() > bestBinding.method().getParameterCount()) {
                    bestBinding = binding;
                    bestArgs = args;
                }
            }
        }

        if (bestBinding != null) {
            try {
                bestBinding.method().invoke(bestBinding.bean(), bestArgs);
                return;
            } catch (Exception e) {
                throw new RuntimeException("❌ Ошибка вызова метода '" + methodName + "': " + e.getMessage(), e);
            }
        }

        throw new IllegalArgumentException("🚫 Ни один метод '" + methodName + "' не соответствует переданным параметрам: " + params.keySet());
    }

    private Object convertValueStrict(Object value, Class<?> type) {
        if (value == null) return null;

        // 📌 Строгое соответствие для String
        if (type == String.class) {
            if (!(value instanceof String)) {
                throw new IllegalArgumentException("Expected String, got " + value.getClass());
            }
            return value;
        }

        // 📌 Строгое соответствие для Integer
        if (type == Integer.class || type == int.class) {
            if (!value.getClass().equals(Integer.class)) {
                throw new IllegalArgumentException("Expected Integer, got " + value.getClass());
            }
            return value;
        }

        // 📌 Строгое соответствие для Long
        if (type == Long.class || type == long.class) {
            if (!value.getClass().equals(Long.class)) {
                throw new IllegalArgumentException("Expected Long, got " + value.getClass());
            }
            return value;
        }

        // 📌 Строгое соответствие для Double
        if (type == Double.class || type == double.class) {
            if (!value.getClass().equals(Double.class)) {
                throw new IllegalArgumentException("Expected Double, got " + value.getClass());
            }
            return value;
        }

        // 📌 DTO: если это Map → пробуем конвертировать
        if (value instanceof Map<?, ?>) {
            Object converted = objectMapper.convertValue(value, type);
            if (!type.isInstance(converted)) {
                throw new IllegalArgumentException("Failed to convert to " + type.getSimpleName());
            }
            return converted;
        }

        // 📌 Если уже правильный объект (например, передан как готовый User)
        if (type.isAssignableFrom(value.getClass())) {
            return value;
        }

        throw new IllegalArgumentException("Unsupported conversion from " + value.getClass() + " to " + type);
    }


    private boolean isSameOrBoxedType(Class<?> actual, Class<?> expected) {
        if (actual.equals(expected)) return true;

        // авто-boxing совместимость
        if (expected == int.class && actual == Integer.class) return true;
        if (expected == Integer.class && actual == int.class) return true;

        if (expected == long.class && actual == Long.class) return true;
        if (expected == Long.class && actual == long.class) return true;

        if (expected == double.class && actual == Double.class) return true;
        if (expected == Double.class && actual == double.class) return true;

        return false;
    }

}
