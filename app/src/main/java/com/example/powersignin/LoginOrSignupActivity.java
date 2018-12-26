package com.example.powersignin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import cn.bmob.v3.exception.BmobException;
import com.example.powersignin.bean.Teacher;
import com.example.powersignin.bean.User;

public class LoginOrSignupActivity extends BaseActivity implements View.OnClickListener
{
    private Toolbar loginOrSignupActivityToolbar;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private RadioButton studentRadioButton;
    private RadioButton teacherRadioButton;
    private Button loginButton;
    private Button signupButton;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setContentView()
    {
        setContentView(R.layout.activity_login_or_signup);
    }

    @Override
    protected void initViews()
    {
        loginOrSignupActivityToolbar = (Toolbar)findViewById(R.id.toolbar_login_or_signup_activity);
        usernameEditText = (EditText)findViewById(R.id.edit_username);
        passwordEditText = (EditText)findViewById(R.id.edit_password);
        studentRadioButton = (RadioButton)findViewById(R.id.radio_student);
        teacherRadioButton = (RadioButton)findViewById(R.id.radio_teacher);
        loginButton = (Button)findViewById(R.id.btn_login);
        signupButton = (Button)findViewById(R.id.btn_signup);
    }

    @Override
    protected void initListeners()
    {
        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        //设置Toolbar
        setSupportActionBar(loginOrSignupActivityToolbar);
        setToolbarTitle("登陆/注册");

        //默认选中学生
        studentRadioButton.setChecked(true);
    }

    //按钮点击事件
    @Override
    public void onClick(View v)
    {
        //登陆按钮
        if (v == loginButton)
        {
            //toast("准备登陆");
            /*String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            login(username, password, new LoginListener()
            {
                //登陆成功
                @Override
                public void succeed(final User user)
                {
                    //用户类型为教师
                    if (user.getType().equals("teacher"))
                    {
                        //获取教师objectId
                        findTeacherByUserObjectId(user.getObjectId(), new FindTeacherListener()
                        {
                            @Override
                            public void succeed(Teacher teacher)
                            {
                                startTeacherMainActivity(user.getUsername(), teacher.getObjectId());
                            }

                            @Override
                            public void failed(BmobException e)
                            {
                                toast("查找教师失败: " + e.getMessage());
                            }
                        });
                    }
                    //用户类型为学生
                    else if (user.getType().equals("student"))
                    {

                    }
                }

                //登陆失败
                @Override
                public void failed(BmobException e)
                {
                    toast("登陆失败: " + e.getMessage());
                }
            });*/
        }
        //注册按钮
        else if (v == signupButton)
        {
            /*String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String type = "";
            if (studentRadioButton.isChecked())
            {
                type = "student";
            }
            else
            {
                type = "teacher";
            }
            signup(username, password, type, new SignupListener()
            {
                @Override
                public void succeed(User user)
                {
                    toast("注册成功: " + user.getUsername());
                }

                @Override
                public void failed(String info)
                {
                    toast("注册失败: " + info);
                }
            });*/
        }
    }

    /*private void startTeacherMainActivity(String teacherUsername, String teacherObjectId)
    {
        Intent intent = TeacherMainActivity.newIntent(this, teacherUsername, teacherObjectId);
        startActivity(intent);
    }*/


}
