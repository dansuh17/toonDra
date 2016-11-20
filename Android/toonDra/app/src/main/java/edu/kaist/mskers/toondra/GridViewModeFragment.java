package edu.kaist.mskers.toondra;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;
import edu.kaist.mskers.toondra.navermodule.webtoon.Day;

/**
 * Created by harrykim on 2016. 10. 14..
 * Fragment class that gives the view of the gridView mode.
 */

public class GridViewModeFragment extends Fragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.gridviewmode_fragment_main, container, false);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Log.d(this.getClass().getSimpleName(), "Fragment is created");
    super.onActivityCreated(savedInstanceState);
    addThumbnailsToGrid(((MainActivity)getActivity()).getCurrentDay());
  }

  /**
   * Remove previous gridview, and then set the new gridview.
   */
  public void addThumbnailsToGrid(Day day) {
    GridView gridView = (GridView) getView().findViewById(R.id.fullGrid);
    gridView.setAdapter(null);

    NaverToonInfo[] webtoons = ((MainActivity)getActivity()).getCurrentWebtoonsByDay(day);
    if (webtoons != null) {
      ThumbnailAdapter adapter = new ThumbnailAdapter(getActivity(), webtoons);
      gridView.setAdapter(adapter);
    }
  }
}
