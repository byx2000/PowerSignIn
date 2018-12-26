package com.example.powersignin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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
import com.example.powersignin.bean.Teacher;
import com.example.powersignin.bean.User;

import java.util.List;

public class StudentClassInfoActivity extends BaseActivity implements View.OnClickListener
{
    private static final String EXTRA_CLASSROOM_OBJECTID = "classroom_objectid";
    private static final String EXTRA_STUDENT_OBJECTID = "student_objectid";

    private Toolbar mToolbar;
    private TextView mClassroomNameTextView;
    private TextView mClassroomCodeTextView;
    private TextView mClassroomTeacherTextView;
    private Button mViewStudentsButton;
    private Button mSigninButton;

    private String mClassroomObjectId;
    private String mClassroomName;
    private String mClassroomTeacher;
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
        mClassroomCodeTextView = (TextView)findViewById(R.id.text_class_code);
        mClassroomTeacherTextView = (TextView)findViewById(R.id.text_class_teacher);
        //mViewStudentsButton = (Button)findViewById(R.id.btn_students);
        mSigninButton = (Button)findViewById(R.id.btn_signin);
    }

    @Override
    protected void initListeners()
    {
        //mViewStudentsButton.setOnClickListener(this);
        mSigninButton.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        setSupportActionBar(mToolbar);
        setToolbarTitle("班级信息");

        mClassroomObjectId = getIntent().getStringExtra(EXTRA_CLASSROOM_OBJECTID);
        findClassroom(mClassroomObjectId, new QueryListener<Classroom>()
        {
            @Override
            public void done(final Classroom classroom, BmobException e)
            {
                if (e == null)
                {
                    String teacherObjectId = classroom.getTeacher().getObjectId();
                    mClassroomName = classroom.getDescription();
                    mClassroomNameTextView.setText(mClassroomName);
                    mClassroomCodeTextView.setText(mClassroomObjectId);
                    if (classroom.isSignin())
                    {
                        //mSigninButton.setEnabled(true);
                        //mSigninButton.setText("点击签到");
                        //查找学生是否已签到
                        findSigninedStudents(classroom.getCurrentSigninEvent(), new FindListener<Student>()
                        {
                            @Override
                            public void done(List<Student> list, BmobException e)
                            {
                                if (e == null)
                                {
                                    mSigninButton.setEnabled(true);
                                    mSigninButton.setText("点击签到");
                                    for (Student student : list)
                                    {
                                        if (student.getObjectId().equals(mStudentObjectId))
                                        {
                                            mSigninButton.setEnabled(false);
                                            mSigninButton.setText("签到成功");
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
                        mSigninButton.setEnabled(false);
                        mSigninButton.setText("签到尚未开始");
                    }
                    //查找教师
                    findTeacherByTeacherObjectId(teacherObjectId, new QueryListener<Teacher>()
                    {
                        @Override
                        public void done(Teacher teacher, BmobException e)
                        {
                            if (e == null)
                            {

                                mClassroomTeacher = teacher.getNickname();
                                mClassroomTeacherTextView.setText(mClassroomTeacher);

                            }
                            else
                            {
                                toast("查找教师失败: " + e.getMessage());
                            }
                        }
                    });
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
                builder.setIcon(R.drawable.ic_launcher_foreground);
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
                        //toast("否");
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
        //查看同班同学
        if (v == mViewStudentsButton)
        {
            //查找该班级的所有学生
            findClassroomStudents(mClassroomObjectId, new FindListener<Student>()
            {
                @Override
                public void done(List<Student> list, BmobException e)
                {
                    //查找成功
                    if (e == null)
                    {
                        //设置对话框布局
                        View view = LayoutInflater.from(StudentClassInfoActivity.this).inflate(R.layout.dialog_classroom_students, null, false);
                        final TextView studentsTextView = view.findViewById(R.id.text_students);

                        //显示学生昵称
                        String s = "";
                        for (int i = 0; i < list.size(); ++i)
                        {
                            if (i != 0)
                            {
                                s += '\n';
                            }
                            s += list.get(i).getNickname();

                        }
                        studentsTextView.setText(s);

                        //打开学生列表对话框
                        final AlertDialog.Builder builder = new AlertDialog.Builder(StudentClassInfoActivity.this);
                        builder.setIcon(R.drawable.ic_launcher_foreground);
                        builder.setView(view);
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                            }
                        });
                        builder.show();
                    }
                    //查找失败
                    else
                    {
                        toast("查找学生信息失败: " + e.getMessage());
                    }
                }
            });
        }
        //进入签到页面
        else if (v == mSigninButton)
        {
            findClassroom(mClassroomObjectId, new QueryListener<Classroom>()
            {
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
                            toast("签到已停止");
                            mSigninButton.setEnabled(false);
                            mSigninButton.setText("签到尚未开始");
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_SIGNIN)
        {
            if (resultCode == RESULT_OK)
            {
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
}
