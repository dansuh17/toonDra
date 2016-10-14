package edu.kaist.mskers.toondra;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by harrykim on 2016. 10. 14..
 */

public class ReadEpisodePage extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.episode_read_page);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getSupportActionBar().hide();
    findViewById(R.id.eye_screen_center).setVisibility(View.INVISIBLE);
    LinearLayout readLinear = (LinearLayout) findViewById(R.id.readLinear);

    for (int i = 0; i < 3; i++) {
      ImageView imageView = new ImageView(this);
      int res = getResources().getIdentifier("s"+i, "drawable", getPackageName());

      BitmapFactory.Options options = new BitmapFactory.Options();
      Bitmap bitmap = BitmapFactory.decodeResource(getResources(), res, options);
      imageView.setImageBitmap(bitmap);
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
      imageView.setLayoutParams(layoutParams);
      imageView.setAdjustViewBounds(true);
      imageView.setScaleType(ImageView.ScaleType.FIT_START);


      readLinear.addView(imageView);

    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getSupportActionBar().show();
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            getSupportActionBar().hide();
          }
        }, 1000);
      }
    };
    readLinear.setOnClickListener(mOnClickListener);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.sample_eye_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    if (id == R.id.read_eye) {
      findViewById(R.id.eye_screen_center).setVisibility(View.VISIBLE);
      Handler mHandler = new Handler();
      mHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          findViewById(R.id.eye_screen_center).setVisibility(View.INVISIBLE);
        }
      }, 1000);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
