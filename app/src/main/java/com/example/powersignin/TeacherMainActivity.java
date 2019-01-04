package com.example.powersignin;

import android.annotation.SuppressLint;
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

import java.util.List;

public class TeacherMainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String EXTRA_TEACHER_USERNAME = "teacher_username";
    private static final String EXTRA_TEACHER_OBJECTID = "teacher_objectid";
    private static final String EXTRA_TEACHER_NICKNAME = "teacher_nickname";

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private TextView mEmptyText;
    private SwipeRefreshLayout mRefresh;

    private String mTeacherObjectId;
    private String mTeacherUsername;
    private String mTeacherNickname;
    private List<Classroom> classrooms;
    private Adapter mAdapter;

    public static Intent newIntent(Context context, String teacherUsername, String teacherObjectId, String teacherNickname)
    {
        Intent intent = new Intent(context, TeacherMainActivity.class);
        intent.putExtra(EXTRA_TEACHER_USERNAME, teacherUsername);
        intent.putExtra(EXTRA_TEACHER_OBJECTID, teacherObjectId);
        intent.putExtra(EXTRA_TEACHER_NICKNAME, teacherNickname);
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
        setContentView(R.layout.activity_teacher_main);
    }

    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar_teacher_main_activity);
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view_teacher_class);
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
        //获取教师用户名
        mTeacherUsername = getIntent().getStringExtra(EXTRA_TEACHER_USERNAME);

        //获取教师objectId
        mTeacherObjectId = getIntent().getStringExtra(EXTRA_TEACHER_OBJECTID);

        //获取教师nickname
        mTeacherNickname = getIntent().getStringExtra(EXTRA_TEACHER_NICKNAME);

        //设置Toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView textView = (TextView)findViewById(R.id.text_title);
        textView.setText(mTeacherNickname + "管理的班级");

        updateClassroomsList();
    }

    //设置Toolbar菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_teacher_main_activity_toolbar, menu);
        return true;
    }

    //响应Toolbar菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.new_class:
                startNewClassActivity(mTeacherObjectId);
                break;
            case R.id.my_info:
                if (!new WifiUtil(this).isNetworkConnected())
                {
                    toast("请检查网络连接!");
                    return true;
                }
                startUserInfoActivity(mTeacherUsername, mTeacherObjectId, mTeacherNickname, "teacher");
                break;
        }

        return true;
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
        public TextView classCodeTextView;
        public TextView isSignin;
        public int position;

        public ViewHolder(View itemView)
        {
            super(itemView);
            classNameTextView = (TextView)itemView.findViewById(R.id.text_teacher_class_name);
            classCodeTextView = (TextView)itemView.findViewById(R.id.text_teacher_class_code);
            isSignin = (TextView)itemView.findViewById(R.id.text_issignin);

            CardView cardView = (CardView)itemView.findViewById(R.id.card_view);
            cardView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (!new WifiUtil(TeacherMainActivity.this).isNetworkConnected())
                    {
                        toast("请检查网络连接!");
                        return;
                    }

                    startTeacherClassInfoActivity(classNameTextView.getText().toString(), classCodeTextView.getText().toString(), position);
                }
            });
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder>
    {
        private List<Classroom> classroom;

        public Adapter(List<Classroom> classroom)
        {
            this.classroom = classroom;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(TeacherMainActivity.this);
            View view = layoutInflater.inflate(R.layout.item_teacher_class, parent, false);
            return new ViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {
            Classroom classroom = classrooms.get(position);
            holder.position = position;
            holder.classNameTextView.setText(classroom.getDescription());
            holder.classCodeTextView.setText(classroom.getObjectId());
            if (classroom.isSignin())
            {
                holder.isSignin.setText("正在签到");
                holder.isSignin.setTextColor(getResources().getColor(R.color.colorEmphasis));
            }
            else
            {
                holder.isSignin.setText("未在签到");
                holder.isSignin.setTextColor(getResources().getColor(R.color.colorIgnore));
            }
        }

        @Override
        public int getItemCount()
        {
            return classrooms.size();
        }
    }

    private void updateClassroomsList()
    {
        findTeacherClassrooms(mTeacherObjectId, new FindListener<Classroom>()
        {
            @Override
            public void done(List<Classroom> list, BmobException e)
            {
                if (e == null)
                {
                    //班级列表为空
                    if (list.size() == 0)
                    {
                        mRecyclerView.setVisibility(View.GONE);
                        mEmptyText.setVisibility(View.VISIBLE);
                    }
                    //班级列表不为空
                    else
                    {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mEmptyText.setVisibility(View.GONE);
                        classrooms = list;
                        //设置RecyclerView
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(TeacherMainActivity.this));
                        mAdapter = new Adapter(classrooms);
                        mRecyclerView.setAdapter(mAdapter);
                    }


                }
                else
                {
                    toast("获取班级数据失败: " + e.getMessage());
                }

                //关闭刷新状态
                mRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_NEW_CLASS)
        {
            if (resultCode == RESULT_OK)
            {
                //更新教师班级列表
                updateClassroomsList();
            }
        }
        else if (requestCode == REQUEST_TEACHER_CLASS_INFO)
        {
            if (resultCode == RESULT_OK)
            {
                //更新教师班级列表
                //updateClassroomsList();

                final int position = data.getIntExtra(TeacherClassInfoActivity.EXTRA_POSITION, -1);
                //toast(Integer.toString(position));

                if (position != -1)
                {
                    //更新第position个数据
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
                                toast("数据更新失败");
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
                //更新教师班级列表
                updateClassroomsList();
            }
        }
    }
}
