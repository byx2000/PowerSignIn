package com.example.powersignin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import com.example.powersignin.bean.Classroom;
import com.example.powersignin.bean.Student;

import java.util.List;

public class StudentClassInfoActivity extends BaseActivity implements View.OnClickListener
{
    private static final String EXTRA_CLASSROOM_OBJECTID = "classroom_objectid";
    private static final String EXTRA_STUDENT_OBJECTID = "student_objectid";

    private Toolbar mToolbar;
    private TextView mClassroomNameTextView;
    private TextView mClassroomCodeTextView;
    private TextView mClassroomTeacherTextView;
    private Button mSigninButton;

    private String mClassroomObjectId;
    private String mClassroomName;
    private String mStudentObjectId;

    public static Intent newIntent(Context context, String classroomObjectId, String studentObjectId)
    {
        Intent intent = new Intent(context, StudentClassInfoActivity.class);
        intent.putExtra(EXTRA_CLASSROOM_OBJECTID, classroomObjectId);
        intent.putExtra(EXTRA_STUDENT_OBJECTID, studentObjectId);
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
        setContentView(R.layout.activity_student_class_info);
    }

    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mClassroomNameTextView = (TextView)findViewById(R.id.text_class_name);
        mClassroomCodeTextView = (TextView)findViewById(R.id.text_code);
        mClassroomTeacherTextView = (TextView)findViewById(R.id.text_class_teacher);
        mSigninButton = (Button)findViewById(R.id.btn_signin);
    }

    @Override
    protected void initListeners()
    {
        mSigninButton.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mClassroomObjectId = getIntent().getStringExtra(EXTRA_CLASSROOM_OBJECTID);
        findClassroom(mClassroomObjectId, new QueryListener<Classroom>()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void done(final Classroom classroom, BmobException e)
            {
                if (e == null)
                {
                    mClassroomName = classroom.getDescription();
                    mClassroomNameTextView.setText("\t" + mClassroomName);
                    mClassroomCodeTextView.setText("\t" + mClassroomObjectId);
                    mClassroomTeacherTextView.setText("\t" + classroom.getTeacherNickname());
                    if (classroom.isSignin())
                    {
                        //查找学生是否已签到
                        findSigninedStudents(classroom.getCurrentSigninEvent(), new FindListener<Student>()
                        {
                            @Override
                            public void done(List<Student> list, BmobException e)
                            {
                                if (e == null)
                                {
                                    setSigninButtonEnable();
                                    for (Student student : list)
                                    {
                                        if (student.getObjectId().equals(mStudentObjectId))
                                        {
                                            setSigninButtonSucceed();
                                            break;
                                        }
                                    }
                                }
                                else
                                {
                                    toast("获取签到信息失败: " + e.getMessage());
                                    finish();
                                }
                            }
                        });
                    }
                    else
                    {
                        setSigninButtonDisable();
                    }
                }
                else
                {
                    toast("查找班级失败: " + e.getMessage());
                }
            }
        });

        mStudentObjectId = getIntent().getStringExtra(EXTRA_STUDENT_OBJECTID);
    }

    //创建Toolbar菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_student_class_info_activity_toolbar, menu);
        return true;
    }

    //响应Toolbar菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.quit_class:
                //显示退出警告对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.warning);
                builder.setTitle("退出班级");
                builder.setMessage("确定要退出该班级吗?");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //toast("是");
                        quitClassroom(mStudentObjectId, mClassroomObjectId, new QuitClassroomListener()
                        {
                            @Override
                            public void succeed()
                            {
                                toast("退出班级成功");
                                setResult(RESULT_OK);
                                finish();
                            }

                            @Override
                            public void failed(String info)
                            {
                                toast("退出班级失败: " + info);
                            }
                        });
                    }
                });
                builder.setNegativeButton("否", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                builder.show();
                break;
        }

        return true;
    }

    @Override
    public void onClick(View v)
    {
        //进入签到页面
        if (v == mSigninButton)
        {
            findClassroom(mClassroomObjectId, new QueryListener<Classroom>()
            {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void done(Classroom classroom, BmobException e)
                {
                    if (e == null)
                    {
                        if (classroom.isSignin())
                        {
                            startSigninActivity(mClassroomObjectId, mStudentObjectId);
                        }
                        else
                        {
                            setSigninButtonDisable();
                        }
                    }
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_SIGNIN)
        {
            if (resultCode == RESULT_OK)
            {
                mSigninButton.setBackground(getDrawable(R.drawable.button_ok));
                mSigninButton.setEnabled(false);
                mSigninButton.setText("签到成功");
                toast("签到成功");
            }
            else
            {
                toast("签到失败");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setSigninButtonEnable()
    {
        mSigninButton.setEnabled(true);
        mSigninButton.setText("点击签到");
        mSigninButton.setTextColor(getColor(R.color.white));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setSigninButtonDisable()
    {
        mSigninButton.setEnabled(false);
        mSigninButton.setText("签到尚未开始");
        mSigninButton.setTextColor(getColor(R.color.grey));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setSigninButtonSucceed()
    {
        mSigninButton.setEnabled(false);
        mSigninButton.setText("签到成功");
        mSigninButton.setTextColor(getColor(R.color.white));
        mSigninButton.setBackground(getDrawable(R.drawable.button_ok));
    }
}
