package edu.kaist.mskers.toondra;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by harrykim on 2016. 12. 3..
 * Referenced from http://stackoverflow.com/questions/10316743/detect-end-of-scrollview
 */

public class ScrollViewExt extends ScrollView {
  private ScrollViewListener scrollViewListener = null;
  public ScrollViewExt(Context context) {
    super(context);
  }

  public ScrollViewExt(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public ScrollViewExt(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setScrollViewListener(ScrollViewListener scrollViewListener) {
    this.scrollViewListener = scrollViewListener;
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    if (scrollViewListener != null) {
      scrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
    }
  }
}

