package com.example.powersignin.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class Student extends BmobObject
{
    //学生对应的用户
    private User user;

    //学生加入的班级(多个)
    private BmobRelation classrooms;

    //学生的昵称
    private String nickname;

    //面部图片地址
    private String faceImageUrl;

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public BmobRelation getClassrooms()
    {
        return classrooms;
    }

    public void setClassrooms(BmobRelation classrooms)
    {
        this.classrooms = classrooms;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public String getFaceImageUrl()
    {
        return faceImageUrl;
    }

    public void setFaceImageUrl(String faceImageUrl)
    {
        this.faceImageUrl = faceImageUrl;
    }
}
