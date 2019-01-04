package com.example.powersignin;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import com.example.powersignin.Util.WifiUtil;
import com.example.powersignin.bean.Classroom;
import com.example.powersignin.bean.Student;

import java.util.List;

public class StudentClassInfoActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener
{
    private static final String EXTRA_CLASSROOM_OBJECTID = "classroom_objectid";
    private static final String EXTRA_STUDENT_OBJECTID = "student_objectid";
    public static final String EXTRA_POSITION = "position";

    private Toolbar mToolbar;
    private TextView mClassroomNameTextView;
    private TextView mClassroomCodeTextView;
    private TextView mClassroomTeacherTextView;
    private TextView mPrompt;
    private Button mSigninButton;
    private SwipeRefreshLayout mRefresh;
    private int mPosition;

    private String mClassroomObjectId;
    private String mClassroomName;
    private String mStudentObjectId;

    public static Intent newIntent(Context context, String classroomObjectId, String studentObjectId, int position)
    {
        Intent intent = new Intent(context, StudentClassInfoActivity.class);
        intent.putExtra(EXTRA_CLASSROOM_OBJECTID, classroomObjectId);
        intent.putExtra(EXTRA_STUDENT_OBJECTID, studentObjectId);
        intent.putExtra(EXTRA_POSITION, position);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mClassroomNameTextView = (TextView)findViewById(R.id.text_class_name);
        mClassroomCodeTextView = (TextView)findViewById(R.id.text_code);
        mClassroomTeacherTextView = (TextView)findViewById(R.id.text_class_teacher);
        mSigninButton = (Button)findViewById(R.id.btn_signin);
        mRefresh = (SwipeRefreshLayout)findViewById(R.id.refresh);
        mRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        mPrompt = (TextView)findViewById(R.id.text_prompt);
    }

    @Override
    protected void initListeners()
    {
        mSigninButton.setOnClickListener(this);
        mRefresh.setOnRefreshListener(this);
        mClassroomCodeTextView.setOnClickListener(this);
        mPrompt.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mClassroomObjectId = getIntent().getStringExtra(EXTRA_CLASSROOM_OBJECTID);
        mStudentObjectId = getIntent().getStringExtra(EXTRA_STUDENT_OBJECTID);
        updateData();
        mPosition = getIntent().getIntExtra(EXTRA_POSITION, -1);

        /*findClassroom(mClassroomObjectId, new QueryListener<Classroom>()
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
        });*/


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
                if (!new WifiUtil(this).isNetworkConnected())
                {
                    toast("请检查网络连接!");
                    return true;
                }

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
                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_POSITION, -1);
                                setResult(RESULT_OK, intent);
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
        if (!new WifiUtil(this).isNetworkConnected())
        {
            toast("请检查网络连接!");
            return;
        }

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
        //点击复制班级邀请码
        else if (v== mClassroomCodeTextView || v == mPrompt)
        {
            ClipboardManager clip = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            clip.setText(mClassroomCodeTextView.getText().toString().trim());
            toast("已复制到剪切板");
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
        mSigninButton.setTextColor(getResources().getColor(R.color.white));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setSigninButtonDisable()
    {
        mSigninButton.setEnabled(false);
        mSigninButton.setText("签到尚未开始");
        mSigninButton.setBackground(getDrawable(R.drawable.button_normal));
        mSigninButton.setTextColor(getResources().getColor(R.color.grey));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setSigninButtonSucceed()
    {
        mSigninButton.setEnabled(false);
        mSigninButton.setText("签到成功");
        mSigninButton.setTextColor(getResources().getColor(R.color.white));
        mSigninButton.setBackground(getDrawable(R.drawable.button_ok));
    }

    private void updateData()
    {
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

                mRefresh.setRefreshing(false);
            }
        });
    }

    //下拉刷新页面
    @Override
    public void onRefresh()
    {
        if (!new WifiUtil(this).isNetworkConnected())
        {
            toast("请检查网络连接!");
            mRefresh.setRefreshing(false);
            return;
        }

        updateData();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_POSITION, mPosition);
        setResult(RESULT_OK, intent);
        finish();
    }
}
