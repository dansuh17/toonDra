package edu.kaist.mskers.toondra;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;
import edu.kaist.mskers.toondra.navermodule.webtoon.Day;

/**
 * Created by harrykim on 2016. 10. 14..
 */

public class GridViewModeFragment extends Fragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
    View view = inflater.inflate(R.layout.gridviewmode_fragment_main, container, false);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Log.d(this.getClass().getSimpleName(), "Fragment is created");
    super.onActivityCreated(savedInstanceState);
    addThumbnailsToGrid(((MainActivity)getActivity()).getCurrentDay());
  }

  public void addThumbnailsToGrid(Day day) {
    NaverToonInfo webtoons[] = ((MainActivity)getActivity()).getCurrentWebtoonsByDay(day);

    ThumbnailAdapter adapter = new ThumbnailAdapter (getActivity(), webtoons);
    GridView gridView = (GridView) getView().findViewById(R.id.fullGrid);
    gridView.setAdapter(adapter);
  }
}

class ThumbnailAdapter extends BaseAdapter {
  Context context;
  NaverToonInfo[] webtoons;
  LayoutInflater inflater;

  public ThumbnailAdapter(Context context, NaverToonInfo[] webtoons) {
    this.context = context;
    this.webtoons = webtoons;
    inflater = (LayoutInflater) context.getSystemService
        (Context.LAYOUT_INFLATER_SERVICE);
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
    if (convertView==null) {
      convertView = new Thumbnail(context);
      //convertView = inflater.inflate(R.layout.thumbnail_custom, null);
    }
    ((Thumbnail)convertView).initView(webtoons[position]);
    convertView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(context, EpisodeListPage.class);
        context.startActivity(intent);
        return;
      }
    });
    return convertView;
  }
}

