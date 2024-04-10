package com.wellnest.user.model;

import com.wellnest.user.enmus.Gender;
import jakarta.persistence.*;

import java.util.Date;

@Entity // 標記為JPA實體
@Table(name = "user") // 定義對應的表名為users
public class User {
    @Id // 标记为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 标识符生成策略为数据库自增
    private Integer userId;
    private String email;
    private String name;
    private String password;

    private Integer age;
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "last_modified_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Enumerated(EnumType.STRING) // 使用字符串形式存储枚举值
    private Gender gender; // 性别字段

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getUserId() {
        return userId;
    }

    // Corrected setter method for userId
    public void setUserId(Integer userId) {
        this.userId = userId; // Correctly assigns the input parameter to the instance variable
    }

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

}
