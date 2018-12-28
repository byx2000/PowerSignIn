package com.example.powersignin.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class Classroom extends BmobObject
{
    //班级描述
    private String description;

    //管理该班级的教师
    private Teacher teacher;

    //该班级的学生(多个)
    private BmobRelation students;

    //是否正在签到
    private Boolean isSignin;

    //班级的签到事件
    private BmobRelation signinEvents;

    //当前正在进行的签到
    private String currentSigninEvent;

    //班级教师的昵称
    private String teacherNickname;

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Teacher getTeacher()
    {
        return teacher;
    }

    public void setTeacher(Teacher teacher)
    {
        this.teacher = teacher;
    }

    public BmobRelation getStudents()
    {
        return students;
    }

    public void setStudents(BmobRelation students)
    {
        this.students = students;
    }

    public boolean isSignin()
    {
        return isSignin;
    }

    public void setSignin(boolean signin)
    {
        isSignin = signin;
    }

    public BmobRelation getSigninEvents()
    {
        return signinEvents;
    }

    public void setSigninEvents(BmobRelation signinEvents)
    {
        this.signinEvents = signinEvents;
    }

    public String getCurrentSigninEvent()
    {
        return currentSigninEvent;
    }

    public void setCurrentSigninEvent(String currentSigninEvent)
    {
        this.currentSigninEvent = currentSigninEvent;
    }

    public String getTeacherNickname()
    {
        return teacherNickname;
    }

    public void setTeacherNickname(String teacherNickname)
    {
        this.teacherNickname = teacherNickname;
    }
}
