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

import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;
import edu.kaist.mskers.toondra.navermodule.webtoon.Day;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by harrykim on 2016. 10. 14..
 */

public class PreviewModeFragment extends Fragment {
  private static final int MAX_DOWNLOAD_SIZE_BYTES = 1024 * 1024 * 10; // 10MiB
  private Thumbnail currentWebtoon = null;
  private long lastClickTime = 0;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
    View view = inflater.inflate(R.layout.previewmode_fragment_main, container, false );
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Log.d(this.getClass().getSimpleName(), "Fragment is created");
    super.onActivityCreated(savedInstanceState);
    addThumbnailsToLeftByDay(((MainActivity)getActivity()).getCurrentDay());
  }

  public void addThumbnailsToLeftByDay(final Day day) {
    final LinearLayout leftLinear = (LinearLayout) getView().findViewById(R.id.leftLinearLayout);
    final LinearLayout rightLinear = (LinearLayout) getView().findViewById(R.id.rightLinearLayout);
    leftLinear.removeAllViews();
    final ArrayList<Thumbnail> thumbnailArrayList = new ArrayList<Thumbnail>();

    /* Download webtoon list by day and set first preview link */
    Thread mThread = new Thread(new Runnable() {
      @Override
      public void run() {
        NaverToonInfo webtoons[] = ((MainActivity)getActivity()).getCurrentWebtoonsByDay(day);
        for (int i = 0; i < webtoons.length; i++) {
          final Thumbnail thumb = new Thumbnail(getActivity());
          thumb.initView(webtoons[i]);
          thumbnailArrayList.add(thumb);

          final String firstEpisodeUrl = thumb.getFirstEpisodeUrl();

          View.OnClickListener mOnClickListener = new View.OnClickListener() {
            public void onClick(View view) {
              if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                return;
              }
              lastClickTime = SystemClock.elapsedRealtime();

              if (currentWebtoon != null) {
                currentWebtoon.thumbName.setBackgroundColor(ContextCompat
                    .getColor(getActivity(), R.color.waitButtonColor));
              }
              thumb.thumbName.setBackgroundColor(ContextCompat
                  .getColor(getActivity(), R.color.activeButtonColor));
              currentWebtoon = thumb;
              rightLinear.removeAllViews();
              startFirstEpisodePreview(firstEpisodeUrl);
            }
          };

          View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
              Log.e("longClick", "ok");
              Intent intent = new Intent(getActivity(), EpisodeListPage.class);
              startActivity(intent);
              return false;
            }
          };

          thumb.thumbLayout.setOnClickListener(mOnClickListener);
          thumb.thumbLayout.setOnLongClickListener(mOnLongClickListener);

          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              leftLinear.addView(thumb);
            }
          });

        }
      }
    });
    mThread.start();
  }

  private void startFirstEpisodePreview(final String firstEpisodeUrl) {
    final LinearLayout rightLinear = (LinearLayout) getView().findViewById(R.id.rightLinearLayout);
    final Thread pageThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Element wtPage;
          wtPage = Jsoup.connect(firstEpisodeUrl)
              .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0)" +
                  "Gecko/20100101 Firefox/23.0")
              .get();
          Element wtViewer = wtPage.getElementsByClass("wt_viewer").first();

          for (Element imgLink : wtViewer.children()) {
            String imgUrl = imgLink.absUrl("src");

            // Check whether imgURL is a valid image file
            if (imgUrl == null || !imgUrl.endsWith(".jpg"))
              continue;


            Connection.Response wtRes;
            wtRes = Jsoup.connect(imgUrl)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0)" +
                    "Gecko/20100101 Firefox/23.0")
                .referrer(firstEpisodeUrl)
                .ignoreContentType(true)
                .maxBodySize(MAX_DOWNLOAD_SIZE_BYTES)
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
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    });
    pageThread.start();
  }
}
