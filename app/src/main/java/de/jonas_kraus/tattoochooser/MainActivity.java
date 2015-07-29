package de.jonas_kraus.tattoochooser;

import android.app.Activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import de.jonas_kraus.tattoochooser.util.ImageProcessor;


public class MainActivity extends Activity {

    Button downloadButton, playButton, viewButton;
    ImageView img;
    AnimationDrawable frameAnimation;
    Vibrator vibrator;
    Context context;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = (ImageView)findViewById(R.id.spinning);
        img.setBackgroundResource(R.drawable.spin_animation);

        // Get the background, which has been compiled to an AnimationDrawable object.
        frameAnimation = (AnimationDrawable) img.getBackground();
        vibrator = (Vibrator) getSystemService(MainActivity.VIBRATOR_SERVICE);
        context = getApplicationContext();

        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            private long time = 0;

            @Override
            public void run()
            {
                // do stuff then
                // can call h again after work!
                Random rand = new Random();
                time += rand.nextInt(3678-2123)+2123;
                Log.d("Timer", "Going for... " + time);
                h.postDelayed(this, time);
                time = 0;
                frameAnimation.stop();
            }
        }, 0);

    }

    public void addLongImageClickListener() {
        img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                vibrator.vibrate(50);
                viewImageInViewer();
                return false;
            }
        });
    }

    public void addImageClickListener() {
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameAnimation.start();
            }
        });
    }

    public void addDownloadButtonlistener() {
        downloadButton = (Button) findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(50);

                Bitmap bitmap = getBitmapFromImageView(img);
                byte[] b = getByteArrayFromBitmap(bitmap);
                ImageProcessor imgProcessor = new ImageProcessor(context);
                imgProcessor.storeImage(bitmap);
                /* // just save the file to internal storage
                Intent myIntent = new Intent(MainActivity.this, Viewer.class);
                myIntent.putExtra("picture", b);
                startActivity(myIntent);
                */
                Toast.makeText(context,"Saved Image to dir TattooChooser", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void addPlayButtonlistener() {
        playButton = (Button) findViewById(R.id.hebel);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"Play",Toast.LENGTH_SHORT).show();
                vibrator.vibrate(50);
                // Start the animation (looped playback by default).
                frameAnimation.start();
            }
        });
    }
    public void addViewButtonlistener() {
        viewButton = (Button) findViewById(R.id.viewButton);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(50);
                viewImageInViewer();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        setShareIntent(setUpShareIntent());
        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_item_share) {
            setShareIntent(setUpShareIntent());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent setUpShareIntent() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "I think you might be interested in this funny tattoo choosing app";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "The amazing TattooChooser App");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        //startActivity(Intent.createChooser(sharingIntent, "Share via"));
        return sharingIntent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDownloadButtonlistener();
        addPlayButtonlistener();
        addViewButtonlistener();
        addLongImageClickListener();
        addImageClickListener();
        setShareIntent(setUpShareIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        downloadButton.setOnClickListener(null);
        playButton.setOnClickListener(null);
        viewButton.setOnClickListener(null);
        img.setOnClickListener(null);
        img.setOnLongClickListener(null);
    }

    private byte[] getByteArrayFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return b;
    }

    private Bitmap getBitmapFromImageView(ImageView imageView) {
        Drawable drawable = imageView.getBackground().getCurrent();
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable)drawable).getBitmap();
        }
        return bitmap;
    }

    private void viewImageInViewer() {
        Intent myIntent = new Intent(MainActivity.this, Viewer.class);
        Bitmap bitmap = getBitmapFromImageView(img);
        byte[] b = getByteArrayFromBitmap(bitmap);

        myIntent.putExtra("picture", b);
        startActivity(myIntent);
    }

    // FRAGMENT INNER CLASS ************************************************************************
    public static class AdFragment extends Fragment {

        private AdView mAdView;

        public AdFragment() {
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
            // values/strings.xml.
            mAdView = (AdView) getView().findViewById(R.id.adView);

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ad, container, false);
        }

        /** Called when leaving the activity */
        @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        /** Called when returning to the activity */
        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }

        }

        /** Called before the activity is destroyed */
        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }
    }
}


