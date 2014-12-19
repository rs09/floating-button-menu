package com.example.rishabh.myfloatingbutton;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.floatingbuttonmenu.FloatingButtonMenu;
import com.floatingbuttonmenu.animation.AlphaFloatingButtonAnimationHandler;
import com.floatingbuttonmenu.animation.FloatingButtonAnimationHandlerBase;
import com.floatingbuttonmenu.animation.RotateTranslateAlphaFloatingButtonAnimationHandler;
import com.floatingbuttonmenu.animation.RotateTranslateFloatingButtonAnimationHandler;
import com.floatingbuttonmenu.animation.TranslateAlphaFloatingButtonAnimationHandler;

public class MainActivity extends Activity {
    private static final int[] ITEM_DRAWABLES = {R.drawable.composer_camera, R.drawable.composer_music,
            R.drawable.composer_place, R.drawable.composer_sleep};

    private FloatingButtonAnimationHandlerBase animationHandler;
    private Interpolator openInterpolator;
    private Interpolator closeInterpolator;
    private long duration;
    private long startOffsetChild;
    private FloatingButtonMenu floatingButtonMenu;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initViewsAndParams();

        floatingButtonMenu.setAnimationHandler(animationHandler)
                .setOnStateChangeListener(new FloatingButtonMenu.OnStateChangeListener() {
                    @Override
                    public void onMenuStateChanged(boolean opened) {
                        Toast.makeText(MainActivity.this, "Menu opened: " + opened, Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnItemClickListener(new FloatingButtonMenu.OnItemClickListener() {
                    @Override
                    public void onItemClick(View childView, int index) {
                        Toast.makeText(MainActivity.this, "Child clicked: " + index + " view: " + childView, Toast.LENGTH_SHORT).show();
                    }
                });

        initFloatingLayoutMenu(floatingButtonMenu, ITEM_DRAWABLES);
    }

    private void initViewsAndParams() {
        floatingButtonMenu = (FloatingButtonMenu) findViewById(R.id.floating_menu);

        openInterpolator = new OvershootInterpolator(1.5f);
        closeInterpolator = new AnticipateInterpolator(1.5f);
        duration = 500;
        startOffsetChild = 80;

        buildAnimationHandler(new TranslateAlphaFloatingButtonAnimationHandler.Builder(floatingButtonMenu));
    }

    private void buildAnimationHandler(FloatingButtonAnimationHandlerBase.Builder builder) {
        animationHandler = builder
                .setOpenInterpolator(openInterpolator)
                .setCloseInterpolator(closeInterpolator)
                .setDuration(duration)
                .setStartOffsetBetweenEachChild(startOffsetChild)
                .build();
    }

    private void initFloatingLayoutMenu(FloatingButtonMenu menu, int[] itemDrawables) {
        final int itemCount = itemDrawables.length;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < itemCount; i++) {
            ImageView item = (ImageView) inflater.inflate(R.layout.child_image, floatingButtonMenu, false);
            item.setImageResource(itemDrawables[i]);
            menu.addItem(item);
        }
    }

    public void onButtonClick(View v) {
        switch (v.getId()) {
            case R.id.anim_btn:
                showListAlertDialog(LIST_DIALOG_ANIMATION);
                break;
            case R.id.duration_btn:
                showTextBoxDiloag(true);
                break;
            case R.id.offset_btn:
                showTextBoxDiloag(false);
                break;
            case R.id.open_interpolator_btn:
                showListAlertDialog(LIST_DIALOG_OPEN_INTER);
                break;
            case R.id.close_interpolator_btn:
                showListAlertDialog(LIST_DIALOG_CLOSE_INTER);
                break;
        }
    }

    private String[] animationList = {"Translate Alpha (Default)", "Rotate Translate", "Rotate Translate Alpha", "Alpha"};
    private String[] openInterpolatorList = {"AccelerateDecelerateInterpolator", "AccelerateInterpolator", "AnticipateInterpolator", "AnticipateOvershootInterpolator", "BounceInterpolator", "DecelerateInterpolator", "OvershootInterpolator (Default)", "Linear"};
    private String[] closeInterpolatorList = {"AccelerateDecelerateInterpolator", "AccelerateInterpolator", "AnticipateInterpolator", "AnticipateOvershootInterpolator", "BounceInterpolator", "DecelerateInterpolator (Default)", "OvershootInterpolator", "Linear"};

    private static final int LIST_DIALOG_ANIMATION = 1;
    private static final int LIST_DIALOG_OPEN_INTER = 2;
    private static final int LIST_DIALOG_CLOSE_INTER = 3;

    private void showListAlertDialog(final int dialogType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        CharSequence[] items = animationList;
        if (dialogType == LIST_DIALOG_OPEN_INTER) {
            items = openInterpolatorList;
        } else if (dialogType == LIST_DIALOG_CLOSE_INTER) {
            items = closeInterpolatorList;
        }

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (dialogType) {
                    case LIST_DIALOG_ANIMATION:
                        handleOnItemClickForAnimations(which);
                        break;
                    case LIST_DIALOG_OPEN_INTER:
                        handleOnItemClickForInterpolators(true, which);
                        break;
                    case LIST_DIALOG_CLOSE_INTER:
                        handleOnItemClickForInterpolators(false, which);
                        break;
                }
            }
        });
        builder.show();
    }

    private void showTextBoxDiloag(final boolean changeDuration) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.edit_text_dialog);

        final EditText text = (EditText) dialog.findViewById(R.id.text);
        Button okBtn = (Button) dialog.findViewById(R.id.ok_btn);

        text.setText(Long.toString(changeDuration ? duration : startOffsetChild));

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeText = text.getText().toString();
                try {
                    long time = Long.parseLong(timeText);
                    if (changeDuration) {
                        updateDuration(time);
                    } else {
                        updateStartOffset(time);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Enter a valid value", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void updateDuration(long duration) {
        this.duration = duration;
        animationHandler.setDuration(duration);
    }

    private void updateStartOffset(long startOffset) {
        this.startOffsetChild = startOffset;
        animationHandler.setStartOffsetBetweenEachChild(startOffset);
    }

    private void handleOnItemClickForAnimations(int which) {
        switch (which) {
            case 0:
                buildAnimationHandler(new TranslateAlphaFloatingButtonAnimationHandler.Builder(floatingButtonMenu));
                break;
            case 1:
                buildAnimationHandler(new RotateTranslateFloatingButtonAnimationHandler.Builder(floatingButtonMenu));
                break;
            case 2:
                buildAnimationHandler(new RotateTranslateAlphaFloatingButtonAnimationHandler.Builder(floatingButtonMenu));
                break;
            case 3:
                buildAnimationHandler(new AlphaFloatingButtonAnimationHandler.Builder(floatingButtonMenu));
                break;
        }
        floatingButtonMenu.setAnimationHandler(animationHandler);
    }

    private void handleOnItemClickForInterpolators(boolean open, int which) {
        Interpolator interpolator = null;
        switch (which) {
            case 0:
                interpolator = new AccelerateDecelerateInterpolator();
                break;
            case 1:
                interpolator = new AccelerateInterpolator(1.5f);
                break;
            case 2:
                interpolator = new AnticipateInterpolator(1.5f);
                break;
            case 3:
                interpolator = new AnticipateOvershootInterpolator(1.5f, 1.5f);
                break;
            case 4:
                interpolator = new BounceInterpolator();
                break;
            case 5:
                interpolator = new DecelerateInterpolator(1.5f);
                break;
            case 6:
                interpolator = new OvershootInterpolator(1.5f);
                break;
            case 7:
                interpolator = new LinearInterpolator();
                break;
        }

        if (open) {
            animationHandler.setOpenInterpolator(interpolator);
        } else {
            animationHandler.setCloseInterpolator(interpolator);
        }
    }
}
