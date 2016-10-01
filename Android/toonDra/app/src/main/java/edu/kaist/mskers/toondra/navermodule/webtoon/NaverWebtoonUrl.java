package edu.kaist.mskers.toondra.navermodule.webtoon;

import edu.kaist.mskers.toondra.navermodule.NaverUrl;

import java.util.regex.Pattern;

/**
 * This class defines URLs needed to access Naver webtoons.
 */
public class NaverWebtoonUrl {

  public static final String WEBTOON_BASE = NaverUrl.BASE_URL + "/webtoon";

  public static final String WEBTOON_LIST_BASE
      = WEBTOON_BASE + "/list.nhn?titleId=";

  public static final String WEBTOON_DETAIL_BASE
      = WEBTOON_BASE + "/detail.nhn?";

  private static final String WEBTOON_WEEKDAY_BASE
      = WEBTOON_BASE + "/weekdayList.nhn?week=";

  /* titleIdPat is used to pull titieId from a href links.
   ex> It is used to pull titleId from
   /webtoon/list.nhn?titleId=530311&weekday=sat */
  public static final Pattern titleIdPat
      = Pattern.compile("titleId=(\\d*)");
  public static final Pattern noPat = Pattern.compile("no=(\\d*)");


  /**
   * Get the url of the list of webtoons categorized by the day.
   */
  public static String getDayListUrl(final Day day) {
    if (day == Day.ALL) {
      return WEBTOON_BASE + "/finish.nhn";
    }
    return WEBTOON_WEEKDAY_BASE + day.getDay();
  }

  /**
   * Get selected webtoon main page url.
   */
  public static String getWebtoonListUrl(final String titleId) {
    return WEBTOON_LIST_BASE + titleId;
  }

  /**
   * Get full url of the webtoon episode.
   */
  public static String getWebtoonDetailUrl(final String titleId, final int num) {
    return WEBTOON_DETAIL_BASE + "titleId=" + titleId + "&no=" + num;
  }
}
