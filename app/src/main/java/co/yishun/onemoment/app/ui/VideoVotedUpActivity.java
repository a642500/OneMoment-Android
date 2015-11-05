package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.VideoLikeAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.VotedUpController_;

/**
 * Created by yyz on 7/20/15.
 */
//TODO handle not extend BaseActivity
@EActivity(R.layout.activity_video_voted_up)
public class VideoVotedUpActivity extends BaseActivity implements AbstractRecyclerViewAdapter.OnItemClickListener<Video> {
    @ViewById Toolbar toolbar;
    @ViewById SuperRecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void setupToolbar() {
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.video_vote_up_title);
        Log.i("setupToolbar", "set home as up true");
    }

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @AfterViews
    void setView() {

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        AbstractRecyclerViewAdapter<Video, VideoLikeAdapter.SimpleViewHolder> adapter = new VideoLikeAdapter(this, this, recyclerView);
        recyclerView.setAdapter(adapter);
        VotedUpController_.getInstance_(this).setUp(adapter, recyclerView);
    }
    //TODO not test

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // android:launchMode="singleTop" solve navigationUp to a new Activity issue http://stackoverflow.com/questions/13293772/how-to-navigate-up-to-the-same-parent-state
            // but there is no dragger animation, so we should close manually
            // but draggerView.closeActivity(); has bug: https://github.com/ppamorim/Dragger/issues/27
            onBackPressed();//TODO handle back stack
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view, Video item) {

    }


}

