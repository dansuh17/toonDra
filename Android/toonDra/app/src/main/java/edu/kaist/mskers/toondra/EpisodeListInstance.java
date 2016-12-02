package edu.kaist.mskers.toondra;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by harrykim on 2016. 12. 3..
 */

public class EpisodeListInstance extends FrameLayout {
  public LinearLayout episodeLayout;
  public ImageView episodeImage;
  public TextView episodeName;
  public String episodeUrl;
  public int episodeId;

  public EpisodeListInstance(Context context) {
    super(context);
  }

  public EpisodeListInstance(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Initialize the thumbnail view.
   */
  public void initView(Bitmap img, String name, String url, int id) {
    episodeLayout = (LinearLayout) inflate(getContext(), R.layout.episode_instance_custom, null );
    addView(episodeLayout);

    episodeImage = (ImageView) episodeLayout.findViewById(R.id.episode_instance_image);
    episodeName = (TextView) episodeLayout.findViewById(R.id.episode_instance_text);
    setEpisodeImage(img);
    setEpisodeName(name);
    setEpisodeUrl(url);
    setEpisodeId(id);
  }

  /**
   * Set the thumbnail view with given bitmap.
   */
  private void setEpisodeImage(Bitmap thumbBimtmap) {
    episodeImage.setImageBitmap(thumbBimtmap);
  }

  /**
   * Set the thumbnail name.
   */
  private void setEpisodeName(String name) {
    Log.d("episode name", name);
    episodeName.setText(name);
  }

  /**
   * Set the thumbnail name.
   */
  public void setEpisodeUrl(String url) {
    episodeUrl = url;
  }

  /**
   *Get the first episode url of the webtoon of thumbnail.
   */
  public String getEpisodeUrl() {
    return episodeUrl;
  }

  public int getEpisodeId() {
    return episodeId;
  }

  public void setEpisodeId(int id) {
    episodeId = id;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return false;
  }
}
