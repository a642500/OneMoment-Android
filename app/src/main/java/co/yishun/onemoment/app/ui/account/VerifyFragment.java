package co.yishun.onemoment.app.ui.account;

import android.app.Activity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.view.CountDownResentView;

/**
 * Created by Carlos on 2015/8/11.
 */
@EFragment(R.layout.fragment_phone_verify)
public class VerifyFragment extends AccountFragment {
    public static final String EXTRA_TYPE_SIGN_UP = "signup";
    public static final String EXTRA_TYPE_FIND_PASSWORD = "reset_pw";
    private static final String TAG = "VerifyFragment";
    @ViewById
    CountDownResentView countDownResentView;
    @FragmentArg
    String type = EXTRA_TYPE_SIGN_UP;
    @FragmentArg
    String phoneNum;
    @FragmentArg
    String password;
    boolean isSending = false;
    private String mVerificationCode;

    @Override
    public void onFABClick(View view) {
        if (checkVerifyCode())
            verify();
    }

    @EditorAction(R.id.verificationCodeEditText)
    void onSubmit(TextView view, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            onFABClick(view);
    }

    @AfterTextChange(R.id.verificationCodeEditText)
    void onPhoneChange(Editable text, TextView textView) {
        mVerificationCode = text.toString();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @AfterViews
    void setViews() {
        sendSms();
        countDownResentView.setOnClickListenerWhenEnd(view -> {
            LogUtil.d(TAG, "" + isSending);
            if (!isSending)
                sendSms();
            mActivity.showSnackMsg(R.string.fragment_phone_verify_sms_sending);//TODO change to sticky
        });
    }

    @Background
    void sendSms() {
        isSending = true;
        LogUtil.d(TAG, phoneNum + " " + type);
        ApiModel model = mActivity.getAccountService().sendVerifySms(phoneNum, type);
        if (model.code > 0) {
            mActivity.runOnUiThread(countDownResentView::countDown);
            mActivity.showSnackMsg(R.string.fragment_phone_verify_sms_success);
        } else
            switch (model.errorCode) {
                case Constants.ErrorCode.ACCOUNT_EXISTS:
                    mActivity.showSnackMsg(R.string.fragment_phone_verify_sms_fail_account_exist);
                    break;
                case Constants.ErrorCode.ACCOUNT_DOESNT_EXIST:
                    mActivity.showSnackMsg(R.string.fragment_phone_verify_sign_up_error_account_not_exist);
                    break;
                default:
                    mActivity.showSnackMsg(R.string.fragment_phone_verify_sms_fail);
                    break;
            }
        isSending = false;
    }

    @Override
    int getFABBackgroundColorRes() {
        return type.equals(EXTRA_TYPE_SIGN_UP) ? R.color.colorAccent : R.color.colorSecondary;
    }

    @Override
    int getFABImageResource() {
        return type.equals(EXTRA_TYPE_SIGN_UP) ? R.drawable.ic_login_next : R.drawable.ic_login_done;
    }

    private boolean checkVerifyCode() {
        return !TextUtils.isEmpty(mVerificationCode) && mVerificationCode.length() < 8 && mVerificationCode.length() > 2;
    }

    @Background
    void verify() {
        mActivity.showProgress(R.string.fragment_phone_verify_verify_progress);
        ApiModel result = mActivity.getAccountService().verifyPhone(phoneNum, mVerificationCode);

        if (result.code > 0) {
            if (TextUtils.equals(type, EXTRA_TYPE_FIND_PASSWORD)) {
                ApiModel resetResult = mActivity.getAccountService().resetPassword(phoneNum, password);
                if (resetResult.code > 0) {
                    mActivity.showSnackMsg(R.string.fragment_phone_verify_reset_password_success);
                    next();
                } else
                    mActivity.showSnackMsg(R.string.fragment_phone_verify_verify_error_network);
            } else {
                mActivity.showSnackMsg(R.string.fragment_phone_verify_verify_success);
                next();
            }
        } else
            switch (result.errorCode) {
                case Constants.ErrorCode.PHONE_VERIFY_CODE_WRONG:
                    mActivity.showSnackMsg(R.string.fragment_phone_verify_verify_error_verify_fail);
                    break;
                case Constants.ErrorCode.ACCOUNT_EXISTS:
                    mActivity.showSnackMsg(R.string.fragment_phone_verify_sign_up_error_account_exist);
                    break;
                case Constants.ErrorCode.PHONE_VERIFY_CODE_EXPIRES:
                    mActivity.showSnackMsg(R.string.fragment_phone_verify_verify_error_expire);
                    break;
                case Constants.ErrorCode.ACCOUNT_DOESNT_EXIST:
                    mActivity.showSnackMsg(R.string.fragment_phone_verify_sign_up_error_account_not_exist);
                    break;
                default:
                    mActivity.showSnackMsg(R.string.fragment_phone_verify_verify_error_network);
                    break;
            }
        mActivity.hideProgress();
    }

    @UiThread(delay = 300)
    void next() {
        if (TextUtils.equals(type, EXTRA_TYPE_FIND_PASSWORD)) {
            mActivity.openFragment(PhoneLoginFragment_.builder().build());
        } else {
            mActivity.openFragment(IntegrateInfoFragment_.builder().phoneNum(phoneNum).password(password).build());
        }
    }

    @Override
    public void setPageInfo() {
        mPageName = "VerifyFragment";
    }
}
