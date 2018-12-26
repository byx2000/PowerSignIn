package com.example.powersignin.bean;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser
{
    public static final String TEACHER = "teacher";
    public static final String STUDENT = "student";

    //父类属性:
    //String username; //用户名为手机号
    //String password

    //teacher为教师, student为学生
    private String type;

    //面部图片地址
    private String faceImageUrl;

    //用户昵称
    private String nickname;

    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
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
