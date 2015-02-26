package de.jonas_kraus.tattoochooser;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Layout;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import de.jonas_kraus.tattoochooser.util.ImageProcessor;

/**
 * Created by jonas on 19.01.2015.
 */
public class Viewer extends Activity implements View.OnTouchListener, View.OnClickListener {

    private static final String TAG = "Touch";
    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    Vibrator vibrator;
    Bitmap bmp;
    ImageView imageView;
    Context context;
    private ShareActionProvider mShareActionProvider;
    private File imageFile;
    private ImageProcessor imgProcessor;
    private ImageProcessor imgSharingProcessor;
    private byte[] b;
    private Bundle extras;
    private Intent intent;
    private Button downloadButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer);

        extras = getIntent().getExtras();
        b = extras.getByteArray("picture");
        bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
        imageView = (ImageView) findViewById(R.id.fullscreen_image);
        backButton = (Button) findViewById(R.id.back_view);
        downloadButton = (Button) findViewById(R.id.save_view);
        imageView.setImageBitmap(bmp);
        vibrator = (Vibrator) getSystemService(MainActivity.VIBRATOR_SERVICE);
        context = getApplicationContext();
        imgProcessor = new ImageProcessor(context);
        imgSharingProcessor = new ImageProcessor();
        // Toast.makeText(context, "Long press on Image to view larger", Toast.LENGTH_SHORT);
    }

    private void addOnlongClickListener() {
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                vibrator.vibrate(50);
                Toast.makeText(Viewer.this, "Save image to internal storage", Toast.LENGTH_SHORT);
                imageFile = imgProcessor.storeImage(bmp);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //addOnlongClickListener();
        imageView.setOnTouchListener(this);
        downloadButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        // setShareIntent(setUpShareIntent());
        //addDownloadButtonlistener();
        //addBackButtonListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // imageView.setOnLongClickListener(null);
        imageView.setOnTouchListener(null);
        downloadButton.setOnTouchListener(null);
        backButton.setOnTouchListener(null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        dumpEvent(event);
        // Handle touch events here...

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                //matrix.set(view.getMatrix());
                savedMatrix.set(matrix);


                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                }
                else if (mode == ZOOM)
                {
                    // pinch zooming
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix); // display the transformation on screen

        return true; // indicate event was handled
    }
    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share_image);

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

        if (id == R.id.menu_item_share_image) {
            //setShareIntent(setUpShareIntent());
            startActivity(Intent.createChooser(intent, "Share via"));
            //imageFile.delete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent setUpShareIntent() {
        imageFile = imgProcessor.storeImage(bmp);
        Uri uri = Uri.fromFile(imageFile);

        intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/jpeg");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Image from TattooChoser");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "This will be my new tattoo :)");
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        return intent;
    }

    @Override
    public void onClick(View v) {
        if (v == downloadButton ) {
            vibrator.vibrate(50);
            imgProcessor.storeImage(bmp);
            Toast.makeText(context,"Saved Image to dir TattooChooser", Toast.LENGTH_SHORT).show();
            if (imageFile != null) {
                boolean del = imageFile.delete();
                Log.d("delete file", del + "");
            }
        } else if ( v == backButton) {
            vibrator.vibrate(50);
            if (imageFile != null) {
                boolean del = imageFile.delete();
                Log.d("delete file", del + "");
            }
            finish();
            Intent myIntent = new Intent(Viewer.this, MainActivity.class);
            startActivity(myIntent);
        }
    }
    public void onBackPressed(){
        if (imageFile != null) {
            boolean del = imageFile.delete();
            Log.d("delete file", del + "");
        }
        super.onBackPressed();
    }
}
