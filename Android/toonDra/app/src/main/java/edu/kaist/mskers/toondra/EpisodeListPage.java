package edu.kaist.mskers.toondra;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by harrykim on 2016. 10. 14..
 */

public class EpisodeListPage extends AppCompatActivity{

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.episodelist_fragment);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getSupportActionBar().setTitle("Sample listview from Naver Webtoon");
    ImageView imageView = (ImageView) findViewById(R.id.episodeListView);

    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ReadEpisodePage.class);
        startActivity(intent);
      }
    });
  }

}
