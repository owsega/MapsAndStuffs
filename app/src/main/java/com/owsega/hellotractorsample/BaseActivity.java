package com.owsega.hellotractorsample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.kinvey.android.Client;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Base Activity, to be overridden by all other activities in the app because this guy here:
 * <ul>
 * <li>Sets up Realm</li>
 * <li>Sets up ButterKnife binding. If the activity is going to set content view, it must
 * contain a CoordinatorLayout with {@link R.id#coordinator_layout}</li>
 * </ul>
 *
 * @author Seyi Owoeye. Created on 1/25/17.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Realm realm;
    protected Client kinvey;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout rootCoordinatorLayout;

    public Client getKinvey() {
        if (kinvey == null){
            kinvey = new Client.Builder(//key, appSecret
                    getApplicationContext()).build();
        }
        return kinvey;
    }

    public static Realm getRealm(Context context) {
        return ((BaseActivity) context).realm;
    }

    public static void snack(Context context, CharSequence text) {
        if (context instanceof BaseActivity) ((BaseActivity) context).snack(text);
        else Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void snack(Context context, @StringRes int textId) {
        if (context instanceof BaseActivity) ((BaseActivity) context).snack(textId);
        else Toast.makeText(context, textId, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        realm = Realm.getDefaultInstance();
        kinvey = getKinvey();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) { // guard against weird low-budget phones
            realm.close();
            realm = null;
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        ButterKnife.bind(this);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        ButterKnife.bind(this);
    }

    public void snack(@StringRes int textId) {
        snack(getResources().getText(textId));
    }

    public void snack(CharSequence text) {
        Snackbar.make(rootCoordinatorLayout, text, Snackbar.LENGTH_SHORT).show();
    }
}
