package edu.kaist.mskers.toondra.navermodule.webtoon;

import edu.kaist.mskers.toondra.navermodule.NaverToonCategory;
import edu.kaist.mskers.toondra.navermodule.NaverToonInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;


/**
 * This class provides utility methods to crawl naver webtoon.
 */
public class NaverWebtoonCrawler {


  /**
   * Download available webtoon information for given day.
   *
   * @param day the day you want to download webtoon list from
   * @return Available webtoon information for the given day
   */
  public static NaverToonInfo[] downloadWebtoonListByDay(Day day) {
    // Create necessary references
    NaverToonInfo[] info;
    String url = "";
    String thumbUrl;
    Document doc;
    Element img;
    Element link;
    Matcher mat;

    // Try connect to url.
    try {
      url = NaverWebtoonUrl.getDayListUrl(day);
      doc = Jsoup.connect(url).userAgent("Mozilla").get();
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }


    // Grab available webtoon lists
    Element content = doc.getElementById("content");
    Elements imgList = content.getElementsByClass("img_list").first().children();
    int webtoonTotal = imgList.size(); // total number of webtoons for this day

    info = new NaverToonInfo[webtoonTotal];

    String href;
    for (int i = 0; i < webtoonTotal; i++) {
      img = imgList.get(i);
      link = img.getElementsByClass("thumb").first()
          .getElementsByTag("a").first();
      href = link.attr("href");

      thumbUrl = link.child(0).absUrl("src");
      // Use Regex to pull title id from the href link
      mat = NaverWebtoonUrl.titleIdPat.matcher(href);
      mat.find();
      info[i] = new NaverToonInfo(mat.group(1), link.attr("title"),
          thumbUrl,
          NaverToonCategory.WEBTOON);
    }

    return info;
  }

}
