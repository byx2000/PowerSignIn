package com.example.powersignin;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.powersignin.Util.WifiUtil;

public class JoinClassActivity extends BaseActivity implements View.OnClickListener
{
    private static final String EXTRA_STUDENT_OBJECTID = "student_objectid";

    private Toolbar mToolbar;
    private EditText mClassCodeEditText;
    private Button mJoinClassButton;

    private String mStudentObjectId;

    public static Intent newIntent(Context context, String studentObjectId)
    {
        Intent intent = new Intent(context, JoinClassActivity.class);
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
        setContentView(R.layout.activity_join_class);
    }

    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mClassCodeEditText = (EditText)findViewById(R.id.edit_class_code);
        mJoinClassButton = (Button)findViewById(R.id.btn_join_class);
    }

    @Override
    protected void initListeners()
    {
        mJoinClassButton.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mStudentObjectId = getIntent().getStringExtra(EXTRA_STUDENT_OBJECTID);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v)
    {
        if (!new WifiUtil(this).isNetworkConnected())
        {
            toast("请检查网络连接!");
            return;
        }

        disableJoinClassButton();

        String classroomObjectId = mClassCodeEditText.getText().toString();
        joinClassroom(mStudentObjectId, classroomObjectId, new JoinClassroomListener()
        {
            @Override
            public void succeed()
            {
                toast("加入班级成功");
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void failed(String info)
            {
                toast("加入班级失败: " + info);
                //mJoinClassButton.setEnabled(true);
                //mJoinClassButton.setText("加入班级");
                enableJoinClassButton();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disableJoinClassButton()
    {
        mJoinClassButton.setEnabled(false);
        mJoinClassButton.setTextColor(getResources().getColor(R.color.grey));
        mJoinClassButton.setText("正在加入...");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void enableJoinClassButton()
    {
        mJoinClassButton.setEnabled(true);
        mJoinClassButton.setTextColor(getResources().getColor(R.color.white));
        mJoinClassButton.setText("加入班级");
    }
}
