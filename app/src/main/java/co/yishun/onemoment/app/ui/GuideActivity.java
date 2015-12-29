package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.ui.view.PageIndicatorDot;
import co.yishun.onemoment.app.wxapi.EntryActivity;
import co.yishun.onemoment.app.wxapi.EntryActivity_;

@EActivity(R.layout.activity_guide)
public class GuideActivity extends AppCompatActivity {
    private final int pagePicRes[] = new int[]{
            R.drawable.pic_guide_001,
            R.drawable.pic_guide_002,
            R.drawable.pic_guide_003,
            R.drawable.pic_guide_004};
    private final int pageTextRes[] = new int[]{
            R.drawable.pic_guide_001txt,
            R.drawable.pic_guide_002txt,
            R.drawable.pic_guide_003txt,
            R.drawable.pic_guide_004txt};

    @Extra boolean isFirstLuanch;

    @ViewById ViewPager viewPager;
    @ViewById PageIndicatorDot pageIndicator;

    private int pageNum = 5;

    @AfterViews void setUpViews() {
        viewPager.setAdapter(getViewPager(LayoutInflater.from(this)));
        viewPager.setOffscreenPageLimit(2);
        pageIndicator.setViewPager(viewPager);
        pageIndicator.setNum(pageNum);
    }

    protected View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position) {
        View rootView;
        if (position != 4) {
            rootView = inflater.inflate(R.layout.page_guide, container, false);
            ((ImageView) rootView.findViewById(R.id.picImage)).setImageResource(pagePicRes[position]);
            ((ImageView) rootView.findViewById(R.id.textImage)).setImageResource(pageTextRes[position]);
        } else {
            rootView = inflater.inflate(R.layout.page_guide_final, container, false);
            rootView.findViewById(R.id.enterText).setOnClickListener(this::enterClick);
        }
        return rootView;
    }

    void enterClick(View view) {
        if (isFirstLuanch) {
            if (AccountManager.isLogin(this))
                MainActivity_.intent(this).start();
            else
                EntryActivity_.intent(this).start();
        }
        finish();
    }

    private PagerAdapter getViewPager(LayoutInflater inflater) {
        return new PagerAdapter() {
            @Override
            public int getCount() {
                return pageNum;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View rootView = onCreatePagerView(inflater, container, position);
                container.addView(rootView);
                return rootView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(((View) object));
            }
        };
    }

}