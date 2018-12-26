package com.example.powersignin;

import android.os.Bundle;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import com.example.powersignin.Util.FileUtil;
import com.example.powersignin.bean.Student;
import com.example.powersignin.bean.Teacher;
import com.example.powersignin.bean.User;

import java.util.List;

public class LaunchActivity extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*startLoginActivity();
        finish();*/

        //读取登陆信息
        FileUtil.load(this, "login", new FileUtil.LoadFileListener()
        {
            //有登录信息 直接登陆
            @Override
            public void succeed(String content)
            {
                if (content.equals(""))
                {
                    startLoginActivity();
                    finish();
                    return;
                }

                //提取用户名和密码
                String[] info = content.split(" ");
                String username = info[0];
                String password = info[1];

                //登陆
                login(username, password, new SaveListener<User>()
                {
                    @Override
                    public void done(final User user, BmobException e)
                    {
                        //登陆成功
                        if (e == null)
                        {
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
                                            toast("登陆失败，请重新登陆: " + e.getMessage());
                                            //显示登陆页面
                                            startLoginActivity();
                                            finish();
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
                                            toast("登陆失败，请重新登陆: " + e.getMessage());
                                            //显示登陆页面
                                            startLoginActivity();
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                        //登陆失败
                        else
                        {
                            //显示登陆页面
                            startLoginActivity();
                            finish();
                        }
                    }
                });
            }

            //无登录信息 显示登陆页面
            @Override
            public void failed(String info)
            {
                startLoginActivity();
                finish();
            }
        });
    }

    @Override
    protected void setContentView()
    {
        setContentView(R.layout.activity_launch);
    }

    @Override
    protected void initViews()
    {

    }

    @Override
    protected void initListeners()
    {

    }

    @Override
    protected void initData()
    {

    }
}
