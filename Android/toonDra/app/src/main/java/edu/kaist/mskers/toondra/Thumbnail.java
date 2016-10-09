package edu.kaist.mskers.toondra;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

/**
 * Created by harrykim on 2016. 10. 9..
 */

public class Thumbnail extends LinearLayout {
  ImageView thumbImage;
  TextView thumbName;

  public Thumbnail(Context context) {
    super(context);
  }
  public Thumbnail(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void initView(String thumbUrl, String name) {
    LinearLayout.LayoutParams params = new
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT);
    super.setOrientation(VERTICAL);
    super.setLayoutParams(params);

    setThumbImage(thumbUrl);
    setThumbName(name);
  }

  private void setThumbImage(String thumbUrl) {

    try {
      URL url = new URL(thumbUrl);
      Log.e("thumbUrl:", thumbUrl);
      Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
      thumbImage = new ImageView(getContext());
      thumbImage.setImageBitmap(bitmap);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
          (LinearLayout.LayoutParams.WRAP_CONTENT,
              LinearLayout.LayoutParams.WRAP_CONTENT);
      thumbImage.setLayoutParams(params);
      App mApp = (App)getContext().getApplicationContext();
      int pixels = mApp.dpToPixel(120);
      thumbImage.getLayoutParams().height = pixels;
      thumbImage.getLayoutParams().width = pixels;
      super.addView(thumbImage);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void setThumbName(String name) {
    Log.d("toon name", name);
    thumbName = new TextView(getContext());
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
        (LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
    params.gravity = Gravity.CENTER;

    thumbName.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
    thumbName.setTextColor(getResources().getColor(android.R.color.black));
    thumbName.setText(name);
    thumbName.setLayoutParams(params);
    super.addView(thumbName);
  }
}
