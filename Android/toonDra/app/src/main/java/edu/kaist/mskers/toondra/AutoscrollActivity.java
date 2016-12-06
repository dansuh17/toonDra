package edu.kaist.mskers.toondra;

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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Class AutoscrollActivity
 * This class uses the front facing camera to detect face.
 */
public class AutoscrollActivity extends AppCompatActivity {
  private static final String TAG = "FaceTracker";
  private static final int MY_PERMISSIONS_USE_CAMERA = 1;
  private static final long MILLIS_IN_FUTURE = 10000;
  private static final long COUNTDOWN_INTERVAL = 20;
  private static final int SCROLL_AMOUNT = 1000;
  private static final int BLINK_SCROLL_UP = 0;
  private static final int BLINK_SCROLL_DOWN = 1;
  enum BlinkType {
    None, Right, Left, Both;
  }
  private BlinkType blinkEye = BlinkType.None;
  private CameraSource cameraSource;
  private CountDownTimer countDownTimer;
  private int scrollVelocity = 0;
  private ScrollHandler scrollHandler = null;
  private Toast scrollToast = null;

  private ToggleButton toggleButton;
  private ScrollView scrollView;
  private TextView textArea;
  /**
   * Prepares resources such as camera view, text view, and graphic overlay.
   * @param savedInstanceState current context
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scrolldebug_layout);
    textArea = (TextView) findViewById(R.id.textView);
    scrollView = (ScrollView) findViewById(R.id.scrollView);
    scrollView.setOnTouchListener(touchEvent);
    toggleButton = (ToggleButton) findViewById(R.id.toggleButton);




    String temp = "Lorem ipsum dolor sit amet, vel minimum conceptam constituto eu, cum no iusto doming molestie, te qui vero putent labitur. Justo facete pri no. Labitur dignissim reprimique ne nam, id sit amet interpretaris. Has at ornatus principes elaboraret, ridens fabulas voluptua ne qui, patrioque persequeris efficiantur et nam. Ius audire offendit ne. Pri posse deserunt ad, ius ex minim omittam petentium.\n" +
            "\n" + "Ne has erat aliquando inciderint, cetero placerat ex eos, errem eleifend eam ne. Vel petentium omittantur ex, splendide assentior cu has. Dicat ludus quo ex, no quo amet oratio dissentias. Qui cu tollit integre, omnis erant complectitur eum ut. Graeci integre et quo, vim ut detracto euripidis.\n" +
            "\n" + "Ex facer vitae maiorum est. Habeo splendide moderatius duo id. Quo in viris euripidis. Qui graeci recteque accusamus in, ut qui diam sint, saperet mentitum vel ut.\n" +
            "\n" + "Duo modus nihil eripuit eu, eu malorum labitur consequat mea, veri omnesque patrioque at mea. Vis timeam patrioque id. Nam putant aperiri ad, at assum delectus mea, sonet nominavi periculis sea an. Sit admodum apeirian ex, congue tritani repudiare ei ius. Virtute omittam mea ex, hinc consul cu his, ea dicit homero principes quo. Dicant consul gubergren quo no, ea has sonet salutandi.\n" +
            "\n" + "Ad mea libris blandit adversarium, ignota inermis instructior ei mei, est no causae alienum verterem. An ius dico nobis feugait, scaevola recteque consequat eu cum. Pro ei sale appetere facilisis. Detracto indoctum te quo, discere dolorum impedit no sed. Sea aperiri honestatis ad, eos tale prompta dolorum no.";
    textArea.setText(temp + temp);
    setFaceDetectScroll();
  }

  private void setFaceDetectScroll() {
    // create a handler for scrolling
    if (scrollHandler == null) {
      scrollHandler = new ScrollHandler(this);
    } else {
      scrollHandler.setTarget(this);
    }

    Context context = getApplicationContext();

    FaceDetector detector = new FaceDetector.Builder(context)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build();
    detector.setProcessor(new LargestFaceFocusingProcessor.Builder(detector,
            new GraphicFaceTracker()).build());
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
        cameraSource.start();

      } catch (IOException ioe) {
        Log.e(TAG, "Unable to start camera source.", ioe);
        cameraSource.release();
      }
    }
  }

  /**
   * Set the scroll velocity to a specific value.
   * @param vel velocity value
   */
  private void setScrollVelocity(int vel) {
    scrollVelocity = vel;
  }

  /**
   * Add amount to scroll velocity.
   * @param amount amount to add to.
   */
  public void addVelocity(int amount) {
    scrollVelocity += amount;
    Log.i(TAG, "ScrollVelocity :" +String.valueOf(scrollVelocity));
  }

  /**
   * Sets a new timer that allows scrolling according to the scroll velocity.
   */
  private void setNewTimer() {
    // remove any other timer set previously
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }

