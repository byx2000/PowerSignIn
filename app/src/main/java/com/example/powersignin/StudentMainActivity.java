package com.example.powersignin;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.TextView;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import com.example.powersignin.Util.WifiUtil;
import com.example.powersignin.bean.Classroom;
import com.example.powersignin.bean.Student;

import java.util.List;

public class StudentMainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String EXTRA_STUDENT_USERNAME = "student_username";
    private static final String EXTRA_STUDENT_OBJECTID = "student_objectid";
    private static final String EXTRA_STUDENT_NICKNAME = "student_nickname";

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private TextView mEmptyText;
    private SwipeRefreshLayout mRefresh;
    private Adapter mAdapter;

    private String mStudentObjectId;
    private String mStudentUsername;
    private String mStudentNickname;
    private List<Classroom> classrooms;

    public static Intent newIntent(Context context, String studentUsername, String studentObjectId, String studentNickname)
    {
        Intent intent = new Intent(context, StudentMainActivity.class);
        intent.putExtra(EXTRA_STUDENT_USERNAME, studentUsername);
        intent.putExtra(EXTRA_STUDENT_OBJECTID, studentObjectId);
        intent.putExtra(EXTRA_STUDENT_NICKNAME, studentNickname);
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
        setContentView(R.layout.activity_student_main);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler);
        mEmptyText = (TextView)findViewById(R.id.text_empty_text);
        mRefresh = (SwipeRefreshLayout)findViewById(R.id.refresh);
        mRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    protected void initListeners()
    {
        mRefresh.setOnRefreshListener(this);
    }

    @Override
    protected void initData()
    {
        mStudentUsername = getIntent().getStringExtra(EXTRA_STUDENT_USERNAME);
        mStudentObjectId = getIntent().getStringExtra(EXTRA_STUDENT_OBJECTID);
        mStudentNickname = getIntent().getStringExtra(EXTRA_STUDENT_NICKNAME);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView textView = (TextView)findViewById(R.id.text_title);
        textView.setText(mStudentNickname + "加入的班级");

        updateClassroomsList();
    }

    //设置Toolbar菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_student_main_activity_toolbar, menu);
        return true;
    }

    //响应Toolbar菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.join_class:
                startJoinClassActivity(mStudentObjectId);
                break;
            case R.id.my_info:
                if (!new WifiUtil(this).isNetworkConnected())
                {
                    toast("请检查网络连接!");
                    return true;
                }

                startUserInfoActivity(mStudentUsername, mStudentObjectId, mStudentNickname, "student");
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_JOIN_CLASS)
        {
            if (resultCode == RESULT_OK)
            {
                updateClassroomsList();
            }
            /*else
            {
                updateClassroomsList();
            }*/
        }
        else if (requestCode == REQUEST_STUDENT_CLASS_INFO)
        {
            if (resultCode == RESULT_OK)
            {
                final int position = data.getIntExtra(StudentClassInfoActivity.EXTRA_POSITION, -1);
                //toast(Integer.toString(position));
                if (position != -1)
                {
                    //更新一条数据
                    findClassroom(classrooms.get(position).getObjectId(), new QueryListener<Classroom>()
                    {
                        @Override
                        public void done(Classroom classroom, BmobException e)
                        {
                            if (e == null)
                            {
                                classrooms.remove(position);
                                classrooms.add(position, classroom);
                                mAdapter.notifyItemChanged(position);
                            }
                            else
                            {
                                toast("班级信息更新失败");
                            }
                        }
                    });
                }
                else
                {
                    updateClassroomsList();
                }
            }
            else
            {
                updateClassroomsList();
            }
        }
    }

    //下拉刷新时执行
    @Override
    public void onRefresh()
    {
        if (!new WifiUtil(this).isNetworkConnected())
        {
            toast("请检查网络连接!");
            mRefresh.setRefreshing(false);
            return;
        }

        //更新班级列表
        updateClassroomsList();
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView classNameTextView;
        public TextView classTeacher;
        public TextView signinStateTextView;
        public String classroomObjectId;
        public int position;

        public ViewHolder(View itemView)
        {
            super(itemView);
            classNameTextView = (TextView)itemView.findViewById(R.id.text_class_name);
            classTeacher = (TextView)itemView.findViewById(R.id.text_teacher);
            signinStateTextView = (TextView)itemView.findViewById(R.id.text_signin_state);

            CardView cardView = (CardView)itemView.findViewById(R.id.card_view);
            cardView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (!new WifiUtil(StudentMainActivity.this).isNetworkConnected())
                    {
                        toast("请检查网络连接!");
                        return;
                    }

                    startStudentClassInfoActivity(classroomObjectId, mStudentObjectId, position);
                }
            });
        }
    }

    private class Adapter extends RecyclerView.Adapter<StudentMainActivity.ViewHolder>
    {
        private List<Classroom> classroom;

        public Adapter(List<Classroom> classroom)
        {
            this.classroom = classroom;
        }

        @Override
        public StudentMainActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(StudentMainActivity.this);
            View view = layoutInflater.inflate(R.layout.item_student_class, parent, false);
            return new StudentMainActivity.ViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(final StudentMainActivity.ViewHolder holder, int position)
        {
            final Classroom classroom = classrooms.get(position);
            holder.classNameTextView.setText(classroom.getDescription());
            holder.classTeacher.setText(classroom.getTeacherNickname());
            holder.position = position;

            if (classroom.isSignin())
            {
                //判断是否签到成功
                findSigninedStudents(classroom.getCurrentSigninEvent(), new FindListener<Student>()
                {
                    @Override
                    public void done(List<Student> list, BmobException e)
                    {
                        if (e == null)
                        {
                            boolean flag = true;
                            for (Student student : list)
                            {
                                if (student.getObjectId().equals(mStudentObjectId))
                                {
                                    holder.signinStateTextView.setText("签到成功");
                                    holder.signinStateTextView.setTextColor(getResources().getColor(R.color.green));
                                    flag = false;
                                    break;
                                }
                            }

                            if (flag)
                            {
                                holder.signinStateTextView.setText("正在签到");
                                holder.signinStateTextView.setTextColor(getResources().getColor(R.color.colorEmphasis));
                            }
                        }
                    }
                });
            }
            else
            {
                holder.signinStateTextView.setText("未在签到");
                holder.signinStateTextView.setTextColor(getResources().getColor(R.color.colorIgnore));
            }

            holder.classroomObjectId = classroom.getObjectId();
        }

        @Override
        public int getItemCount()
        {
            return classrooms.size();
        }
    }

    private void updateClassroomsList()
    {
        findStudentClassrooms(mStudentObjectId, new FindListener<Classroom>()
        {
            @Override
            public void done(List<Classroom> list, BmobException e)
            {
                //查找成功
                if (e == null)
                {
                    if (list.size() == 0)
                    {
                        mEmptyText.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    }
                    else
                    {
                        mEmptyText.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);

                        classrooms = list;
                        //设置RecyclerView
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(StudentMainActivity.this));
                        mAdapter = new Adapter(classrooms);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }
                //查找失败
                else
                {
                    toast("获取班级数据失败: " + e.getMessage());
                }

                //关闭刷新状态
                mRefresh.setRefreshing(false);
            }
        });
    }
}
