package com.example.powersignin.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class Teacher extends BmobObject
{
    //教师对应的用户
    private User user;

    //教师管理的班级(多个)
    private BmobRelation classrooms;

    //面部图片地址
    private String faceImageUrl;

    //教师的昵称
    private String nickname;

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
