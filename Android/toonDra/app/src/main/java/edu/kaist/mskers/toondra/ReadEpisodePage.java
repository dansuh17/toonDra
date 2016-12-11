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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import edu.kaist.mskers.toondra.navermodule.webtoon.NaverWebtoonCrawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class ReadEpisodePage extends AppCompatActivity implements ScrollViewListener  {

  private BottomNavigationView bottomNavigationView = null;
  private String read_url = null;
  private int latest_id = 0;
  private int episode_id = 0;
  private LinearLayout readLinear = null;
  private boolean isAutoScroll = false;
  private static final String TAG = "FaceTracker";
  private static final int MY_PERMISSIONS_USE_CAMERA = 1;
  private static final long MILLIS_IN_FUTURE = 10000;
  private static final long COUNTDOWN_INTERVAL = 20;
  private static final long TIMER_DELAY = 250;
  private static final int SCROLL_AMOUNT = 1000;
  private static final int BLINK_SCROLL_UP = 0;
  private static final int BLINK_SCROLL_DOWN = 1;
  private static final int NEXT_EPISODE = 2;
  private static final int PREVIOUS_EPISODE = 3;
  private static final int SCROLL_PAUSE = 4;

  enum BlinkType {
    None, Right, Left, Both
  }

  private ReadEpisodePage.BlinkType blinkEye = ReadEpisodePage.BlinkType.None;
  private CameraSource cameraSource;
  private CountDownTimer countDownTimer;
  private Handler actionHandler;
  private Runnable actionRunnable;
  private boolean actionPerfomed = false;
  private int scrollVelocity = 0;
  private ReadEpisodePage.ScrollHandler scrollHandler = null;
  private Toast scrollToast = null;
  private ScrollViewExt readScroll;
  private SurfaceView cameraPreview;
  private boolean cameraStatus = false;
  private Thread pageThread = null;
  private Timer timer = null;
  private TimerWakeupTask timerWakeupTask = null;
  private boolean timerIsSet;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.episode_read_page);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    Toolbar toolbar = (Toolbar) findViewById(R.id.read_toolbar);
    toolbar.setTitle("");
    setSupportActionBar(toolbar);
    getSupportActionBar().setHomeButtonEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().hide();
    findViewById(R.id.eye_screen_center).setVisibility(View.INVISIBLE);

    cameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
    readLinear = (LinearLayout) findViewById(R.id.readLinear);
    read_url = getIntent().getStringExtra("read_url");
    latest_id = getIntent().getIntExtra("latest_id", 0);
    Log.e("latest_id", "latest_id: " + latest_id);
    episode_id = getIntent().getIntExtra("episode_id", 0);
    Log.e("start_id", "start_id: " + episode_id);
    Log.d("readUrl", getIntent().getStringExtra("read_url"));
    readEpisode(read_url + episode_id);
    readScroll = (ScrollViewExt) findViewById(R.id.readScroll);
    readScroll.setScrollViewListener(this);
    if (scrollHandler == null) {
      scrollHandler = new ReadEpisodePage.ScrollHandler(this);
    } else {
      scrollHandler.setTarget(this);
    }
    setFaceDetectScroll();
    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
          case MotionEvent.ACTION_DOWN: {
            if (countDownTimer != null) {
              Log.i("TouchEvent","CountDown canceled");
              countDownTimer.cancel();
              scrollVelocity = 0;
            }
            return false;
          }
          default:
            return false;
        }
      }
    };
    readLinear.setOnTouchListener(onTouchListener);
    View.OnClickListener onClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getSupportActionBar().show();
        bottomNavigationView.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            getSupportActionBar().hide();
            bottomNavigationView.setVisibility(View.INVISIBLE);
          }
        }, 2000);
      }
    };
    readLinear.setOnClickListener(onClickListener);

    bottomNavigationView = (BottomNavigationView)
        findViewById(R.id.bottom_navigation);
    bottomNavigationView.setVisibility(View.INVISIBLE);

    // resize the text on the bottom
    setBigTitle(episode_id);

    bottomNavigationView.setOnNavigationItemSelectedListener(
        new BottomNavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Log.e("current episode id: ", ""+episode_id);
            switch (item.getItemId()) {
              case R.id.prev_episode:
                gotoPreviousEpisode();
                break;
              case R.id.bottom_title_text:
                break;
              case R.id.next_episode:
                gotoNextEpisode();
                break;
            }
            return false;
          }
        });

    // initialize timer
    timerIsSet = false;
  }
  @Override
  protected void onPause() {
    super.onPause();
    if (cameraSource != null) {
      cameraSource.release();
    }
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    setFaceDetectScroll();
  }

  private void gotoPreviousEpisode() {
    if (episode_id - 1 <= 0) {
      Log.e("read_episode minus", "id: " + episode_id);
      Toast toast =  Toast.makeText(getApplicationContext(),
              "이전화가 존재하지 않습니다.", Toast.LENGTH_LONG);
      toast.show();
    } else {
      episode_id -= 1;
      setBigTitle(episode_id);
      if (pageThread != null) {
        try {
          pageThread.interrupt();
          pageThread.join();
          //Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      readEpisode(read_url + episode_id);
    }
  }

  private void gotoNextEpisode() {
    if (latest_id == episode_id) {
      Log.e("read_episode same", "latest id: " + latest_id + " episode_id: " + episode_id);
      Toast toast =  Toast.makeText(getApplicationContext(),
              "현재 페이지가 가장 최신화입니다.", Toast.LENGTH_LONG);
      toast.show();
    } else {
      episode_id += 1;
      setBigTitle(episode_id);
      if (pageThread != null) {
        try {
          pageThread.interrupt();
          pageThread.join();
          //Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      readEpisode(read_url + episode_id);
    }
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.read_main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.read_eye) {
      ImageView eye_image = (ImageView)findViewById(R.id.eye_screen_center);
      if (!isAutoScroll) {
        isAutoScroll = true;
        item.setIcon(R.drawable.eye_off_menu);
        eye_image.setImageResource(R.drawable.eye_temp);
        eye_image.setVisibility(View.VISIBLE);
      } else {
        isAutoScroll = false;
        item.setIcon(R.drawable.eye_menu);
        eye_image.setImageResource(R.drawable.eye_off_img);
        eye_image.setVisibility(View.VISIBLE);
      }
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          findViewById(R.id.eye_screen_center).setVisibility(View.INVISIBLE);
        }
      }, 1000);
      hideMenus();
      return true;
    } else if (id == android.R.id.home) {
      finish();
    } else if (id == R.id.camera_enable) {
      if (!cameraStatus) {
        cameraStatus = true;
        cameraPreview.setVisibility(cameraPreview.VISIBLE);
        cameraSource.release();
        setFaceDetectScroll();
      } else {
        cameraStatus = false;
        cameraPreview.setVisibility(cameraPreview.INVISIBLE);
        cameraSource.release();
        setFaceDetectScroll();
      }
      hideMenus();
    }

    return super.onOptionsItemSelected(item);
  }

  private void readEpisode(final String firstEpisodeUrl) {
    readLinear.removeAllViews();
    pageThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Element wtPage;
          wtPage = Jsoup.connect(firstEpisodeUrl)
              .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0)"
                  + "Gecko/20100101 Firefox/23.0")
              .get();
          Element wtViewer = wtPage.getElementsByClass("wt_viewer").first();

          if (wtViewer == null) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast toast =  Toast.makeText(getApplicationContext(),
                    "19세 웹툰 이용을 위해서는 성인 인증이 필요합니다.",
                    Toast.LENGTH_LONG);
                toast.show();
              }
            });
            return;
          }

          for (Element imgLink : wtViewer.children()) {
            if (!Thread.currentThread().isInterrupted()) {
              Log.d("ThumbImgLink", imgLink.toString());
              String imgUrl = imgLink.absUrl("src");

              // Check whether imgURL is a valid image file
              if (imgUrl == null || !imgUrl.endsWith(".jpg")) {
                continue;
              }

              Connection.Response wtRes;
              wtRes = Jsoup.connect(imgUrl)
                  .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0)"
                      + "Gecko/20100101 Firefox/23.0")
                  .referrer(firstEpisodeUrl)
                  .ignoreContentType(true)
                  .maxBodySize(NaverWebtoonCrawler.MAX_DOWNLOAD_SIZE_BYTES)
                  .execute();

              byte[] bitmapData = wtRes.bodyAsBytes();

              BitmapFactory.Options options = new BitmapFactory.Options();
              ;
              options.inSampleSize = 2;
              Bitmap bitmap = BitmapFactory
                  .decodeByteArray(bitmapData, 0, bitmapData.length, options);

              final ImageView pageImage = (ImageView) getLayoutInflater().inflate(R.layout.page_custom, null);

              pageImage.setImageBitmap(bitmap);
              Runnable uiRunnable = new Runnable() {
                @Override
                public void run() {
                  readLinear.addView(pageImage);
                  synchronized ( this )
                  {
                    this.notify();
                  }
                }
              };
              synchronized (uiRunnable) {
                runOnUiThread(uiRunnable);
                Log.e("wait for uiRunnable", "dd");
                uiRunnable.wait();
              }
            } else {
              Log.e("Interruption: ", Thread.currentThread().toString());
              break;
            }
          }

        } catch (IOException ex) {
          Log.e("IOExcetipn: ", ex.toString());
          ex.printStackTrace();
        } catch (InterruptedException ex) {
          Log.e("Interruption: ", ex.toString());
          ex.printStackTrace();
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            Log.e("Interruption Again: ", e.toString());
            e.printStackTrace();
          }
        }

      }
    });
    pageThread.start();
  }

  @Override
  public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
    // We take the last son in the scrollview
    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

    // if diff is zero, then the bottom has been reached
    if (diff == 0) {
      bottomNavigationView.setVisibility(View.VISIBLE);
      getSupportActionBar().show();
    } else {
      hideMenus();
    }
  }

  public void hideMenus() {
    if (bottomNavigationView.getVisibility() == View.VISIBLE) {
      bottomNavigationView.setVisibility(View.INVISIBLE);
    }
    if (getSupportActionBar().isShowing()) {
      getSupportActionBar().hide();
    }
  }

  public void setBigTitle(int titleId) {
    SpannableString spanString = new SpannableString(String.valueOf(titleId));
    int end = spanString.length();
    spanString.setSpan(new RelativeSizeSpan(2.0f), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    BottomNavigationItemView titleItem = (BottomNavigationItemView) findViewById(R.id.bottom_title_text);
    titleItem.setTitle(spanString);
  }

  private void setFaceDetectScroll() {
    // create a handler for scrolling
    Context context = getApplicationContext();
    FaceDetector detector = new FaceDetector.Builder(context)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setMode(FaceDetector.ACCURATE_MODE).build();
    detector.setProcessor(new LargestFaceFocusingProcessor.Builder(detector,
            new ReadEpisodePage.GraphicFaceTracker()).build());
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
                      ActivityCompat.requestPermissions(ReadEpisodePage.this,
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
        if (cameraStatus) {
          cameraSource.start(cameraPreview.getHolder());
        }
        else {
          cameraSource.start();
        }

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
  private void setActivityTimer() {
    actionPerfomed = true;
    actionRunnable = new Runnable() {
      @Override
      public void run() {
        actionPerfomed = false;
      }
    };

    actionHandler = new Handler();
    actionHandler.postDelayed(actionRunnable, 1000);
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
      View view = (View) readScroll.getChildAt(readScroll.getChildCount() - 1);
      int beginScrollY = readScroll.getScrollY();

      /**
       * On every time tick, move the scroller. Check if the scroller reached the boundary.
       * @param millisUntilFinished The amount of time until finished.
       */
      public void onTick(long millisUntilFinished) {
        if (scrollVelocity < 0) {
          readScroll.scrollTo(0, (int) (beginScrollY
                  + (SCROLL_AMOUNT - SCROLL_AMOUNT
                  * millisUntilFinished / (MILLIS_IN_FUTURE / Math.abs(scrollVelocity)))));
          if (view.getBottom() == (readScroll.getHeight() + readScroll.getScrollY())) {
            Log.d(TAG, "Bottom reached before end");
            setScrollVelocity(0);
            this.cancel();
          }
        } else if (scrollVelocity > 0) {
          readScroll.scrollTo(0, (int) (beginScrollY
                  - (SCROLL_AMOUNT - SCROLL_AMOUNT
                  * millisUntilFinished / (MILLIS_IN_FUTURE / Math.abs(scrollVelocity)))));
          if (view.getTop() == readScroll.getScrollY()) {
            Log.d(TAG, "Top reached before end");
            setScrollVelocity(0);
            this.cancel();
          }
        }
      }

      public void onFinish() {
        if (scrollVelocity < 0) {
          if (view.getBottom() != (readScroll.getHeight() + readScroll.getScrollY())) {
            Log.d(TAG, "Bottom not reached");
            beginScrollY = readScroll.getScrollY();
            this.start();
          }
        } else if (scrollVelocity > 0) {
          if (view.getTop() != readScroll.getScrollY()) {
            Log.d(TAG, "Top not reached");
            beginScrollY = readScroll.getScrollY();
            this.start();
          }
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
      switch (isAutoScroll ? 1 : 0) {
        case 0: //Euler
          break;
        case 1: //Wink & Blink
          float eulerY = face.getEulerY();
          final float blinkThresh = 0.30f;  // the threshold of 'blinking'
          final int eulerYThresh = 30;
          float leftOpen = face.getIsLeftEyeOpenProbability();
          float rightOpen = face.getIsRightEyeOpenProbability();

          // Actions depending on the detected face parameters
          if (eulerY > eulerYThresh) {  // Left tilt
            Log.i("Euler", String.valueOf(eulerY));
            Message msg = new Message();
            msg.what = PREVIOUS_EPISODE;
            scrollHandler.sendMessage(msg);
          }
          else if (eulerY < -eulerYThresh) {  // Right tilt
            Log.i("Euler", String.valueOf(eulerY));
            Message msg = new Message();
            msg.what = NEXT_EPISODE;
            scrollHandler.sendMessage(msg);
          } else if (leftOpen > blinkThresh && rightOpen < blinkThresh) {  // right blink
            actionTimer(BlinkType.Right);
          } else if (leftOpen < blinkThresh && rightOpen > blinkThresh) {  // left blink
            actionTimer(BlinkType.Left);
          } else if (leftOpen < blinkThresh && rightOpen < blinkThresh) {  // both blink
            actionTimer(BlinkType.Both);
          } else {  // both open
            blinkEye = ReadEpisodePage.BlinkType.None;
            timerIsSet = false;
          }
          break;
        default:
          break;
      }
    }

    // gives a timer buffer - the user has to be still performing the action
    // for a given amount of time
    void actionTimer(BlinkType blinkType) {
      // schedule new task only when the already scheduled task is different from new one,
      // or when the timer is off.
      if (timerIsSet) {
        if (!timerWakeupTask.isActionEqual(blinkType)) {
          timer = new Timer();
          timerWakeupTask = new TimerWakeupTask(blinkType);
          timer.schedule(timerWakeupTask, TIMER_DELAY);
        }
      } else {
        timer = new Timer();
        timerWakeupTask = new TimerWakeupTask(blinkType);
        timer.schedule(timerWakeupTask, TIMER_DELAY);
        timerIsSet = true;
      }
    }
  }

  /**
   * TimeTask extension to send the message handler to scroll / move episodes
   * according to the blink type after the Timer wakes up.
   */
  private class TimerWakeupTask extends TimerTask {
    private BlinkType blinkType;

    TimerWakeupTask(BlinkType type) {
      blinkType = type;
    }

    public void run() {
      Log.e("WakeupTask", blinkType.toString());
      Message msg = new Message();

      // determine the message type based on the blink type
      switch (blinkType) {
        case Right:
          msg.what = BLINK_SCROLL_DOWN;
          break;
        case Left:
          msg.what = BLINK_SCROLL_UP;
          break;
        case Both:
          msg.what = SCROLL_PAUSE;
          break;
        case None:
          timerIsSet = false;
          return;
        default:
          break;
      }
      scrollHandler.sendMessage(msg);
      timerIsSet = false;
    }

    // Determines whether the scheduled task is equal to requested task
    boolean isActionEqual(BlinkType type) {
      return blinkType == type;
    }
  }

  /**
   * The Handler class that controls the scrolling activity.
   * Create a static Handler class since using a normal Handler may cause memory leak.
   * This is because the view objects will not be garbage collected even if the Handler is removed.
   * To access the methods within the Activity class, create a weak reference to the Activity.
   */
  private static class ScrollHandler extends Handler {
    private WeakReference<ReadEpisodePage> mainActivityWeakReference;

    ScrollHandler(ReadEpisodePage target) {
      mainActivityWeakReference = new WeakReference<>(target);
    }

    void setTarget(ReadEpisodePage target) {
      mainActivityWeakReference.clear();
      mainActivityWeakReference = new WeakReference<>(target);
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      final ReadEpisodePage activity = mainActivityWeakReference.get();
      Log.i(TAG, String.valueOf(activity.actionPerfomed));
      if (!activity.actionPerfomed) {
        switch (msg.what) {
          case BLINK_SCROLL_UP:
            activity.addVelocity(6);
            activity.setNewTimer();
            break;
          case BLINK_SCROLL_DOWN:
            activity.addVelocity(-6);
            activity.setNewTimer();
            break;
          case PREVIOUS_EPISODE:
            activity.gotoPreviousEpisode();
            break;
          case NEXT_EPISODE:
            activity.gotoNextEpisode();
            break;
          case SCROLL_PAUSE:
            activity.setScrollVelocity(0);
            break;
          default:
            break;
        }
        activity.setActivityTimer();
      }
    }
  }
}
