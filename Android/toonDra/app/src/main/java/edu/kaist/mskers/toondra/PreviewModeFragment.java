package edu.kaist.mskers.toondra;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;
import edu.kaist.mskers.toondra.navermodule.webtoon.Day;
import edu.kaist.mskers.toondra.navermodule.webtoon.NaverWebtoonCrawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by harrykim on 2016. 10. 14..
 * Class PreviewModeFragment
 * Fragment class that gives teh view of the preview mode.
 */
public class PreviewModeFragment extends Fragment {
  private Thumbnail currentWebtoon = null;
  private long lastClickTime = 0;
  Thread pageThread = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.previewmode_fragment_main, container, false);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Log.d(this.getClass().getSimpleName(), "Fragment is created");
    super.onActivityCreated(savedInstanceState);
    addThumbnailsToLeftByDay(((MainActivity)getActivity()).getCurrentDay());
  }

  /**
   * Add preview mode thumbnails to the left of the view.
   */
  public void addThumbnailsToLeftByDay(final Day day) {
    final LinearLayout leftLinear = (LinearLayout) getView().findViewById(R.id.leftLinearLayout);
    final LinearLayout rightLinear = (LinearLayout) getView().findViewById(R.id.rightLinearLayout);
    leftLinear.removeAllViews();
    final ArrayList<Thumbnail> thumbnailArrayList = new ArrayList<Thumbnail>();

    /* Download webtoon list by day and set first preview link */
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        NaverToonInfo[] webtoons = ((MainActivity)getActivity()).getCurrentWebtoonsByDay(day);
        if (webtoons != null) {
          for (int i = 0; i < webtoons.length; i++) {
            final Thumbnail thumb = new Thumbnail(getActivity());
            thumb.initView(webtoons[i]);
            thumbnailArrayList.add(thumb);

            final String firstEpisodeUrl = thumb.getFirstEpisodeUrl();

            View.OnClickListener onClickListener = new View.OnClickListener() {
              public void onClick(View view) {

                if (SystemClock.elapsedRealtime() - lastClickTime < 2000) {
                  return;
                }

                lastClickTime = SystemClock.elapsedRealtime();

                if (currentWebtoon != null) {
                  currentWebtoon.thumbName.setBackgroundColor(ContextCompat
                      .getColor(getActivity(), R.color.waitButtonColor));
                  currentWebtoon.thumbName.setTextColor(ContextCompat
                      .getColor(getActivity(), R.color.waitTextColor));
                }
                thumb.thumbName.setBackgroundColor(ContextCompat
                    .getColor(getActivity(), R.color.activeButtonColor));
                thumb.thumbName.setTextColor(ContextCompat
                    .getColor(getActivity(), R.color.activeTextColor));
                currentWebtoon = thumb;

                if (pageThread != null && pageThread.isAlive()) {
                  pageThread.interrupt();
                }
                rightLinear.removeAllViews();
                startFirstEpisodePreview(firstEpisodeUrl);
              }
            };

            View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
              public boolean onLongClick(View view) {
                Log.e("longClick", "ok");
                Intent intent = new Intent(getActivity(), EpisodeListPage.class);
                intent.putExtra("listview_url", thumb.getListViewUrl());
                intent.putExtra("webtoon_name", thumb.getThumbName());
                startActivity(intent);
                return false;
              }
            };

            thumb.thumbLayout.setOnClickListener(onClickListener);
            thumb.thumbLayout.setOnLongClickListener(onLongClickListener);

            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                leftLinear.addView(thumb);
              }
            });
          }
        }
      }
    });
    thread.start();
  }

  /**
   * Add the first episode to the right side of the main view by giving first episode url.
   */
  private void startFirstEpisodePreview(final String firstEpisodeUrl) {
    final LinearLayout rightLinear = (LinearLayout) getView().findViewById(R.id.rightLinearLayout);
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
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast toast =  Toast.makeText(getActivity().getApplicationContext(),
                    "19세 웹툰 이용을 위해서는 성인 인증이 필요합니다.",
                    Toast.LENGTH_LONG);
                toast.show();
              }
            });
            return;
          }


          int previewPageLengthCnt = 0;
          for (Element imgLink : wtViewer.children()) {
            previewPageLengthCnt++;
            if (previewPageLengthCnt > 5) {
              break;
            }
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
            BitmapFactory.Options options = new BitmapFactory.Options();;
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory
                .decodeByteArray(bitmapData, 0, bitmapData.length, options);

            final ImageView pageImage = (ImageView) getActivity()
                .getLayoutInflater().inflate(R.layout.page_custom, null);

            pageImage.setImageBitmap(bitmap);
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                rightLinear.addView(pageImage);
              }
            });
          }

        } catch (IOException ex) {
          ex.printStackTrace();
        }

      }
    });
    pageThread.start();
  }
}
