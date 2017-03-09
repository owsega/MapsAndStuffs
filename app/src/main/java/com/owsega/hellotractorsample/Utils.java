package com.owsega.hellotractorsample;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.owsega.hellotractorsample.realm.Farmer;

import io.realm.Realm;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;
import rebus.permissionutils.PermissionUtils;
import rebus.permissionutils.SmartCallback;

/**
 * static methods for ease of life
 *
 * @author Seyi Owoeye. Created on 3/9/17.
 */
public class Utils {

    /**
     * ask the user for permission to access Location Services, else close the app
     */
    public static void verifyLocationPermissions(final BaseActivity ctx) {
        boolean granted = PermissionUtils.isGranted(ctx, PermissionEnum.ACCESS_FINE_LOCATION);
        if (!granted) {
            ctx.snack(R.string.grant_locations_permissn);
            PermissionManager.with(ctx)
                    .permission(PermissionEnum.READ_EXTERNAL_STORAGE)
                    .callback(new SmartCallback() {
                        @Override
                        public void result(boolean granted, boolean deniedForever) {
                            if (deniedForever) {
                                new AlertDialog.Builder(ctx)
                                        .setMessage(R.string.grant_pictures_permissn_in_settings)
                                        .setPositiveButton(R.string.yes,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface d, int which) {
                                                        PermissionUtils.openApplicationSettings(
                                                                ctx, ctx.getPackageName());
                                                    }
                                                })
                                        .setNegativeButton(R.string.no, null)
                                        .show();
                                return;
                            }
                            if (!granted) ctx.finish(); //todo should I really end the app here???
                        }
                    })
                    .ask();
        }
    }

    static String[] names = new String[]{
            "Seyi", "Hammed", "Gabe", "Aminu", "Kaura", "Mariam", "Chika", "Misturah",
            "Khan", "Khadijat", "John", "Christopher", "Simbi", "Dan"
    };

    public static void addDummyFarmers(Realm realm) {

        long farmerCount = realm.where(Farmer.class).count();
        if (farmerCount < 25) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (int i = 0; i < 10; i++) {
                        Farmer farmer = realm.createObject(Farmer.class);
                        farmer.setFarmSize(Math.random() * 120000);
                        farmer.setLatitude(6 + Math.random() * 5);
                        farmer.setLongitude(3 + Math.random() * 8);
                        farmer.setName(names[(int) (Math.random() * names.length)] + " " +
                                names[(int) (Math.random() * names.length)]);
                        farmer.setPhone("08106184121");
                    }
                }
            });
        }
    }
}
