package com.jazperfox.sweetalert.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.jazperfox.sweetalert.Constants;
import com.jazperfox.sweetalert.SweetAlertDialog;

import java.util.ArrayList;
import java.util.List;

public class SampleActivity extends AppCompatActivity implements View.OnClickListener {

    private int progressIndex = -1;
    private final List<SweetAlertDialog> activeDialogs = new ArrayList<>();
    private CountDownTimer progressTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_activity);

        int[] btnIds = {
                R.id.basic_test, R.id.styled_text_and_stroke, R.id.basic_test_without_buttons, R.id.under_text_test,
                R.id.error_text_test, R.id.success_text_test, R.id.warning_confirm_test, R.id.warning_cancel_test,
                R.id.custom_img_test, R.id.progress_dialog, R.id.neutral_btn_test, R.id.disabled_btn_test, R.id.dark_style,
                R.id.custom_view_test, R.id.custom_btn_colors_test
        };
        for (int id : btnIds) {
            View v = findViewById(id);
            if (v != null) {
                v.setOnClickListener(this);
                v.setOnTouchListener(Constants.FOCUS_TOUCH_LISTENER);
            }
        }
    }

    private void showDialog(SweetAlertDialog dialog) {
        if (!isFinishing() && !isDestroyed()) {
            activeDialogs.add(dialog);
            dialog.setOnDismissListener(d -> activeDialogs.remove(dialog));
            dialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer = null;
        }
        for (SweetAlertDialog dialog : new ArrayList<>(activeDialogs)) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        activeDialogs.clear();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.basic_test) {
            SweetAlertDialog sd = new SweetAlertDialog(this);
            sd.setCancelable(true);
            sd.setCanceledOnTouchOutside(true);
            sd.setContentText(getString(R.string.title_text));
            showDialog(sd);
        } else if (id == R.id.basic_test_without_buttons) {
            SweetAlertDialog sd2 = new SweetAlertDialog(this);
            sd2.setCancelable(true);
            sd2.setCanceledOnTouchOutside(true);
            sd2.setContentText(getString(R.string.title_text));
            sd2.hideConfirmButton();
            showDialog(sd2);
        } else if (id == R.id.under_text_test) {
            showDialog(new SweetAlertDialog(this)
                    .setTitleText(getString(R.string.title_text))
                    .setContentText(getString(R.string.content_text)));
        } else if (id == R.id.styled_text_and_stroke) {
            showDialog(new SweetAlertDialog(this)
                    .setTitleText(getString(R.string.styled_title))
                    .setContentText(getString(R.string.styled_content))
                    .setContentTextSize(21)
                    .setStrokeWidth(2));
        } else if (id == R.id.error_text_test) {
            showDialog(new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error_title))
                    .setContentText(getString(R.string.error_content)));
        } else if (id == R.id.success_text_test) {
            showDialog(new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getString(R.string.success_title))
                    .setContentText(getString(R.string.success_content)));
        } else if (id == R.id.warning_confirm_test) {
            showDialog(new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.warning_title))
                    .setContentText(getString(R.string.warning_content))
                    .setCancelButton(getString(R.string.delete_confirm), sweetAlertDialog -> sweetAlertDialog.setTitleText(getString(R.string.deleted_title))
                            .setContentText(getString(R.string.deleted_content))
                            .setConfirmClickListener(null)
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)));
        } else if (id == R.id.warning_cancel_test) {
            showDialog(new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.warning_title))
                    .setContentText(getString(R.string.warning_content))
                    .setCancelText(getString(R.string.cancel))
                    .setConfirmText(getString(R.string.delete_confirm))
                    .showCancelButton(true)
                    .setCancelClickListener(sDialog -> sDialog.setTitleText(getString(R.string.cancelled_title))
                            .setContentText(getString(R.string.cancelled_content))
                            .setConfirmText("OK")
                            .showCancelButton(false)
                            .setCancelClickListener(null)
                            .setConfirmClickListener(null)
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE))
                    .setConfirmClickListener(sDialog -> sDialog.setTitleText(getString(R.string.deleted_title))
                            .setContentText(getString(R.string.deleted_content))
                            .setConfirmText("OK")
                            .showCancelButton(false)
                            .setCancelClickListener(null)
                            .setConfirmClickListener(null)
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)));
        } else if (id == R.id.custom_img_test) {
            showDialog(new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setTitleText(getString(R.string.app_name))
                    .setContentText(getString(R.string.custom_icon_content))
                    .setCustomImage(R.drawable.custom_img));
        } else if (id == R.id.progress_dialog) {
            final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                    .setTitleText(getString(R.string.loading));
            showDialog(pDialog);
            pDialog.setCancelable(false);
            if (progressTimer != null) progressTimer.cancel();
            progressTimer = new CountDownTimer(800 * 7, 800) {
                public void onTick(long millisUntilFinished) {
                    progressIndex++;
                    int color;
                    switch (progressIndex) {
                        case 0: color = ContextCompat.getColor(SampleActivity.this, R.color.blue_btn_bg_color); break;
                        case 1: color = ContextCompat.getColor(SampleActivity.this, R.color.material_deep_teal_50); break;
                        case 2: color = ContextCompat.getColor(SampleActivity.this, R.color.success_stroke_color); break;
                        case 3: color = ContextCompat.getColor(SampleActivity.this, R.color.material_deep_teal_20); break;
                        case 4: color = ContextCompat.getColor(SampleActivity.this, R.color.material_blue_grey_80); break;
                        case 5: color = ContextCompat.getColor(SampleActivity.this, R.color.warning_stroke_color); break;
                        default: color = ContextCompat.getColor(SampleActivity.this, R.color.success_stroke_color); break;
                    }
                    pDialog.getProgressHelper().setBarColor(color);
                }

                public void onFinish() {
                    progressIndex = -1;
                    pDialog.setTitleText(getString(R.string.completed))
                            .setConfirmText("OK")
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                }
            }.start();
        } else if (id == R.id.neutral_btn_test) {
            showDialog(new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(getString(R.string.neutral_title))
                    .setContentText(getString(R.string.neutral_content))
                    .setConfirmText("Confirm")
                    .setCancelText(getString(R.string.cancel))
                    .setNeutralText(getString(R.string.neutral_text)));
        } else if (id == R.id.disabled_btn_test) {
            final SweetAlertDialog disabledBtnDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(getString(R.string.title_text))
                    .setContentText(getString(R.string.disabled_ok_button))
                    .setConfirmText("OK")
                    .setCancelText(getString(R.string.cancel))
                    .setNeutralText(getString(R.string.neutral_text));

            disabledBtnDialog.setOnShowListener(dialog -> disabledBtnDialog.getButton(SweetAlertDialog.BUTTON_CONFIRM).setEnabled(false));
            showDialog(disabledBtnDialog);
        } else if (id == R.id.dark_style) {
            SweetAlertDialog.DARK_STYLE = ((CheckBox) v).isChecked();
        } else if (id == R.id.custom_view_test) {
            final EditText editText = new EditText(this);
            final CheckBox checkBox = new CheckBox(this);
            editText.setText("Some edit text");
            checkBox.setChecked(true);
            checkBox.setText("Some checkbox");

            if (SweetAlertDialog.DARK_STYLE) {
                editText.setTextColor(Color.WHITE);
                checkBox.setTextColor(Color.WHITE);
            }

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(editText);
            linearLayout.addView(checkBox);

            SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(getString(R.string.custom_view_title))
                    .hideConfirmButton();

            dialog.setCustomView(linearLayout);
            showDialog(dialog);
        } else if (id == R.id.custom_btn_colors_test) {
            showDialog(new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(getString(R.string.custom_btn_colors))
                    .setCancelButton("red", null)
                    .setCancelButtonBackgroundColor(Color.RED)
                    .setNeutralButton("cyan", null)
                    .setNeutralButtonBackgroundColor(Color.CYAN)
                    .setConfirmButton("blue", null)
                    .setConfirmButtonBackgroundColor(Color.BLUE));
        }
    }
}
