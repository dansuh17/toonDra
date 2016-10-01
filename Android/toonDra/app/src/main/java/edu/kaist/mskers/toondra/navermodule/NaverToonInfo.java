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

package edu.kaist.mskers.toondra.navermodule;

/**
 * This class contains information related to a naver webtoon.
 */
public class NaverToonInfo {
  private String titleId;
  private String titleName;
  private String thumbUrl;
  private NaverToonCategory cat;

  /**
   * Set titleId, titlename, thumbnail url and category to the class.
   */
  public NaverToonInfo(String titleId, String titleName, String thumbUrl,
                       NaverToonCategory cat) {
    this.titleId = titleId;
    this.titleName = titleName;
    this.thumbUrl = thumbUrl;
    this.cat = cat;
  }

  public String getTitleId() {
    return titleId;
  }

  public String getTitleName() {
    return titleName;
  }

  public String getthumbUrl() {
    return thumbUrl;
  }

  public NaverToonCategory getCategory() {
    return cat;
  }


}
