package com.jazperfox.sweetalert;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

public class SweetAlertDialog extends Dialog implements View.OnClickListener {
    private View mDialogView;
    private AnimationSet mModalInAnim;
    private AnimationSet mModalOutAnim;
    private Animation mOverlayOutAnim;
    private Animation mErrorInAnim;
    private AnimationSet mErrorXInAnim;
    private AnimationSet mSuccessLayoutAnimSet;
    private Animation mSuccessBowAnim;
    private TextView mTitleTextView;
    private TextView mContentTextView;
    private FrameLayout mCustomViewContainer;
    private View mCustomView;
    private String mTitleText;
    private String mContentText;
    private boolean mShowCancel;
    private boolean mShowContent;
    private String mCancelText;
    private String mConfirmText;
    private String mNeutralText;
    private int mAlertType;
    private FrameLayout mErrorFrame;
    private FrameLayout mSuccessFrame;
    private FrameLayout mProgressFrame;
    // --- Campo Nuevo ---
    private FrameLayout mInfoFrame;
    // -------------------
    private SuccessTickView mSuccessTick;
    private ImageView mErrorX;
    private View mSuccessLeftMask;
    private View mSuccessRightMask;
    private Drawable mCustomImgDrawable;
    private ImageView mCustomImage;
    private LinearLayout mButtonsContainer;
    private Button mConfirmButton;
    private boolean mHideConfirmButton = false;
    private Button mCancelButton;
    private Button mNeutralButton;
    private Integer mConfirmButtonBackgroundColor;
    private Integer mConfirmButtonTextColor;
    private Integer mNeutralButtonBackgroundColor;
    private Integer mNeutralButtonTextColor;
    private Integer mCancelButtonBackgroundColor;
    private Integer mCancelButtonTextColor;
    private ProgressHelper mProgressHelper;
    private FrameLayout mWarningFrame;
    private OnSweetClickListener mCancelClickListener;
    private OnSweetClickListener mConfirmClickListener;
    private OnSweetClickListener mNeutralClickListener;
    private boolean mCloseFromCancel;
    private boolean mHideKeyBoardOnDismiss = true;
    private int contentTextSize = 0;
    private int titleTextSize = 0;

    private Typeface mTypeface;

    public static final int NORMAL_TYPE = 0;
    public static final int ERROR_TYPE = 1;
    public static final int SUCCESS_TYPE = 2;
    public static final int WARNING_TYPE = 3;
    public static final int CUSTOM_IMAGE_TYPE = 4;
    public static final int PROGRESS_TYPE = 5;
    // --- Nuevo Tipo ---
    public static final int INFO_TYPE = 6;
    // ------------------

    public static boolean DARK_STYLE = false;

    public final static int BUTTON_CONFIRM = DialogInterface.BUTTON_POSITIVE;
    public final static int BUTTON_CANCEL = DialogInterface.BUTTON_NEGATIVE;
    public final static int BUTTON_NEUTRAL = DialogInterface.BUTTON_NEUTRAL;

    private final float defStrokeWidth;
    private float strokeWidth = 0;

    public SweetAlertDialog hideConfirmButton() {
        this.mHideConfirmButton = true;
        return this;
    }

    public interface OnSweetClickListener {
        void onClick(SweetAlertDialog sweetAlertDialog);
    }

    public SweetAlertDialog(Context context) {
        this(context, NORMAL_TYPE);
    }

