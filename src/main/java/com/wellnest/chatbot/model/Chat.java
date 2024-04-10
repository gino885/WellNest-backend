package com.wellnest.chatbot.model;
import com.wellnest.user.model.User;
import jakarta.persistence.*;
import java.util.Date;
@Entity // 标记这个类为JPA实体
@Table(name = "chat") // 指定映射到数据库表的名称
public class Chat {
    @Id // 标记为主键字段
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自动增长策略
    private Integer ChatId;
    @Column(unique = true) // UUID 应该是唯一的
    private String UUId;
    private String status;
    private String content;
    @Temporal(TemporalType.TIMESTAMP) // 指定日期时间的精确度
    private Date date;
//    private Integer userId;
    @ManyToOne(fetch = FetchType.LAZY) // 多对一关系，用户可能有多个聊天记录
    @JoinColumn(name = "userId", referencedColumnName = "userId") // 外键为userId，引用用户表的userId字段

    private User user;
    // 添加一个新的属性
    @Column(name = "threadID")
    private String threadID;

    public String getThreadID() {
        return threadID;
    }

    public void setThreadID(String threadID) {
        this.threadID = threadID;
    }

    public Integer getChatId() {
        return ChatId;
    }

    public void setChatId(Integer chatId) {
        ChatId = chatId;
    }

    public String getUUId() {
        return UUId;
    }

    public void setUUId(String UUId) {
        this.UUId = UUId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User userId) {
        this.user = user;
    }
}
