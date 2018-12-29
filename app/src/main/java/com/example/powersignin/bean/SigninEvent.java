package com.example.powersignin.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class SigninEvent extends BmobObject
{
    //发起签到的班级
    private Classroom classroom;

    //缺席的学生
    private BmobRelation signedStudents;

    //教师机的bssid
    private String bssid;

    //缺席学生的数量
    private String absentStudentsCount;

    public Classroom getClassroom()
    {
        return classroom;
    }

    public void setClassroom(Classroom classroom)
    {
        this.classroom = classroom;
    }

    public BmobRelation getSignedStudents()
    {
        return signedStudents;
    }

    public void setSignedStudents(BmobRelation signedStudents)
    {
        this.signedStudents = signedStudents;
    }

    public String getBssid()
    {
        return bssid;
    }

    public void setBssid(String bssid)
    {
        this.bssid = bssid;
    }

    public String getAbsentStudentsCount()
    {
        return absentStudentsCount;
    }

    public void setAbsentStudentsCount(String absentStudentsCount)
    {
        this.absentStudentsCount = absentStudentsCount;
    }
}
