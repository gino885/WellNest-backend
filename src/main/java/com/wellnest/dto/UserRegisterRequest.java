package com.wellnest.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

public class UserRegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    private String status; // Assuming that the status is not required for registration

    private String gender; // Should be 'Male', 'Female', or 'Other'

    @NotNull
    @PositiveOrZero
    private Integer age; // Assuming that age cannot be negative

    private Integer avatarNum; // Assuming that the avatar number is not required for registration

    // Getters and setters for all fields

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getAvatarNum() {
        return avatarNum;
    }

    public void setAvatarNum(Integer avatarNum) {
        this.avatarNum = avatarNum;
    }
}
