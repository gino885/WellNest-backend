package com.wellnest.dto;

public class CreateThreadRequest {

    private String name;
    private int age;
    private String gender;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public String CreateMessage(){
        return String.format("我叫做 %s, 我的性別是 %s, 我的年齡是 %d 請根據我的基本資訊客製化回答",name, gender, age);
    }

}
