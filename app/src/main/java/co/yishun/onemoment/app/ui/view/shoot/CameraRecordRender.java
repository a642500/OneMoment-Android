package co.yishun.onemoment.app.ui.view.shoot;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Message;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.ui.view.shoot.gles.FullFrameRect;
import co.yishun.onemoment.app.ui.view.shoot.gles.GlUtil;
import co.yishun.onemoment.app.ui.view.shoot.video.EncoderConfig;
import co.yishun.onemoment.app.ui.view.shoot.video.MediaAudioEncoder;
import co.yishun.onemoment.app.ui.view.shoot.video.MediaMuxerWrapper;
import co.yishun.onemoment.app.ui.view.shoot.video.TextureMovieEncoder;

import static co.yishun.onemoment.app.ui.view.shoot.filter.FilterManager.FilterType;
import static co.yishun.onemoment.app.ui.view.shoot.filter.FilterManager.getCameraFilter;

/**
 * Created by Carlos on 2015/10/13.
 */
@TargetApi(value = 17)
public class CameraRecordRender implements GLSurfaceView.Renderer {
    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;
    private static final String TAG = "CameraRecordRender";
    private static final String DIR_NAME = "AVRecSample";
    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());

    private final Context mApplicationContext;
    private final CameraGLSurfaceView.CameraHandler mCameraHandler;
    private final float[] mSTMatrix = new float[16];
    private int mTextureId = GlUtil.NO_TEXTURE;
    private FullFrameRect mFullScreen;
    private SurfaceTexture mSurfaceTexture;

    private FilterType mCurrentFilterType;
    private FilterType mNewFilterType;
    private int mNewFilterIndex;
    private TextureMovieEncoder mVideoEncoder;

    private boolean mRecordingEnabled;
    private int mRecordingStatus;
    private EncoderConfig mEncoderConfig;

    private float mMvpScaleX = 1f, mMvpScaleY = 1f;
    private int mSurfaceWidth, mSurfaceHeight;
    private int mIncomingWidth, mIncomingHeight;
    private MediaMuxerWrapper mMediaMuxerWrapper;
    private MediaAudioEncoder mAudioEncoder;
    private FilterType[] types = FilterType.values();

    public CameraRecordRender(Context context, CameraGLSurfaceView.CameraHandler cameraHandler, EncoderConfig config) {
        mApplicationContext = context.getApplicationContext();
        mCameraHandler = cameraHandler;
        mCurrentFilterType = mNewFilterType = FilterType.Normal;
        mNewFilterIndex = 0;
//        mVideoEncoder = TextureMovieEncoder.getInstance();
        mEncoderConfig = config;
        try {
            mMediaMuxerWrapper = new MediaMuxerWrapper(config.mOutputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mVideoEncoder = new TextureMovieEncoder(context.getApplicationContext(), mEncoderConfig, mMediaMuxerWrapper);
        mAudioEncoder = new MediaAudioEncoder(mMediaMuxerWrapper);
        mMediaMuxerWrapper.setAudioEncoder(mAudioEncoder);
        mMediaMuxerWrapper.setVideoEncoder(mVideoEncoder);
    }


    public void setRecordingEnabled(boolean recordingEnabled) {
        mRecordingEnabled = recordingEnabled;
    }

    public void nextFilter() {
        mNewFilterIndex = (mNewFilterIndex + 1) % types.length;
    }

    public void preFilter() {
        // java % return remainder, so -1 % 4 = -1, not 3. Here needs modulus: -1 % 4 = 3
        // So get modulus by ((-1 % 4) + 4) % 4
        mNewFilterIndex = ((mNewFilterIndex - 1) % types.length + types.length) % types.length;
    }

    public int getCurrentFilterIndex() {
        return mNewFilterIndex;
    }

    public void setCameraPreviewSize(int width, int height) {
        mIncomingWidth = width;
        mIncomingHeight = height;

        float scaleHeight = mSurfaceHeight / (height * 1f / width * 1f);
        float surfaceHeight = mSurfaceHeight;

        if (mFullScreen != null) {
            mMvpScaleX = 1f;
            mMvpScaleY = scaleHeight / surfaceHeight;
            mFullScreen.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Matrix.setIdentityM(mSTMatrix, 0);
        mRecordingEnabled = mVideoEncoder.isRecording();
        if (mRecordingEnabled) {
            mRecordingStatus = RECORDING_RESUMED;
        } else {
            mRecordingStatus = RECORDING_OFF;
            mVideoEncoder.initFilter(mCurrentFilterType);
        }
        mFullScreen = new FullFrameRect(getCameraFilter(mCurrentFilterType, mApplicationContext));
        mTextureId = mFullScreen.createTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        if (gl != null) {
            gl.glViewport(0, 0, width, height);
        }

        Message message = mCameraHandler.obtainMessage(CameraGLSurfaceView.CameraHandler.START);
        message.obj = mSurfaceTexture;
        mCameraHandler.sendMessage(message);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mSurfaceTexture.updateTexImage();
        if (types[mNewFilterIndex] != mCurrentFilterType) {
            mFullScreen.changeProgram(getCameraFilter(types[mNewFilterIndex], mApplicationContext));
            mCurrentFilterType = types[mNewFilterIndex];
        }
        mFullScreen.getFilter().setTextureSize(mIncomingWidth, mIncomingHeight);
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);

        videoOnDrawFrame(mTextureId, mSTMatrix, mSurfaceTexture.getTimestamp());
    }

    private void videoOnDrawFrame(int textureId, float[] texMatrix, long timestamp) {
        if (mRecordingEnabled) {
            switch (mRecordingStatus) {
                case RECORDING_OFF:
                    LogUtil.i(TAG, "RECORDING_OFF");
                    mEncoderConfig.updateEglContext(EGL14.eglGetCurrentContext());
//                    mVideoEncoder.startRecording();
                    try {
                        mMediaMuxerWrapper.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMediaMuxerWrapper.startRecording();
                    mVideoEncoder.setTextureId(textureId);
                    mVideoEncoder.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
                    mRecordingStatus = RECORDING_ON;

                    break;
                case RECORDING_RESUMED:
                    LogUtil.i(TAG, "RECORDING_RESUME");
                    mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                    mVideoEncoder.setTextureId(textureId);
                    mVideoEncoder.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
                    mRecordingStatus = RECORDING_ON;
                    break;
                case RECORDING_ON:
                    LogUtil.i(TAG, "RECORDING_ON");
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        } else {
            switch (mRecordingStatus) {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                    LogUtil.i(TAG, "else RECORDING_RESUME");
//                    mVideoEncoder.stopRecording(() -> mCameraHandler.sendEmptyMessage(CameraGLSurfaceView.CameraHandler.END));
                    mMediaMuxerWrapper.stopRecording(() -> mCameraHandler.sendEmptyMessage(CameraGLSurfaceView.CameraHandler.END));
                    mRecordingStatus = RECORDING_OFF;
                    break;
                case RECORDING_OFF:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        }

        mVideoEncoder.updateFilter(mCurrentFilterType);
        mVideoEncoder.frameAvailable(texMatrix, timestamp);
    }

    public void notifyPausing() {

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        if (mFullScreen != null) {
            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
            mFullScreen = null;             // to be destroyed
        }
    }

    public void changeFilter(FilterType filterType) {
        mNewFilterType = filterType;
    }

}
