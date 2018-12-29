package com.example.powersignin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import com.example.powersignin.bean.SigninEvent;
import com.example.powersignin.bean.Student;

import java.util.List;

public class SigninEventDetailActivity extends BaseActivity
{
    private static final String EXTRA_SIGNIN_EVENT_OBJECTID = "signin_event_objectid";

    private Toolbar mToolbar;
    private TextView mAbsent;

    private String mSigninEventObjectId;

    public static Intent newIntent(Context context, String signinEventObjectId)
    {
        Intent intent = new Intent(context, SigninEventDetailActivity.class);
        intent.putExtra(EXTRA_SIGNIN_EVENT_OBJECTID, signinEventObjectId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setContentView()
    {
        setContentView(R.layout.activity_signin_event_detail);
    }

    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //setToolbarTitle("签到详情");
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mAbsent = (TextView)findViewById(R.id.text_absent);
    }

    @Override
    protected void initListeners()
    {

    }

    @Override
    protected void initData()
    {
        mSigninEventObjectId = getIntent().getStringExtra(EXTRA_SIGNIN_EVENT_OBJECTID);
        //toast(mSigninEventObjectId);

        if (mSigninEventObjectId == null || mSigninEventObjectId.equals(""))
        {
            toast("获取签到结果失败，请在班级详情中点击“签到历史”查看");
            return;
        }

        //获取缺席的学生
        findAbsentStudents(mSigninEventObjectId, new FindAbsentStudentListener()
        {
            @Override
            public void succeed(List<Student> students)
            {
                String s = "";
                if (students.size() > 0)
                {
                    for (Student student : students)
                    {
                        s += student.getNickname();
                        s += "\n";
                    }
                }
                else
                {
                    s = "无学生缺席";
                }

                mAbsent.setText(s);

                SigninEvent event = new SigninEvent();
                event.setObjectId(mSigninEventObjectId);
                event.setAbsentStudentsCount(Integer.toString(students.size()));
                event.update(new UpdateListener()
                {
                    @Override
                    public void done(BmobException e)
                    {

                    }
                });
            }

            @Override
            public void failed(String info)
            {
                toast("获取签到详情失败: " + info);
            }
        });
    }
}