    public SweetAlertDialog(Context context, int alertType) {
        super(context, DARK_STYLE ? R.style.alert_dialog_dark : R.style.alert_dialog_light);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        defStrokeWidth = getContext().getResources().getDimension(R.dimen.buttons_stroke_width);
        strokeWidth = defStrokeWidth;

        mProgressHelper = new ProgressHelper(context);
        mAlertType = alertType;

        try {
            mErrorInAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.error_frame_in);
            mErrorXInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.error_x_in);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                List<Animation> childAnims = mErrorXInAnim.getAnimations();
                int idx = 0;
                for (; idx < childAnims.size(); idx++) {
                    if (childAnims.get(idx) instanceof AlphaAnimation) {
                        break;
                    }
                }
                if (idx < childAnims.size()) {
                    childAnims.remove(idx);
                }
            }
            mSuccessBowAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.success_bow_roate);
            mSuccessLayoutAnimSet = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.success_mask_layout);
            mModalInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_in);
            mModalOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_out);
        } catch (Exception e) {
            Log.e("SweetAlertDialog", "Error loading animations", e);
        }

        if (mModalOutAnim != null) {
            mModalOutAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mDialogView != null) mDialogView.setVisibility(View.GONE);
                    if (mHideKeyBoardOnDismiss) {
                        hideSoftKeyboard();
                    }
                    if (mDialogView != null) {
                        mDialogView.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (mCloseFromCancel) {
                                        SweetAlertDialog.super.cancel();
                                    } else {
                                        SweetAlertDialog.super.dismiss();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }

        mOverlayOutAnim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (getWindow() != null) {
                    WindowManager.LayoutParams wlp = getWindow().getAttributes();
                    wlp.alpha = 1 - interpolatedTime;
                    getWindow().setAttributes(wlp);
                }
            }
        };
        mOverlayOutAnim.setDuration(120);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog);

        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);
        mTitleTextView = findViewById(R.id.title_text);
        mContentTextView = findViewById(R.id.content_text);
        mCustomViewContainer = findViewById(R.id.custom_view_container);
        mErrorFrame = findViewById(R.id.error_frame);
        mErrorX = mErrorFrame.findViewById(R.id.error_x);
        mSuccessFrame = findViewById(R.id.success_frame);
        mProgressFrame = findViewById(R.id.progress_dialog);
        mSuccessTick = mSuccessFrame.findViewById(R.id.success_tick);
        mSuccessLeftMask = mSuccessFrame.findViewById(R.id.mask_left);
        mSuccessRightMask = mSuccessFrame.findViewById(R.id.mask_right);
        mCustomImage = findViewById(R.id.custom_image);
        mWarningFrame = findViewById(R.id.warning_frame);
        // --- Binding Nuevo Frame ---
        mInfoFrame = findViewById(R.id.info_frame);
        // ---------------------------
        mButtonsContainer = findViewById(R.id.buttons_container);
        mConfirmButton = findViewById(R.id.confirm_button);
        mConfirmButton.setOnClickListener(this);
        mConfirmButton.setOnTouchListener(Constants.FOCUS_TOUCH_LISTENER);
        mCancelButton = findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(this);
        mCancelButton.setOnTouchListener(Constants.FOCUS_TOUCH_LISTENER);
        mNeutralButton = findViewById(R.id.neutral_button);
        mNeutralButton.setOnClickListener(this);
        mNeutralButton.setOnTouchListener(Constants.FOCUS_TOUCH_LISTENER);
        mProgressHelper.setProgressWheel((ProgressWheel) findViewById(R.id.progressWheel));

        setTitleText(mTitleText);
        setContentText(mContentText);
        setCustomView(mCustomView);
        setCancelText(mCancelText);
        setConfirmText(mConfirmText);
        setNeutralText(mNeutralText);
        applyStroke();
        setConfirmButtonBackgroundColor(mConfirmButtonBackgroundColor);
        setConfirmButtonTextColor(mConfirmButtonTextColor);
        setCancelButtonBackgroundColor(mCancelButtonBackgroundColor);
        setCancelButtonTextColor(mCancelButtonTextColor);
        setNeutralButtonBackgroundColor(mNeutralButtonBackgroundColor);
        setNeutralButtonTextColor(mNeutralButtonTextColor);

        if (mTypeface != null) {
            applyTypeface();
        }

        changeAlertType(mAlertType, true);
    }

    private void applyTypeface() {
        if (mTitleTextView != null) mTitleTextView.setTypeface(mTypeface);
        if (mContentTextView != null) mContentTextView.setTypeface(mTypeface);
        if (mConfirmButton != null) mConfirmButton.setTypeface(mTypeface);
        if (mCancelButton != null) mCancelButton.setTypeface(mTypeface);
        if (mNeutralButton != null) mNeutralButton.setTypeface(mTypeface);
    }

    public SweetAlertDialog setTypeface(Typeface typeface) {
        mTypeface = typeface;
        applyTypeface();
        return this;
    }

    private void restore() {
        if (mCustomImage != null) mCustomImage.setVisibility(View.GONE);
        if (mErrorFrame != null) {
            mErrorFrame.setVisibility(View.GONE);
            mErrorFrame.clearAnimation();
        }
        if (mSuccessFrame != null) mSuccessFrame.setVisibility(View.GONE);
        if (mWarningFrame != null) mWarningFrame.setVisibility(View.GONE);
        if (mProgressFrame != null) mProgressFrame.setVisibility(View.GONE);
        // --- Restaurar Nuevo Frame ---
        if (mInfoFrame != null) mInfoFrame.setVisibility(View.GONE);
        // -----------------------------

        if (mConfirmButton != null) {
            mConfirmButton.setVisibility(mHideConfirmButton ? View.GONE : View.VISIBLE);
            mConfirmButton.setBackgroundResource(R.drawable.green_button_background);
        }

        adjustButtonContainerVisibility();

        if (mErrorX != null) mErrorX.clearAnimation();
        if (mSuccessTick != null) mSuccessTick.clearAnimation();
        if (mSuccessLeftMask != null) mSuccessLeftMask.clearAnimation();
        if (mSuccessRightMask != null) mSuccessRightMask.clearAnimation();
    }

    private void adjustButtonContainerVisibility() {
        boolean showButtonsContainer = false;
        if (mButtonsContainer != null) {
            for (int i = 0; i < mButtonsContainer.getChildCount(); i++) {
                View view = mButtonsContainer.getChildAt(i);
                if (view instanceof Button && view.getVisibility() == View.VISIBLE) {
                    showButtonsContainer = true;
                    break;
                }
            }
            mButtonsContainer.setVisibility(showButtonsContainer ? View.VISIBLE : View.GONE);
        }
    }

    private void playAnimation() {
        if (mAlertType == ERROR_TYPE) {
            if (mErrorFrame != null) mErrorFrame.startAnimation(mErrorInAnim);
            if (mErrorX != null) mErrorX.startAnimation(mErrorXInAnim);
        } else if (mAlertType == SUCCESS_TYPE) {
            if (mSuccessTick != null) mSuccessTick.startTickAnim();
            if (mSuccessRightMask != null) mSuccessRightMask.startAnimation(mSuccessBowAnim);
        }
    }

    private void changeAlertType(int alertType, boolean fromCreate) {
        mAlertType = alertType;
        if (mDialogView != null) {
            if (!fromCreate) {
                restore();
            }
            if (mConfirmButton != null) {
                mConfirmButton.setVisibility(mHideConfirmButton ? View.GONE : View.VISIBLE);
            }
            switch (mAlertType) {
                case ERROR_TYPE:
                    if (mErrorFrame != null) mErrorFrame.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS_TYPE:
                    if (mSuccessFrame != null) {
                        mSuccessFrame.setVisibility(View.VISIBLE);
                        if (mSuccessLeftMask != null) mSuccessLeftMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(0));
                        if (mSuccessRightMask != null) mSuccessRightMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(1));
                    }
                    break;
                case WARNING_TYPE:
                    if (mWarningFrame != null) mWarningFrame.setVisibility(View.VISIBLE);
                    break;
                // --- Caso Nuevo ---
                case INFO_TYPE:
                    if (mInfoFrame != null) mInfoFrame.setVisibility(View.VISIBLE);
                    break;
                // ------------------
                case CUSTOM_IMAGE_TYPE:
                    setCustomImage(mCustomImgDrawable);
                    break;
                case PROGRESS_TYPE:
                    if (mProgressFrame != null) mProgressFrame.setVisibility(View.VISIBLE);
                    if (mConfirmButton != null) mConfirmButton.setVisibility(View.GONE);
                    break;
            }
            adjustButtonContainerVisibility();
            if (!fromCreate) {
                playAnimation();
            }
        }
    }

    public int getAlertType() {
        return mAlertType;
    }

    public void changeAlertType(int alertType) {
        changeAlertType(alertType, false);
    }

    public String getTitleText() {
        return mTitleText;
    }

    public SweetAlertDialog setTitleText(String text) {
        mTitleText = text;
        if (mTitleTextView != null && mTitleText != null) {
            if (text.isEmpty()) {
                mTitleTextView.setVisibility(View.GONE);
            } else {
                mTitleTextView.setVisibility(View.VISIBLE);
                if (titleTextSize != 0) {
                    mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, spToPx(titleTextSize, getContext()));
                }
                mTitleTextView.setText(Html.fromHtml(mTitleText));
            }
        }
        return this;
    }

    public SweetAlertDialog setTitleText(int resId) {
        return setTitleText(getContext().getResources().getString(resId));
    }

    public SweetAlertDialog setTitleTextSize(int value) {
        this.titleTextSize = value;
        return this;
    }

    public int getTitleTextSize() {
        return titleTextSize;
    }

    public SweetAlertDialog setCustomImage(Drawable drawable) {
        mCustomImgDrawable = drawable;
        if (mCustomImage != null && mCustomImgDrawable != null) {
            mCustomImage.setVisibility(View.VISIBLE);
            mCustomImage.setImageDrawable(mCustomImgDrawable);
        }
        return this;
    }

    public SweetAlertDialog setCustomImage(int resourceId) {
        return setCustomImage(ContextCompat.getDrawable(getContext(), resourceId));
    }

    public String getContentText() {
        return mContentText;
    }

    public SweetAlertDialog setContentText(String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText(true);
            if (contentTextSize != 0) {
                mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, spToPx(contentTextSize, getContext()));
            }
            mContentTextView.setText(Html.fromHtml(mContentText));
            mContentTextView.setVisibility(View.VISIBLE);
            mCustomViewContainer.setVisibility(View.GONE);
        }
        return this;
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public SweetAlertDialog setStrokeWidth(float width) {
        this.strokeWidth = spToPx(width, getContext());
        return this;
    }

    private void applyStroke() {
        if (Float.compare(defStrokeWidth, strokeWidth) != 0) {
            setButtonBackgroundColor(mConfirmButton, ContextCompat.getColor(getContext(), R.color.main_green_color));
            setButtonBackgroundColor(mNeutralButton, ContextCompat.getColor(getContext(), R.color.main_disabled_color));
            setButtonBackgroundColor(mCancelButton, ContextCompat.getColor(getContext(), R.color.red_btn_bg_color));
        }
    }

    public boolean isShowCancelButton() {
        return mShowCancel;
    }

    public SweetAlertDialog showCancelButton(boolean isShow) {
        mShowCancel = isShow;
        if (mCancelButton != null) {
            mCancelButton.setVisibility(mShowCancel ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public boolean isShowContentText() {
        return mShowContent;
    }

    public SweetAlertDialog showContentText(boolean isShow) {
        mShowContent = isShow;
        if (mContentTextView != null) {
            mContentTextView.setVisibility(mShowContent ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public String getCancelText() {
        return mCancelText;
    }

    public SweetAlertDialog setCancelText(String text) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            showCancelButton(true);
            mCancelButton.setText(mCancelText);
        }
        return this;
    }

    public String getConfirmText() {
        return mConfirmText;
    }

    public SweetAlertDialog setConfirmText(String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    public SweetAlertDialog setConfirmButtonBackgroundColor(Integer color) {
        mConfirmButtonBackgroundColor = color;
        setButtonBackgroundColor(mConfirmButton, color);
        return this;
    }

    public Integer getConfirmButtonBackgroundColor() {
        return mConfirmButtonBackgroundColor;
    }

    public SweetAlertDialog setNeutralButtonBackgroundColor(Integer color) {
        mNeutralButtonBackgroundColor = color;
        setButtonBackgroundColor(mNeutralButton, color);
        return this;
    }

    public Integer getNeutralButtonBackgroundColor() {
        return mNeutralButtonBackgroundColor;
    }

    public SweetAlertDialog setCancelButtonBackgroundColor(Integer color) {
        mCancelButtonBackgroundColor = color;
        setButtonBackgroundColor(mCancelButton, color);
        return this;
    }

    public Integer getCancelButtonBackgroundColor() {
        return mCancelButtonBackgroundColor;
    }

    private void setButtonBackgroundColor(Button btn, Integer color) {
        if (btn != null && color != null) {
            Drawable[] drawableItems = ViewUtils.getDrawable(btn);
            if (drawableItems != null) {
                GradientDrawable gradientDrawableUnChecked = (GradientDrawable) drawableItems[1];
                gradientDrawableUnChecked.setColor(color);
                gradientDrawableUnChecked.setStroke((int) strokeWidth, genStrokeColor(color));
            }
        }
    }

    private int genStrokeColor(int color) {
        float hsv[] = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.7f;
        return Color.HSVToColor(hsv);
    }

    public SweetAlertDialog setConfirmButtonTextColor(Integer color) {
        mConfirmButtonTextColor = color;
        if (mConfirmButton != null && color != null) {
            mConfirmButton.setTextColor(mConfirmButtonTextColor);
        }
        return this;
    }

    public Integer getConfirmButtonTextColor() {
        return mConfirmButtonTextColor;
    }

    public SweetAlertDialog setNeutralButtonTextColor(Integer color) {
        mNeutralButtonTextColor = color;
        if (mNeutralButton != null && color != null) {
            mNeutralButton.setTextColor(mNeutralButtonTextColor);
        }
        return this;
    }

    public Integer getNeutralButtonTextColor() {
        return mNeutralButtonTextColor;
    }

    public SweetAlertDialog setCancelButtonTextColor(Integer color) {
        mCancelButtonTextColor = color;
        if (mCancelButton != null && color != null) {
            mCancelButton.setTextColor(mCancelButtonTextColor);
        }
        return this;
    }

    public Integer getCancelButtonTextColor() {
        return mCancelButtonTextColor;
    }

    public SweetAlertDialog setCancelClickListener(OnSweetClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    public SweetAlertDialog setConfirmClickListener(OnSweetClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    public SweetAlertDialog setNeutralText(String text) {
        mNeutralText = text;
        if (mNeutralButton != null && mNeutralText != null && !text.isEmpty()) {
            mNeutralButton.setVisibility(View.VISIBLE);
            mNeutralButton.setText(mNeutralText);
        }
        return this;
    }

    public SweetAlertDialog setNeutralClickListener(OnSweetClickListener listener) {
        mNeutralClickListener = listener;
        return this;
    }

    @Override
    public void setTitle(CharSequence title) {
        this.setTitleText(title.toString());
    }

    @Override
    public void setTitle(int titleId) {
        this.setTitleText(getContext().getResources().getString(titleId));
    }

    public Button getButton(int buttonType) {
        switch (buttonType) {
            default:
            case BUTTON_CONFIRM:
                return mConfirmButton;
            case BUTTON_CANCEL:
                return mCancelButton;
            case BUTTON_NEUTRAL:
                return mNeutralButton;
        }
    }

    public SweetAlertDialog setConfirmButton(String text, OnSweetClickListener listener) {
        this.setConfirmText(text);
        this.setConfirmClickListener(listener);
        return this;
    }

    public SweetAlertDialog setConfirmButton(int resId, OnSweetClickListener listener) {
        String text = getContext().getResources().getString(resId);
        setConfirmButton(text, listener);
        return this;
    }


    public SweetAlertDialog setCancelButton(String text, OnSweetClickListener listener) {
        this.setCancelText(text);
        this.setCancelClickListener(listener);
        return this;
    }

    public SweetAlertDialog setCancelButton(int resId, OnSweetClickListener listener) {
        String text = getContext().getResources().getString(resId);
        setCancelButton(text, listener);
        return this;
    }

    public SweetAlertDialog setNeutralButton(String text, OnSweetClickListener listener) {
        this.setNeutralText(text);
        this.setNeutralClickListener(listener);
        return this;
    }

    public SweetAlertDialog setNeutralButton(int resId, OnSweetClickListener listener) {
        String text = getContext().getResources().getString(resId);
        setNeutralButton(text, listener);
        return this;
    }

    public SweetAlertDialog setContentTextSize(int value) {
        this.contentTextSize = value;
        return this;
    }

    public int getContentTextSize() {
        return contentTextSize;
    }

    protected void onStart() {
        if (mModalInAnim != null) mDialogView.startAnimation(mModalInAnim);
        playAnimation();
    }

    public SweetAlertDialog setCustomView(View view) {
        mCustomView = view;
        if (mCustomView != null && mCustomViewContainer != null) {
            mCustomViewContainer.addView(view);
            mCustomViewContainer.setVisibility(View.VISIBLE);
            mContentTextView.setVisibility(View.GONE);
        }
        return this;
    }

    @Override
    public void show() {
        Context context = getContext();
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
        }
        try {
            super.show();
        } catch (Exception e) {
            Log.e("SweetAlertDialog", "Show error", e);
        }
    }

    @Override
    public void cancel() {
        dismissWithAnimation(true);
    }

    public void dismissWithAnimation() {
        dismissWithAnimation(false);
    }

    private void dismissWithAnimation(boolean fromCancel) {
        mCloseFromCancel = fromCancel;
        if (mOverlayOutAnim != null && mDialogView != null) {
            ((ViewGroup) mDialogView).getChildAt(0).startAnimation(mOverlayOutAnim);
            mDialogView.startAnimation(mModalOutAnim);
        } else {
            if (fromCancel) super.cancel(); else super.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_button) {
            if (mCancelClickListener != null) {
                mCancelClickListener.onClick(SweetAlertDialog.this);
            } else {
                dismissWithAnimation();
            }
        } else if (v.getId() == R.id.confirm_button) {
            if (mConfirmClickListener != null) {
                mConfirmClickListener.onClick(SweetAlertDialog.this);
            } else {
                dismissWithAnimation();
            }
        } else if (v.getId() == R.id.neutral_button) {
            if (mNeutralClickListener != null) {
                mNeutralClickListener.onClick(SweetAlertDialog.this);
            } else {
                dismissWithAnimation();
            }
        }
    }

    public ProgressHelper getProgressHelper() {
        return mProgressHelper;
    }

    public SweetAlertDialog setHideKeyBoardOnDismiss(boolean hide) {
        this.mHideKeyBoardOnDismiss = hide;
        return this;
    }

    public boolean isHideKeyBoardOnDismiss() {
        return this.mHideKeyBoardOnDismiss;
    }

    private void hideSoftKeyboard() {
        Activity activity = getOwnerActivity();
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && activity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }
}