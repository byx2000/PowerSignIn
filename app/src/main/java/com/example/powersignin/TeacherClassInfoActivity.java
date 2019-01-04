package com.example.powersignin;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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
import cn.bmob.v3.listener.UpdateListener;
import com.example.powersignin.Util.WifiUtil;
import com.example.powersignin.bean.Classroom;
import com.example.powersignin.bean.Student;

import java.util.List;

public class TeacherClassInfoActivity extends BaseActivity implements View.OnClickListener
{
    public static final String EXTRA_CLASS_NAME = "class_name";
    public static final String EXTRA_CLASS_CODE = "class_code";
    public static final String EXTRA_POSITION = "position";

    private Toolbar mToolbar;
    private TextView mClassroomNameTextView;
    private TextView mClassroomCodeTextView;
    private TextView mIsSignin;
    private TextView mPrompt;
    private Button mViewStudentButton;
    private Button mViewSigninHistory;
    private Menu mMenu;

    private String mClassroomName;
    private String mClassroomObjectId;
    private String mSigninEventObjectId;

    private int position;

    private WifiUtil mWifiUtil;

    private boolean flag;

    public static Intent newIntent(Context context, String classroomName, String classroomObjectId, int position)
    {
        Intent intent = new Intent(context, TeacherClassInfoActivity.class);
        intent.putExtra(EXTRA_CLASS_NAME, classroomName);
        intent.putExtra(EXTRA_CLASS_CODE, classroomObjectId);
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
        setContentView(R.layout.activity_teacher_class_info);
    }

    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar_teacher_class_info_activity);
        mClassroomNameTextView = (TextView)findViewById(R.id.text_teacher_class_info_name);
        mClassroomCodeTextView = (TextView)findViewById(R.id.text_teacher_class_info_code);
        mViewStudentButton = (Button)findViewById(R.id.btn_view_student_info);
        mViewSigninHistory = (Button)findViewById(R.id.btn_sign_in_history);
        mIsSignin = (TextView)findViewById(R.id.text_issignin);
        mPrompt = (TextView)findViewById(R.id.text_prompt);
    }

    @Override
    protected void initListeners()
    {
        mViewStudentButton.setOnClickListener(this);
        mViewSigninHistory.setOnClickListener(this);
        mClassroomCodeTextView.setOnClickListener(this);
        mPrompt.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        //设置Toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //获取班级信息
        mClassroomName = getIntent().getStringExtra(EXTRA_CLASS_NAME);
        mClassroomObjectId = getIntent().getStringExtra(EXTRA_CLASS_CODE);

        //显示班级信息
        mClassroomNameTextView.setText("\t" + mClassroomName);
        mClassroomCodeTextView.setText("\t" + mClassroomObjectId);

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
                        mIsSignin.setText("\t正在签到");
                        mIsSignin.setTextColor(getResources().getColor(R.color.colorEmphasis));
                    }
                    else
                    {
                        mIsSignin.setText("\t未在签到");
                        mIsSignin.setTextColor(getResources().getColor(R.color.colorIgnore));
                    }
                }
                else
                {
                    toast("获取班级信息失败");
                }
            }
        });

        mWifiUtil = new WifiUtil(this);

        position = getIntent().getIntExtra(EXTRA_POSITION, -1);
    }

    //创建Toolbar菜单
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_teacher_class_info_activity_toolbar, menu);
        updateMenuItem(menu);
        return true;
    }

    //响应Toolbar菜单
    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.start_signin:
                if (!mWifiUtil.isNetworkConnected())
                {
                    toast("请检查网络连接!");
                    return true;
                }

                item.setEnabled(false);

                //检测当前是否处于签到状态
                findClassroom(mClassroomObjectId, new QueryListener<Classroom>()
                {
                    @Override
                    public void done(Classroom classroom, BmobException e)
                    {
                        if (e == null)
                        {
                            mSigninEventObjectId = classroom.getCurrentSigninEvent();
                            //处于签到状态
                            if (classroom.isSignin())
                            {
                                //停止签到
                                setClassroomFinishSignin(mClassroomObjectId, new UpdateListener()
                                {
                                    @RequiresApi(api = Build.VERSION_CODES.M)
                                    @Override
                                    public void done(BmobException e)
                                    {
                                        if (e == null)
                                        {
                                            item.setTitle("发起签到");
                                            item.setEnabled(true);
                                            mIsSignin.setText("\t未在签到");
                                            mIsSignin.setTextColor(getResources().getColor(R.color.colorIgnore));
                                            flag = false;
                                            if (mWifiUtil.isHotspotOpen())
                                            {
                                                toast("停止签到成功，请关闭热点");
                                                startWifiApSettingActivity();
                                            }
                                            else
                                            {
                                                toast("停止签到成功");
                                                //显示签到结果
                                                startSigninEventDetailActivity(mSigninEventObjectId);
                                            }
                                        }
                                        else
                                        {
                                            toast("停止签到失败，请重试: " + e.getMessage());
                                            item.setEnabled(true);
                                        }
                                    }
                                });
                            }
                            //不处于签到状态
                            else
                            {
                                //开始签到
                                String bssid = mWifiUtil.getBssid();

                                //新建签到事件
                                saveSigninEvent(mClassroomObjectId, bssid, new SaveSigninEventListener()
                                {
                                    @Override
                                    public void succeed(final String signinEventObjectId)
                                    {
                                        mSigninEventObjectId = signinEventObjectId;
                                        setClassroomSignin(mClassroomObjectId, signinEventObjectId, new UpdateListener()
                                        {
                                            @RequiresApi(api = Build.VERSION_CODES.M)
                                            @Override
                                            public void done(BmobException e)
                                            {
                                                if (e == null)
                                                {
                                                    item.setTitle("停止签到");
                                                    item.setEnabled(true);
                                                    mIsSignin.setText("\t正在签到");
                                                    mIsSignin.setTextColor(getResources().getColor(R.color.colorEmphasis));

                                                    flag = true;

                                                    if (!mWifiUtil.isHotspotOpen())
                                                    {
                                                        //打开系统热点设置界面
                                                        toast("发起签到成功，请开启热点");
                                                        startWifiApSettingActivity();
                                                    }
                                                    else
                                                    {
                                                        toast("发起签到成功");
                                                    }
                                                }
                                                else
                                                {
                                                    toast("发起签到失败，请检查网络，并重试: " + e.getMessage());
                                                    item.setEnabled(true);
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void failed(String info)
                                    {
                                        toast("发起签到失败，请检查网络，并重试: " + info);
                                        item.setEnabled(true);
                                    }
                                });
                            }
                        }
                        else
                        {
                            toast("查找班级失败，请重试: " + e.getMessage());
                            item.setEnabled(true);
                        }
                    }
                });
                break;
            case R.id.delete_class:
                if (!mWifiUtil.isNetworkConnected())
                {
                    toast("请检查网络连接!");
                    return true;
                }

                //显示删除警告对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.warning);
                builder.setTitle("删除班级");
                builder.setMessage("确定要删除该班级吗?");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        deleteClassroom(mClassroomObjectId, new DeleteClassroomListener()
                        {
                            @Override
                            public void succeed()
                            {
                                toast("删除成功");
                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_POSITION, -1);
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            @Override
                            public void failed(String info)
                            {
                                toast("删除失败: " + info);
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
        //查看学生信息
        if (v == mViewStudentButton)
        {
            if (!mWifiUtil.isNetworkConnected())
            {
                toast("请检查网络连接!");
                return;
            }

            //查找该班级的所有学生
            findClassroomStudents(mClassroomObjectId, new FindListener<Student>()
            {
                @Override
                public void done(final List<Student> list, BmobException e)
                {
                    //查找成功
                    if (e == null)
                    {
                        //设置对话框布局
                        View view = LayoutInflater.from(TeacherClassInfoActivity.this).inflate(R.layout.dialog_classroom_students, null, false);
                        final TextView studentsTextView = view.findViewById(R.id.text_students);

                        //显示学生昵称
                        if (list.size() == 0)
                        {
                            studentsTextView.setText("该班级无学生");
                        }
                        else
                        {
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
                        }


                        //打开学生列表对话框
                        final AlertDialog.Builder builder = new AlertDialog.Builder(TeacherClassInfoActivity.this);
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
        //查看签到历史
        else if (v == mViewSigninHistory)
        {
            if (!mWifiUtil.isNetworkConnected())
            {
                toast("请检查网络连接!");
                return;
            }

            startSigninHistoryActivity(mClassroomObjectId);
        }
        //点击复制班级邀请码
        else if (v == mClassroomCodeTextView || v == mPrompt)
        {
            ClipboardManager clip = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            clip.setText(mClassroomCodeTextView.getText().toString().trim());
            toast("已复制到剪切板");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_START_SIGNIN)
        {
            updateMenuItem(mMenu);
        }
        else if (requestCode == REQUEST_SYSTEM_WIFIAP_SETTING)
        {
            if (flag)
            {
                //热点已开启
                if (mWifiUtil.isHotspotOpen())
                {
                    toast("热点已开启");
                }
                //热点未开启
                else
                {
                    toast("热点未开启，请手动开启热点，否则会导致学生签到失败！");
                }
            }
            else
            {
                //热点未关闭
                if (mWifiUtil.isHotspotOpen())
                {
                    toast("热点未关闭，请手动关闭热点");
                }
                //热点已关闭
                else
                {
                    toast("热点已关闭");
                }

                //显示签到结果
                startSigninEventDetailActivity(mSigninEventObjectId);
            }
        }
    }

    private void updateMenuItem(final Menu menu)
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
                        menu.findItem(R.id.start_signin).setTitle("停止签到");
                    }
                    else
                    {
                        menu.findItem(R.id.start_signin).setTitle("发起签到");
                    }
                }
                else
                {

                }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_POSITION, position);
        setResult(RESULT_OK, intent);
        finish();
    }
}
