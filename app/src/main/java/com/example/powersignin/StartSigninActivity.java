package com.example.powersignin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

public class StartSigninActivity extends BaseActivity implements View.OnClickListener
{
    private static final String EXTRA_CLASSROOM_NAME = "classroom_name";
    private static final String EXTRA_CLASSROOM_OBJECTID = "classroom_objectid";
    private static final String EXTRA_SIGNINEVENT_OBJECTID = "signinevent_objectid";

    private Toolbar mToolbar;
    private Button mStopSignin;
    private TextView mSigninResult;

    private String mClassroomName;
    private String mClassroomObjectId;
    private String mSigneventObjectId;

    private WifiUtil mWifiUtil;

    private boolean isSigninStop;

    public static Intent newIntent(Context context, String classroomName, String classroomObjectId, String signinEventObjectId)
    {
        Intent intent = new Intent(context, StartSigninActivity.class);
        intent.putExtra(EXTRA_CLASSROOM_NAME, classroomName);
        intent.putExtra(EXTRA_CLASSROOM_OBJECTID, classroomObjectId);
        intent.putExtra(EXTRA_SIGNINEVENT_OBJECTID, signinEventObjectId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*if (!mWifiUtil.isHotspotOpen())
        {
            //mStopSignin.setEnabled(false);
            isSigninStop = false;

            //打开wifi热点
            mWifiUtil.openHotspot(new WifiUtil.WifiHotspotOpenListener()
            {
                @Override
                public void succeed(String ssid)
                {
                    //mStopSignin.setEnabled(true);
                    //isSigninStop = false;
                    toast("热点开启成功");
                }

                @Override
                public void failed(String info)
                {
                    toast("热点开启失败，请手动开启热点");
                }
            });
        }*/
    }

    @Override
    protected void setContentView()
    {
        setContentView(R.layout.activity_start_sign_in);
    }

    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar_begin_sign_in_activity);
        mStopSignin = (Button)findViewById(R.id.btn_stop_sign_in);
        mSigninResult = (TextView)findViewById(R.id.text_signin_result);
    }

    @Override
    protected void initListeners()
    {
        mStopSignin.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        //设置Toolbar
        setSupportActionBar(mToolbar);
        mClassroomName = getIntent().getStringExtra(EXTRA_CLASSROOM_NAME);
        setToolbarTitle(mClassroomName + " 正在签到...");

        mClassroomObjectId = getIntent().getStringExtra(EXTRA_CLASSROOM_OBJECTID);
        mSigneventObjectId = getIntent().getStringExtra(EXTRA_SIGNINEVENT_OBJECTID);

        mWifiUtil = new WifiUtil(this);
    }

    @Override
    public void onClick(View v)
    {
        if (v == mStopSignin)
        {
            mStopSignin.setEnabled(false);

            //关闭wifi热点
            if (mWifiUtil.isHotspotOpen())
            {
                mWifiUtil.closeHotspot(new WifiUtil.WifiHotspotCloseListener()
                {
                    @Override
                    public void succeed()
                    {
                        toast("热点关闭成功");
                    }

                    @Override
                    public void failed(String info)
                    {
                        toast("热点关闭失败，请手动关闭热点");
                    }
                });
            }

            //停止签到
            setClassroomFinishSignin(mClassroomObjectId, new UpdateListener()
            {
                @Override
                public void done(BmobException e)
                {
                    if (e == null)
                    {
                        mStopSignin.setText("签到已停止");
                        setToolbarTitle(mClassroomName + " 签到已停止");
                        isSigninStop = true;

                        //查找缺席的学生
                        findAbsentStudents(mSigneventObjectId, new FindAbsentStudentListener()
                        {
                            @Override
                            public void succeed(List<Student> students)
                            {
                                String s = "";
                                if (students.size() > 0)
                                {
                                    s += "缺席学生:\n";
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

                                mSigninResult.setText(s);
                            }

                            @Override
                            public void failed(String info)
                            {
                                toast("获取签到结果失败，请检查网络状态，并重试: " + info);
                            }
                        });
                    }
                    else
                    {
                        toast("停止签到失败，请检查网络状态，并重试:" + e.getMessage());
                        mStopSignin.setEnabled(true);
                        isSigninStop = false;
                        //startNetWorkSettingActivity();
                    }
                }
            });
        }
    }

    //返回键按下时
    @Override
    public void onBackPressed()
    {
        if (isSigninStop)
        {
            super.onBackPressed();
        }
    }

    //监听热点开启状态的广播接收器
    /*class WifiBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED"))
            {
                //便携式热点的状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启
                int state = intent.getIntExtra("wifi_state",  0);

                //热点已开启
                if (state == 13)
                {
                    toast("热点开启成功");
                }
                //热点已关闭
                else if (state == 11)
                {
                    toast("热点开启成功");
                }
            }
        }
    }*/
}
