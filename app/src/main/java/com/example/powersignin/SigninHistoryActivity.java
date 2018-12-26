package com.example.powersignin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import com.example.powersignin.bean.Classroom;
import com.example.powersignin.bean.SigninEvent;

import java.util.List;

public class SigninHistoryActivity extends BaseActivity
{
    private static final String EXTRA_CLASSROOM_OBJECTID = "classroom_objectid";

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private TextView mEmptyText;

    private String mClassroomObjectId;

    private List<SigninEvent> mEvents;

    public static Intent newIntent(Context context, String classroomObjectId)
    {
        Intent intent = new Intent(context, SigninHistoryActivity.class);
        intent.putExtra(EXTRA_CLASSROOM_OBJECTID, classroomObjectId);
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
        setContentView(R.layout.activity_signin_history);
    }

    @Override
    protected void initViews()
    {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setToolbarTitle("签到历史");

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mEmptyText = (TextView)findViewById(R.id.text_empty_text);
    }

    @Override
    protected void initListeners()
    {

    }

    @Override
    protected void initData()
    {
        mClassroomObjectId = getIntent().getStringExtra(EXTRA_CLASSROOM_OBJECTID);

        //toast(mClassroomObjectId);

        //获取班级签到历史
        findClassroomSigninEvents(mClassroomObjectId, new FindListener<SigninEvent>()
        {
            @Override
            public void done(List<SigninEvent> list, BmobException e)
            {
                if (e == null)
                {
                    //签到历史为空
                    if (list.size() == 0)
                    {
                        mEmptyText.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    }
                    else
                    {
                        mEmptyText.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);

                        mEvents = list;
                        //初始化RecyclerView
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(SigninHistoryActivity.this));
                        mRecyclerView.setAdapter(new SigninHistoryActivity.Adapter(mEvents));
                    }
                }
                else
                {
                    toast("获取签到历史失败: " + e.getMessage());
                }
            }
        });
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView mBeginTime;
        public TextView mEndTime;
        public int index;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mBeginTime = (TextView)itemView.findViewById(R.id.text_begin_time);
            mEndTime = (TextView)itemView.findViewById(R.id.text_end_time);

            CardView cardView = (CardView)itemView.findViewById(R.id.card_view);
            cardView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //启动签到详情Activity
                    startSigninEventDetailActivity(mEvents.get(index).getObjectId());
                }
            });
        }
    }

    private class Adapter extends RecyclerView.Adapter<SigninHistoryActivity.ViewHolder>
    {
        private List<SigninEvent> events;

        public Adapter(List<SigninEvent> events)
        {
            this.events = events;
        }

        @Override
        public SigninHistoryActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(SigninHistoryActivity.this);
            View view = layoutInflater.inflate(R.layout.item_signin, parent, false);
            return new SigninHistoryActivity.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SigninHistoryActivity.ViewHolder holder, int position)
        {
            SigninEvent event = events.get(position);
            holder.mBeginTime.setText(event.getCreatedAt());
            holder.mEndTime.setText(event.getUpdatedAt());
            holder.index = position;
        }

        @Override
        public int getItemCount()
        {
            return events.size();
        }
    }
}
