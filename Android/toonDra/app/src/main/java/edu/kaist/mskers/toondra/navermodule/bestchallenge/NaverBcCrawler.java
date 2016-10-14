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

package edu.kaist.mskers.toondra.navermodule.bestchallenge;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import edu.kaist.mskers.toondra.navermodule.NaverToonCategory;
import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;
import edu.kaist.mskers.toondra.navermodule.challenge.Genre;
import edu.kaist.mskers.toondra.navermodule.webtoon.NaverWebtoonUrl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.regex.Matcher;


/**
 * This class provides utility methods to crawl naver best challenges.
 */
public class NaverBcCrawler {

  private static PrintWriter pw = new PrintWriter(System.out, true);

  /**
   * Download best challenge webtoon info classified by genre.
   */
  public static NaverToonInfo[] downloadBcListByGenre(Genre genre, int pageNum) {

    // Create necessary references
    String href;
    String url = "";
    String thumbUrl;
    Document doc;
    Element listBox;
    Element img;
    Element imgSrc;
    Element link;
    Elements imgList;
    Matcher mat;

    // Try connect to url.
    try {
      url = NaverBcUrl.getGenreListUrl(genre, pageNum);
      doc = Jsoup.connect(url).get();
    } catch (IOException ioe) {
      pw.println("Unable to connect to " + url);
      ioe.printStackTrace();
      return null;
    }

    // Grab available BC lists
    listBox = doc.getElementById("content")
        .getElementsByClass("weekchallengeBox").first();

    // Each element in imgList contains a thumbnail and a link to the BC
    imgList = listBox.getElementsByClass("fl");

    // Each element in titleList contains a title and a link to the BC
    //        titleList = listBox.getElementsByClass("challengeTitle");

    // Initialize NaverToonInfo
    int bcTotal = imgList.size(); // total number of Best Challenges for this genre and page
    NaverToonInfo[] info = new NaverToonInfo[bcTotal];

    pw.println("Getting total of " + bcTotal + " best challenges");

    for (int i = 0; i < bcTotal; i++) {
      img = imgList.get(i);
      link = img.getElementsByTag("a").first();
      href = link.attr("href");

      thumbUrl = link.child(0).absUrl("src");
      Log.e("thumbUrl:", thumbUrl);
      try {
        URL imgUrl = new URL(thumbUrl);
        Bitmap bitmap = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());
        // Use Regex to pull title id from the href link
        mat = NaverWebtoonUrl.titleIdPat.matcher(href);
        mat.find();
        info[i] = new NaverToonInfo(mat.group(1), link.attr("title"),
            bitmap,
            NaverToonCategory.WEBTOON);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return info;
  }
}
