/*
The MIT License (MIT)

        Copyright (c) 2013 Seulgi Kim

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in
        all copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
        THE SOFTWARE.
*/

package edu.kaist.mskers.toondra.navermodule.challenge;

import edu.kaist.mskers.toondra.navermodule.NaverUrl;

import java.util.regex.Pattern;

/**
 * Contains URL for naver challenge webtoons.
 */
public class NaverChUrl {

  public static final String CH_GENRE_BASE =
      NaverUrl.BASE_URL + "/genre/challenge.nhn?m=";

  public static final String CH_LIST_BASE =
      NaverUrl.BASE_URL + "/challenge/list.nhn?titleId=";

  public static final String CH_DETAIL_BASE =
      NaverUrl.BASE_URL + "/challenge/detail.nhn?";

  public static final Pattern titleIdPat
      = Pattern.compile("titleId=(\\d*)");

  public static final Pattern noPat = Pattern.compile("no=(\\d*)");

  public static String getGenreListUrl(final Genre genre, int pageNum) {
    return CH_GENRE_BASE + genre.getGenre() + "&page=" + pageNum;
  }

  public static String getChListUrl(final String titleId) {
    return CH_LIST_BASE + titleId;
  }

  public static String getChDetailUrl(final String titleId, final int num) {
    return CH_DETAIL_BASE + "titleId=" + titleId + "&no=" + num;
  }

}
