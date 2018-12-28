package com.example.powersignin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import com.example.powersignin.bean.Teacher;

public class NewClassActivity extends BaseActivity implements View.OnClickListener
{
    private static final String EXTRA_TEACHER_OBJECTID = "teacher_objectid";

    private Toolbar newClassActivityToolbar;
    private EditText classroomDescriptionTextView;
    private Button createClassroomButton;

    private String teacherObjectId;

    public static Intent newIntent(Context context, String teacherObjectId)
    {
        Intent intent = new Intent(context, NewClassActivity.class);
        intent.putExtra(EXTRA_TEACHER_OBJECTID, teacherObjectId);
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
        setContentView(R.layout.activity_new_class);
    }

    @Override
    protected void initViews()
    {
        newClassActivityToolbar = (Toolbar)findViewById(R.id.toolbar_new_class_activity);
        classroomDescriptionTextView = (EditText)findViewById(R.id.edit_new_class_description);
        createClassroomButton = (Button)findViewById(R.id.btn_new_classroom);
    }

    @Override
    protected void initListeners()
    {
        createClassroomButton.setOnClickListener(this);
    }

    @Override
    protected void initData()
    {
        //设置Toolbar
        setSupportActionBar(newClassActivityToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //setToolbarTitle("新建班级");

        teacherObjectId = getIntent().getStringExtra(EXTRA_TEACHER_OBJECTID);
    }

    @Override
    public void onClick(View v)
    {
        final String description = classroomDescriptionTextView.getText().toString();

        if (description.equals(""))
        {
            toast("请输入班级描述");
            return;
        }

        createClassroomButton.setEnabled(false);
        createClassroomButton.setText("正在创建...");

        findTeacherByTeacherObjectId(teacherObjectId, new QueryListener<Teacher>()
        {
            @Override
            public void done(Teacher teacher, BmobException e)
            {
                if (e == null)
                {
                    saveClassroom(description, teacherObjectId, teacher.getNickname(), new SaveClassroomListener()
                    {
                        @Override
                        public void succeed(String classroomObjectId)
                        {
                            toast("班级创建成功");
                            setResult(RESULT_OK);
                            finish();
                        }

                        @Override
                        public void failed(String info)
                        {
                            toast("班级创建失败: " + info);
                            createClassroomButton.setEnabled(true);
                            createClassroomButton.setText("创建班级");
                        }
                    });
                }
                else
                {
                    toast("创建班级失败，请重试");
                }
            }
        });
    }
}
