package co.yishun.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by jay on 10/4/15.
 */
public class PlayTagContainer extends FrameLayout {
    public final static String VIDEO_TAG_VIEW_TAG = "video_tag";
    private int mSize;
    private List<VideoTag> videoTags;
    private List<View> tagViews;
    private boolean mShowTags = true;

    public PlayTagContainer(Context context) {
        super(context);
        init();
    }

    public PlayTagContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayTagContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        tagViews = new ArrayList<View>();
    }

    public void showTags() {
        clearTags();

        if (!mShowTags) return;
        if (videoTags == null) return;

        for (int i = 0; i < videoTags.size(); i++) {
            VideoTag videoTag = videoTags.get(i);

            TextView textView;
            if (i < tagViews.size()) {
                textView = (TextView) tagViews.get(i);
            } else {
                textView = new TextView(getContext());
                textView.setTag(VIDEO_TAG_VIEW_TAG);
                tagViews.add(textView);
            }
            textView.setText(videoTag.getText());

            int left = (int) (videoTag.getX() * getSize());
            int top = (int) (videoTag.getY() * getSize());

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(left, top, 0, 0);
            textView.setLayoutParams(params);
            addView(textView);
        }
    }

    private void clearTags() {
        for (int i = 0; i < getChildCount(); ) {
            View v = getChildAt(i);
            Object tag = v.getTag();
            if (tag != null && tag.equals(VIDEO_TAG_VIEW_TAG)) {
                removeViewAt(i);
            } else {
                i++;
            }
        }
    }

    public int getSize() {
        return mSize;
    }

    public List<VideoTag> getVideoTags() {
        return videoTags;
    }

    public void setVideoTags(List<VideoTag> videoTags) {
        this.videoTags = videoTags;
        showTags();
    }

    public boolean isShowTags() {
        return mShowTags;
    }

    public void setShowTags(boolean mShowTags) {
        this.mShowTags = mShowTags;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int size = 0;
        if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            size = widthSize;
        }
        mSize = size;
        if (mShowTags) {
            showTags();
        }
        Log.i("[VTC]", "tag container size " + mSize);
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}