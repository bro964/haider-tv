package com.haider.tv;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    // عدّل هذي القائمة لتغيير البطاقات بالترتيب.
    // اكتب اسم الحزمة، أو SETTINGS للإعدادات، أو كلمة من اسم التطبيق.
    private static final String[] TILES = {
            "com.google.android.youtube.tv",
            "SETTINGS",
            "cinemana"
    };

    private static final int BLUE = 0xFF2F8CFF;
    private final List<View> cards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF0B0F14, 0xFF1A2028}));

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(column, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        column.setPadding(0, dp(60), 0, 0);

        TextClock time = new TextClock(this);
        time.setFormat12Hour("h:mm");
        time.setFormat24Hour("HH:mm");
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 56);
        time.setTextColor(Color.WHITE);
        column.addView(time);

        TextClock date = new TextClock(this);
        date.setFormat12Hour("EEEE, d MMMM");
        date.setFormat24Hour("EEEE, d MMMM");
        date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        date.setTextColor(0xFFB0B8C4);
        column.addView(date);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(120);
        column.addView(row, rowParams);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> launchables = pm.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0);

        for (String key : TILES) {
            Intent intent = null;
            Drawable icon = null;

            if (key.equals("SETTINGS")) {
                intent = new Intent(Settings.ACTION_SETTINGS);
                icon = getDrawable(android.R.drawable.ic_menu_preferences);
            } else {
                for (ResolveInfo ri : launchables) {
                    String pkg = ri.activityInfo.packageName;
                    String label = ri.loadLabel(pm).toString().toLowerCase();
                    if (pkg.equals(key) || label.contains(key.toLowerCase())) {
                        intent = pm.getLeanbackLaunchIntentForPackage(pkg);
                        if (intent == null) intent = pm.getLaunchIntentForPackage(pkg);
                        icon = ri.loadIcon(pm);
                        break;
                    }
                }
            }
            if (intent != null) {
                row.addView(makeCard(intent, icon));
            }
        }

        setContentView(root);

        new Handler(Looper.getMainLooper()).post(() -> {
            if (!cards.isEmpty()) cards.get(0).requestFocus();
        });
    }

    private View makeCard(final Intent intent, Drawable icon) {
        final GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(dp(24));
        shape.setColor(0xFF1E252E);
        shape.setStroke(dp(4), Color.TRANSPARENT);

        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER);
        card.setBackground(shape);
        card.setFocusable(true);
        card.setClickable(true);

        ImageView img = new ImageView(this);
        img.setImageDrawable(icon);
        card.addView(img, new LinearLayout.LayoutParams(dp(120), dp(120)));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(300), dp(200));
        lp.setMargins(dp(16), 0, dp(16), 0);
        card.setLayoutParams(lp);

        card.setOnFocusChangeListener((v, hasFocus) -> {
            shape.setStroke(dp(4), hasFocus ? BLUE : Color.TRANSPARENT);
            float scale = hasFocus ? 1.1f : 1f;
            v.animate().scaleX(scale).scaleY(scale).setDuration(150).start();
        });
        card.setOnClickListener(v -> launch(intent));

        cards.add(card);
        return card;
    }

    private void launch(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح التطبيق", Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