    // prevent divide by zero
    if (scrollVelocity == 0) {
      return;
    }
    if (scrollToast != null) {
      scrollToast.cancel();
    }
    scrollToast = Toast.makeText(this, "Scroll speed: " + String.valueOf(scrollVelocity), Toast.LENGTH_SHORT);
    scrollToast.show();
    countDownTimer = new CountDownTimer(MILLIS_IN_FUTURE / Math.abs(scrollVelocity),
        COUNTDOWN_INTERVAL) {
      View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
      int beginScrollY = scrollView.getScrollY();

      /**
       * On every time tick, move the scroller. Check if the scroller reached the boundary.
       * @param millisUntilFinished The amount of time until finished.
       */
      public void onTick(long millisUntilFinished) {
        if (scrollVelocity < 0) {
          scrollView.scrollTo(0, (int) (beginScrollY
              + (SCROLL_AMOUNT - SCROLL_AMOUNT
              * millisUntilFinished / (MILLIS_IN_FUTURE / Math.abs(scrollVelocity)))));
          if (view.getBottom() == (scrollView.getHeight() + scrollView.getScrollY())) {
            Log.d(TAG, "Bottom reached before end");
            setScrollVelocity(0);
            this.cancel();
          }
        } else if (scrollVelocity > 0) {
          scrollView.scrollTo(0, (int) (beginScrollY
              - (SCROLL_AMOUNT - SCROLL_AMOUNT
              * millisUntilFinished / (MILLIS_IN_FUTURE / Math.abs(scrollVelocity)))));
          if (view.getTop() == scrollView.getScrollY()) {
            Log.d(TAG, "Top reached before end");
            setScrollVelocity(0);
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
        } else {
        }
      }
    }.start();
  }

  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                        @NonNull int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_USE_CAMERA: {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
          try {
            cameraSource.start();
            Log.i(TAG, "Camera??");

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


  //==============================================================================================
  // Graphic Face Tracker
  //==============================================================================================

  /**
   * Face tracker for the detected individual. This maintains a face graphic within the app's
   * associated face overlay.
   */
  private class GraphicFaceTracker extends Tracker<Face> {

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
      switch (toggleButton.isChecked() ? 1 : 0) {
        case 0: //Euler
          break;
        case 1: //Wink & Blink
          final float blinkThresh = 0.40f;  // the threshold of 'blinking'
          // get the screen's view - for getting screen's dimensions
          View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
          // change velocity according to action
          float leftOpen = face.getIsLeftEyeOpenProbability();
          float rightOpen = face.getIsRightEyeOpenProbability();
          if (leftOpen > blinkThresh && rightOpen < blinkThresh) {
            // right blink
            // must modify UI elements through handler because FaceTracker is on a different Thread,
            // and therefore prohibited to modify UI elements.
            if (blinkEye != BlinkType.Right) {
              blinkEye = BlinkType.Right;
              Message msg = new Message();
              msg.what = BLINK_SCROLL_DOWN;
              scrollHandler.sendMessage(msg);
            }
          } else if (leftOpen < blinkThresh && rightOpen > blinkThresh) {
            // left blink
            if (blinkEye != BlinkType.Left) {
              blinkEye = BlinkType.Left;
              Message msg = new Message();
              msg.what = BLINK_SCROLL_UP;
              scrollHandler.sendMessage(msg);
            }
          } else if (leftOpen < blinkThresh && rightOpen < blinkThresh) {
            // both blink
            if (blinkEye != BlinkType.Both) {
              blinkEye = BlinkType.Both;
              setScrollVelocity(0);
            }
          } else {
            blinkEye = BlinkType.None;
          }
          break;
        default:
          break;
      }
    }
  }

  /**
   * The Handler class that controls the scrolling activity.
   * Create a static Handler class since using a normal Handler may cause memory leak.
   * This is because the view objects will not be garbage collected even if the Handler is removed.
   * To access the methods within the Activity class, create a weak reference to the Activity.
   */
  private static class ScrollHandler extends Handler {
    private WeakReference<AutoscrollActivity> mainActivityWeakReference;

    ScrollHandler(AutoscrollActivity target) {
      mainActivityWeakReference = new WeakReference<>(target);
    }

    public void setTarget(AutoscrollActivity target) {
      mainActivityWeakReference.clear();
      mainActivityWeakReference = new WeakReference<>(target);
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      final AutoscrollActivity activity = mainActivityWeakReference.get();
      Log.i(TAG, "handleMessage");

      switch (msg.what) {
        case BLINK_SCROLL_UP:
          activity.addVelocity(3);
          break;
        case BLINK_SCROLL_DOWN:
          activity.addVelocity(-3);
          break;
        default:
          break;
      }

      activity.setNewTimer();
    }
  }

  private View.OnTouchListener touchEvent = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      switch (motionEvent.getAction()) {
        case MotionEvent.ACTION_DOWN: {
          if (countDownTimer != null) {
            countDownTimer.cancel();
            scrollVelocity = 0;
          }
          return true;
        }
        default:
          return false;
      }
    }
  };
}
