package com.example.powersignin;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import com.example.powersignin.Util.FileUtil;
import com.example.powersignin.bean.Student;
import com.example.powersignin.bean.Teacher;
import com.example.powersignin.bean.User;

import java.util.List;

public class LoginActivity extends BaseActivity implements View.OnClickListener
{
    private Toolbar mToolbar;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private TextView mSignupTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setContentView()
    {
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar_login_activity);
        mUsernameEditText = (EditText)findViewById(R.id.edit_username);
        mPasswordEditText = (EditText)findViewById(R.id.edit_password);
        mLoginButton = (Button)findViewById(R.id.btn_login);
        mSignupTextView = (TextView)findViewById(R.id.text_signup);
    }

    @Override
    protected void initListeners()
    {
        mLoginButton.setOnClickListener(this);
        mSignupTextView.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        setSupportActionBar(mToolbar);
        setToolbarTitle("登陆");
    }

    @Override
    public void onClick(View v)
    {
        //登陆
        if (v == mLoginButton)
        {
            mLoginButton.setEnabled(false);
            mLoginButton.setText("正在登陆...");

            final String username = mUsernameEditText.getText().toString();
            final String password = mPasswordEditText.getText().toString();
            login(username, password, new SaveListener<User>()
            {
                @Override
                public void done(final User user, BmobException e)
                {
                    //登陆成功
                    if (e == null)
                    {
                        //保存登录信息到本地
                        FileUtil.save(LoginActivity.this, username + " " + password, "login", new FileUtil.SaveFileListener()
                        {
                            @Override
                            public void succeed()
                            {

                            }

                            @Override
                            public void failed(String info)
                            {

                            }
                        });

                        //toast("登陆成功: " + user.getNickname() + " " + user.getType());
                        //用户类型为教师
                        if (user.getType().equals("teacher"))
                        {
                            //查找用户对应的教师id
                            findTeacherByUserObjectId(user.getObjectId(), new FindListener<Teacher>()
                            {
                                @Override
                                public void done(List<Teacher> list, BmobException e)
                                {
                                    //查找成功
                                    if (e == null)
                                    {
                                        //Log.d("TAG", user.getNickname());
                                        //打开教师主页
                                        startTeacherMainActivity(user.getUsername(), list.get(0).getObjectId(), user.getNickname());
                                        finish();
                                    }
                                    //查找失败
                                    else
                                    {
                                        toast("获取教师数据失败: " + e.getMessage());
                                    }
                                }
                            });
                        }
                        //用户类型为学生
                        else
                        {
                            //查找用户对应的学生id
                            findStudentByUserObjectId(user.getObjectId(), new FindListener<Student>()
                            {
                                @Override
                                public void done(List<Student> list, BmobException e)
                                {
                                    //查找成功
                                    if (e == null)
                                    {
                                        //打开学生主页
                                        startStudentMainActivity(user.getUsername(), list.get(0).getObjectId(), user.getNickname());
                                        finish();
                                    }
                                    //查找失败
                                    else
                                    {
                                        toast("获取学生数据失败: " + e.getMessage());
                                    }
                                }
                            });
                        }
                    }
                    //登陆失败
                    else
                    {
                        toast("登陆失败: " + e.getMessage());
                        mLoginButton.setEnabled(true);
                        mLoginButton.setText("登陆");
                    }
                }
            });
        }
        //注册
        else if (v == mSignupTextView)
        {
            startSignupActivity();
        }
    }
}
