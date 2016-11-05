package edu.kaist.mskers.toondra;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Class AutoscrollActivity
 * This class uses the front facing camera to detect face.
 */
public class AutoscrollActivity extends AppCompatActivity
        implements SurfaceHolder.Callback, View.OnClickListener {
  private static final String TAG = "FaceTracker";
  private static final int MY_PERMISSIONS_USE_CAMERA = 1;
  private GraphicOverlay graphicOverlay;
  private CameraSource cameraSource;
  private SurfaceView cameraview;
  private CountDownTimer countDownTimer;
  private int scrollVelocity = 0;
  private Button buttonUp;
  private Button buttonDown;
  private ScrollView scrollView;

  /**
   * Prepares resources such as camera view, text view, and graphic overlay.
   * @param savedInstanceState current context
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scrolldebug_layout);
    cameraview = (SurfaceView) findViewById(R.id.surfaceView);
    TextView textarea = (TextView) findViewById(R.id.textView);
    graphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
    scrollView = (ScrollView) findViewById(R.id.scrollView);
    buttonUp = (Button) findViewById(R.id.buttonUp);
    buttonDown = (Button) findViewById(R.id.buttonDown);
    buttonUp.setOnClickListener(this);
    buttonDown.setOnClickListener(this);
    scrollView.setOnTouchListener(touchEvent);

    SurfaceHolder surfaceHolder = cameraview.getHolder();
    surfaceHolder.addCallback(this);
    String temp = "Lorem ipsum dolor sit amet, vel minimum conceptam constituto eu, cum no iusto doming molestie, te qui vero putent labitur. Justo facete pri no. Labitur dignissim reprimique ne nam, id sit amet interpretaris. Has at ornatus principes elaboraret, ridens fabulas voluptua ne qui, patrioque persequeris efficiantur et nam. Ius audire offendit ne. Pri posse deserunt ad, ius ex minim omittam petentium.\n" +
            "\n" + "Ne has erat aliquando inciderint, cetero placerat ex eos, errem eleifend eam ne. Vel petentium omittantur ex, splendide assentior cu has. Dicat ludus quo ex, no quo amet oratio dissentias. Qui cu tollit integre, omnis erant complectitur eum ut. Graeci integre et quo, vim ut detracto euripidis.\n" +
            "\n" + "Ex facer vitae maiorum est. Habeo splendide moderatius duo id. Quo in viris euripidis. Qui graeci recteque accusamus in, ut qui diam sint, saperet mentitum vel ut.\n" +
            "\n" + "Duo modus nihil eripuit eu, eu malorum labitur consequat mea, veri omnesque patrioque at mea. Vis timeam patrioque id. Nam putant aperiri ad, at assum delectus mea, sonet nominavi periculis sea an. Sit admodum apeirian ex, congue tritani repudiare ei ius. Virtute omittam mea ex, hinc consul cu his, ea dicit homero principes quo. Dicant consul gubergren quo no, ea has sonet salutandi.\n" +
            "\n" + "Ad mea libris blandit adversarium, ignota inermis instructior ei mei, est no causae alienum verterem. An ius dico nobis feugait, scaevola recteque consequat eu cum. Pro ei sale appetere facilisis. Detracto indoctum te quo, discere dolorum impedit no sed. Sea aperiri honestatis ad, eos tale prompta dolorum no.";
    textarea.setText(temp + temp);
  }

  private View.OnTouchListener touchEvent = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      switch (motionEvent.getAction()) {
        case MotionEvent.ACTION_DOWN: {
          countDownTimer.cancel();
          scrollVelocity = 0;
          return true;
        }
        default:
          return false;
      }
    }
  };

  public void onClick(View v) {
    View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
    Log.i(TAG, String.valueOf(scrollVelocity));
    if (view.getBottom() == (scrollView.getHeight() + scrollView.getScrollY())) {
      scrollVelocity = 0;
    }
    if (view.getTop() == scrollView.getScrollY()) {
      scrollVelocity = 0;
    }
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }

    switch (v.getId()) {
      case R.id.buttonUp:
        scrollVelocity += 3;
        break;
      case R.id.buttonDown:
        scrollVelocity -= 3;
        break;
      default:
        break;
    }

    if (scrollVelocity == 0) {
      return;
    }
    countDownTimer = new CountDownTimer(10000 / Math.abs(scrollVelocity), 20) {
      View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
      int beginScrollY = scrollView.getScrollY();

      public void onTick(long millisUntilFinished) {
        if (scrollVelocity < 0) {
          scrollView.scrollTo(0, (int) (beginScrollY
                  + (1000 - 1000 * millisUntilFinished / (10000 / Math.abs(scrollVelocity)))));
          if (view.getBottom() == (scrollView.getHeight() + scrollView.getScrollY())) {
            Log.d(TAG, "Bottom reached before end");
            this.cancel();
          }
        } else if (scrollVelocity > 0) {
          scrollView.scrollTo(0, (int) (beginScrollY
                  - (1000 - 1000 * millisUntilFinished / (10000 / Math.abs(scrollVelocity)))));
          if (view.getTop() == scrollView.getScrollY()) {
            Log.d(TAG, "Top reached before end");
            this.cancel();
          }
        }
      }

      public void onFinish() {
        if (scrollVelocity < 0) {
          if (view.getBottom() != (scrollView.getHeight() + scrollView.getScrollY())) {
            Log.d(TAG, "Bottom not reached");
            beginScrollY = scrollView.getScrollY();
            this.start();
          }
        } else if (scrollVelocity > 0) {
          if (view.getTop() != scrollView.getScrollY()) {
            Log.d(TAG, "Top not reached");
            beginScrollY = scrollView.getScrollY();
            this.start();
          }
        }
      }
    }.start();
  }


  /**
   * Overrides a method of SurfaceHolder interface.
   * Sets up the camera source, requests user permission, and
   * then sets the GraphicOverlay class according to camera info.
   *
   * @param holder the SurfaceHolder class that called this
   */
  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    Context context = getApplicationContext();

    FaceDetector detector = new FaceDetector.Builder(context)
        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
        .build();
    detector.setProcessor(new LargestFaceFocusingProcessor.Builder(detector,
        new GraphicFaceTracker(graphicOverlay)).build());
    // create a camera source
    cameraSource = new CameraSource.Builder(context, detector)
        .setRequestedPreviewSize(640, 480)
        .setFacing(CameraSource.CAMERA_FACING_FRONT)
        .setRequestedFps(30.0f)
        .build();

    // check for permission status and request for permission
    if (cameraSource != null) {
      if (ActivityCompat.checkSelfPermission(this,
          android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.CAMERA)) {
          // show request alert if camera access needs to show a message
          new AlertDialog.Builder(this)
              .setTitle("Request Permission for Camera")
              .setMessage("App Requires Camera Access")
              .setNegativeButton(R.string.deny, null)
              .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  ActivityCompat.requestPermissions(AutoscrollActivity.this,
                      new String[] {Manifest.permission.CAMERA},
                      MY_PERMISSIONS_USE_CAMERA);
                }
              })
              .create()
              .show();
        } else {
          ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
              MY_PERMISSIONS_USE_CAMERA);
        }
        return;
      }

      // If explicit permission request is unnecessary, proceed with using camera.
      try {
        cameraSource.start(cameraview.getHolder());
        Size size = cameraSource.getPreviewSize();
        int min = Math.min(size.getWidth(), size.getHeight());
        int max = Math.max(size.getWidth(), size.getHeight());

        // sets the camera for graphic overlays
        graphicOverlay.setCameraInfo(min, max, cameraSource.getCameraFacing());
      } catch (IOException ioe) {
        Log.e(TAG, "Unable to start camera source.", ioe);
        cameraSource.release();
      }
    }
  }

  /**
   * A callback function after requestPermissions() method.
   * This checks whether the proper permission was granted, and proceeds with
   * using camera.
   *
   * @param requestCode code number for resource request
   * @param permissions requested permissions registered in manifest
   * @param grantResults result of permission grants
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                        @NonNull int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_USE_CAMERA: {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
          try {
            cameraSource.start(cameraview.getHolder());
            Size size = cameraSource.getPreviewSize();
            int min = Math.min(size.getWidth(), size.getHeight());
            int max = Math.max(size.getWidth(), size.getHeight());

            // sets the camera for graphic overlays
            graphicOverlay.setCameraInfo(min, max, cameraSource.getCameraFacing());
          } catch (IOException ioe) {
            Log.e(TAG, "Unable to start camera source.", ioe);
            cameraSource.release();
          }
        } else {
          Toast.makeText(this, "CAMERA Permission Denied", Toast.LENGTH_SHORT).show();
        }
        break;
      }
      default:
        Log.e(TAG, "Granted Access to Non-requested Resource");
        break;
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder,
                             int format, int width, int height) {
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
  }


  //==============================================================================================
  // Graphic Face Tracker
  //==============================================================================================

  /**
   * Face tracker for the detected individual. This maintains a face graphic within the app's
   * associated face overlay.
   */
  private class GraphicFaceTracker extends Tracker<Face> {
    private GraphicOverlay graphicOverlay;
    private FaceGraphic faceGraphic;

    GraphicFaceTracker(GraphicOverlay overlay) {
      graphicOverlay = overlay;
      faceGraphic = new FaceGraphic(overlay);
    }

    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(int faceId, Face item) {
      faceGraphic.setId(faceId);
    }

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
      graphicOverlay.add(faceGraphic);
      faceGraphic.updateFace(face);
    }

    /**
     * Don't do anything when the face doesn't seem to be there only temporarily.
     * The application needs to keep track of the face until it is sure that
     * it's gone.
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
    }

    /**
     * Called when the face is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone() {
      graphicOverlay.remove(faceGraphic);
    }
  }
}
