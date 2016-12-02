package edu.kaist.mskers.toondra;

/**
 * Created by harrykim on 2016. 12. 3..
 * Referenced from http://stackoverflow.com/questions/10316743/detect-end-of-scrollview
 */

public interface ScrollViewListener {
  void onScrollChanged(ScrollViewExt scrollView,
                       int x, int y, int oldx, int oldy);
}
