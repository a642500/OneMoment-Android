package co.yishun.onemoment.app.ui.controller;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.WorldAPI;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.BannerHeaderProvider;
import co.yishun.onemoment.app.ui.adapter.HeaderRecyclerAdapter;
import co.yishun.onemoment.app.ui.adapter.WorldAdapter;

/**
 * Not extend {@link RecyclerController} because this need to load banner as well as World Tag list.
 * <p> Created by Carlos on 2015/8/16.
 */
@EBean
@Deprecated
// not used any more, WorldFragment use hybrd instead. So static stopping BannerHeaderProvider
// auto cycle in DiscoveryFragment is OK. By Carlos.
public class WorldPagerController implements SwipeRefreshLayout.OnRefreshListener, OnMoreListener {
    private static final String TAG = "WorldPagerController";
    private WorldAdapter mAdapter;
    private HeaderCompatibleSuperRecyclerView mRecyclerView;
    private boolean isRecommend;
    private WorldAPI mWorldAPI;
    private BannerHeaderProvider mBannerHeaderProvider;
    private String ranking = "";

    public WorldPagerController() {
    }

    public void setUp(Context context, HeaderCompatibleSuperRecyclerView mRecyclerView, boolean recommend, WorldAPI worldAPI, WorldAdapter.OnItemClickListener<WorldTag> listener) {
        this.mRecyclerView = mRecyclerView;
        isRecommend = recommend;
        mWorldAPI = worldAPI;
        this.mAdapter = new WorldAdapter(context, listener);
        RecyclerView.Adapter trueAdapter = mAdapter;


        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setRefreshListener(this);
        mRecyclerView.setOnMoreListener(this);

        if (recommend) {
            mBannerHeaderProvider = new BannerHeaderProvider(context);
            trueAdapter = new HeaderRecyclerAdapter(trueAdapter, mBannerHeaderProvider);
            loadBanners();
        }
        mRecyclerView.setAdapter(trueAdapter);

        loadTags();
    }

    public WorldAdapter getAdapter() {
        return mAdapter;
    }

    public SuperRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Background
    void loadBanners() {
        ListWithError<Banner> banners = mWorldAPI.getBanners(null);
        if (banners.isSuccess()) {
            onLoadBanners(banners);
        } else {
            LogUtil.i(TAG, "load banner error");
        }
    }

    @UiThread
    void onLoadBanners(List<Banner> banners) {
        mBannerHeaderProvider.setupBanners(banners);
    }

    @Background
    void loadTags() {
        synchronizedLoadTags();
    }

    synchronized void synchronizedLoadTags() {
        ListWithError<WorldTag> list = mWorldAPI.getWorldTagList(5, ranking, isRecommend ? "recommend" : "time");
        if (list.isSuccess()) {
            ranking = list.get(list.size() - 1).ranking;
            onLoadTags(list);
        } else {
            onLoadError();
        }
    }

    @UiThread
    void onLoadError() {
        Snackbar.make(mRecyclerView, R.string.text_load_error, Snackbar.LENGTH_LONG).show();
        mRecyclerView.loadEnd();
        mRecyclerView.getSwipeToRefresh().setRefreshing(false);
    }


    @UiThread
    void onLoadTags(List<WorldTag> list) {
        mAdapter.addAll(list);
        mRecyclerView.loadEnd();
        mRecyclerView.getSwipeToRefresh().setRefreshing(false);
        if (isRecommend)
            mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        mAdapter.clear();
        ranking = "";
        loadTags();
    }

    @Override
    public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
        LogUtil.i(TAG, "start load more, int numberOfItems, int numberBeforeMore, int currentItemPos: " + numberOfItems + ", " + numberBeforeMore + ", " + currentItemPos);
        loadTags();
    }
}
