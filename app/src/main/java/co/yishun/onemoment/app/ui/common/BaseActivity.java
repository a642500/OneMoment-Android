package co.yishun.onemoment.app.ui.common;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;

/**
 * Created by Carlos on 2015/8/12.
 */
@EActivity
public abstract class BaseActivity extends AppCompatActivity {
    //set it false, if we only take this activity's fragments into count. else set it true, and give a page name.
    protected boolean mIsPage = true;
    protected String mPageName = "BaseActivity";
    private MaterialDialog mProgressDialog;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        PushAgent.getInstance(this).onAppStart();
    }

    @CallSuper @NonNull public View getSnackbarAnchorWithView(@Nullable View view) {
        return view != null ? view : findViewById(android.R.id.content);
    }

    public abstract void setPageInfo();

    @UiThread
    public void showSnackMsg(String msg) {
        Snackbar.make(getSnackbarAnchorWithView(null), msg, Snackbar.LENGTH_SHORT).show();
    }

    public void showSnackMsg(@StringRes int msgRes) {
        showSnackMsg(getString(msgRes));
    }

    public void showProgress() {
        showProgress(R.string.progress_loading_msg);
    }

    @UiThread
    public void showProgress(String msg) {
        if (mProgressDialog == null)
            mProgressDialog = new MaterialDialog.Builder(this).theme(Theme.LIGHT).cancelable(false).content(msg).progress(true, 0).build();
        mProgressDialog.setContent(msg);
        mProgressDialog.show();
    }

    public void showProgress(@StringRes int msgRes) {
        showProgress(getString(msgRes));
    }

    @UiThread
    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPageInfo();
        Log.d(mPageName, String.valueOf(mIsPage));
        if (mIsPage) {
            MobclickAgent.onPageStart(mPageName);
        }
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsPage) {
            MobclickAgent.onPageEnd(mPageName);
        }
        MobclickAgent.onPause(this);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @UiThread(delay = Constants.INT_EXIT_DELAY_MILLIS)
    public void exit() {
        finish();
    }

}
