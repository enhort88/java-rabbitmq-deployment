package com.example.rabbitmqdemo.commands;

import com.example.rabbitmqdemo.dto.User;
import com.example.rabbitmqdemo.meta.CommandMethod;
import org.springframework.stereotype.Service;

@Service
public class MyCommandService {

    @CommandMethod
    public void doSomething(String name, Integer count) {
        System.out.println("📢 Выполнение: doSomething с name=" + name + ", count=" + count);
    }

    @CommandMethod
    public void hello() {
        System.out.println("👋 Hello!👋");
    }
    @CommandMethod
    public void hello(String name) {
        System.out.println("👋 Hello (String) "+name+"!");
    }
    @CommandMethod
    public void hello(int name) {
        System.out.println("👋 Hello (int) "+name+"!");
    }

    @CommandMethod
    public void counted(Integer a, Integer b) {
        System.out.println("👋 Count (Integer) a + b = " + (a + b));
    }
    @CommandMethod
    public void counted(Double a, Double b) {
        System.out.println("👋 Count (Double) a + b = " + (a + b));
    }

    @CommandMethod
    public void createUser(User user) {
        System.out.println("👤 Пользователь: " + user.getName() + ", возраст: " + user.getAge());
    }
    @CommandMethod
    public void createUser(User user, String name) {
        System.out.println("👤 Пользователь: " + user.getName()+"+" + name +"=fRIENdsHIP"+ ", возраст: " + user.getAge());
    }

}
