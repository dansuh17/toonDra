package edu.kaist.mskers.toondra;

import static edu.kaist.mskers.toondra.navermodule.webtoon.NaverWebtoonUrl.getWebtoonDetailUrl;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;

/**
 * Created by harrykim on 2016. 10. 9..
 */

public class Thumbnail extends FrameLayout {
  public LinearLayout thumbLayout;
  public ImageView thumbImage;
  public TextView thumbName;
  public String firstEpisodeUrl;

  public Thumbnail(Context context) {
    super(context);
  }

  public Thumbnail(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Initialize the thumbnail view.
   */
  public void initView(NaverToonInfo info) {
    thumbLayout = (LinearLayout) inflate(getContext(), R.layout.thumbnail_custom, null );
    addView(thumbLayout);

    thumbImage = (ImageView) thumbLayout.findViewById(R.id.thumb_image);
    thumbName = (TextView) thumbLayout.findViewById(R.id.thumb_text);
    setThumbImage(info.getthumbnail());
    setThumbName(info.getTitleName());
    setFirstEpisodeUrl(getWebtoonDetailUrl(info.getTitleId(), 1));
  }

  /**
   * Set the thumbnail view with given bitmap.
   */
  private void setThumbImage(Bitmap thumbBimtmap) {
    thumbImage.setImageBitmap(thumbBimtmap);
  }

  /**
   * Set the thumbnail name.
   */
  private void setThumbName(String name) {
    Log.d("toon name", name);
    thumbName.setText(name);
  }

  /**
   * Set the thumbnail name.
   */
  public void setFirstEpisodeUrl(String url) {
    firstEpisodeUrl = url;
  }

  /**
   *Get the first episode url of the webtoon of thumbnail.
   */
  public String getFirstEpisodeUrl() {
    return firstEpisodeUrl;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return false;
  }
}
