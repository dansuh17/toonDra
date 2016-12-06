package edu.kaist.mskers.toondra;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;

/**
 * Class ThumbnailAdapter
 * Adpater class to add all the items into the gridView.
 */
class ThumbnailAdapter extends BaseAdapter {
  Context context;
  NaverToonInfo[] webtoons;
  LayoutInflater inflater;

  public ThumbnailAdapter(Context context, NaverToonInfo[] webtoons) {
    this.context = context;
    this.webtoons = webtoons;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return webtoons.length;
  }

  @Override
  public Object getItem(int position) {
    return webtoons[position];
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = new Thumbnail(context);
      //convertView = inflater.inflate(R.layout.thumbnail_custom, null);
    }
    ((Thumbnail)convertView).initView(webtoons[position]);
    final String listviewUrl = ((Thumbnail)convertView).getListViewUrl();
    convertView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(context, EpisodeListPage.class);
        Log.e("listviewUrl_in_grid", listviewUrl);
        intent.putExtra("listview_url", listviewUrl);
        context.startActivity(intent);
        return;
      }
    });
    return convertView;
  }
}
