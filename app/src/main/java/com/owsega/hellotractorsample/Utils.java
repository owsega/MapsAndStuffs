package com.owsega.hellotractorsample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.owsega.hellotractorsample.realm.Farmer;

import java.io.ByteArrayOutputStream;

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

    static String[] names = new String[]{
            "Seyi", "Hammed", "Gabe", "Aminu", "Kaura", "Mariam", "Chika", "Misturah",
            "Khan", "Khadijat", "John", "Christopher", "Simbi", "Dan"
    };

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

    public static void showDeleteFarmerDialog(Context ctx, Realm realm, final Farmer farmer) {
        new AlertDialog.Builder(ctx)
                .setMessage(ctx.getString(R.string.delete_armer_confirmation, farmer.getName()))
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                Farmer.deleteFromRealm(farmer);
                                //todo notify ui and server
                            }
                        })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    public static void addDummyFarmers(final Context context, Realm realm) {

        long farmerCount = realm.where(Farmer.class).count();
        if (farmerCount < 25) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.deleteAll();

                    Farmer farmer =  new Farmer()
                            .setLatitude(9.078875)
                            .setLatitude(7.484294)
                            .setName("Hello Tractor Inc")
                            .setFarmSize(0)
                            .setPhone("09096909999");
                    getFarmerAddress(context, realm, farmer);
                    realm.copyToRealmOrUpdate(farmer);

                    for (int i = 0; i < 10; i++) {
                        farmer =  new Farmer()
                                .setFarmSize(Math.random() * 120000)
                                .setLatitude(6 + Math.random() * 5)
                                .setLongitude(3 + Math.random() * 8)
                                .setName(names[(int) (Math.random() * names.length)] + " " +
                                        names[(int) (Math.random() * names.length)])
                                .setPhone("08106184121");
                        getFarmerAddress(context, realm, farmer);
                        realm.copyToRealmOrUpdate(farmer);
                    }
                }
            });
        }
    }

    public static int dpToPx(Context context, int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
        return (int) px;
    }

    public static void getFarmerAddress(Context context, Realm realm, Farmer farmer) {
        Intent intent = new Intent(context, FetchAddressIntentService.class);
        Location location = new Location("");
        location.setLatitude(farmer.getLatitude());
        location.setLongitude(farmer.getLongitude());
        intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, location);
        intent.putExtra(FetchAddressIntentService.FARMER_EXTRA, farmer.getId());
        context.startService(intent);
    }

    /**
     * uses Glide to load the user's profile pic (from SharedPref) into the supplied imageView
     */
    public static void loadProfilePic(Context context, ImageView profilePic, String pictureHexCode) {
        if (pictureHexCode != null) {
            try {
                byte[] decodedString = Base64.decode(pictureHexCode, Base64.DEFAULT);
                Glide.with(context).load(decodedString).into(profilePic);
            } catch (Exception e) {
                Log.e("ImageUtils", e.getMessage(), e);
                profilePic.setImageResource(R.drawable.avatar);
            }
        } else {
            profilePic.setImageResource(R.drawable.avatar);
        }
    }

    public static String getImageString(Drawable drawable) {
        return Base64.encodeToString(getByteArrayFromBitmap(drawableToBitmap(drawable)), Base64.DEFAULT);
    }

    /**
     * returns a byte array from the given bitmap.
     * The process is generally lossless as it uses uses PNG compression method and 100% compression
     * quality
     */
    public static byte[] getByteArrayFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return outputStream.toByteArray();
        } catch (OutOfMemoryError | Exception e) {
            Log.e("ImageUtils", "Error while processing image " + e.getLocalizedMessage());
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                Log.e("ImageUtils", "Error closing stream " + e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Convert a drawable object into a Bitmap.
     * You can checkout {@link CircularImageView#drawableToBitmap(Drawable)} if you want to see
     * another one that can return null. This should never return null
     *
     * @param drawable Drawable to extract a Bitmap from.
     * @return A Bitmap created from the drawable parameter.
     */
    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null)
                return bitmapDrawable.getBitmap();
        }
        if (drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0) try {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            Log.e("ImageUtils", "Encountered OutOfMemoryError while generating bitmap!");
        }
        if (bitmap == null) // if all has failed till now, Single color bitmap will be created of 1x1 pixel
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    public class ResizeAnimation extends Animation {
        final int targetHeight;
        View view;
        int startHeight;

        public ResizeAnimation(View view, int targetHeight, int startHeight) {
            this.view = view;
            this.targetHeight = targetHeight;
            this.startHeight = startHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int newHeight = (int) (startHeight + targetHeight * interpolatedTime);
            //to support decent animation, change new heigt as Nico S. recommended in comments
            //int newHeight = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
            view.getLayoutParams().height = newHeight;
            view.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
