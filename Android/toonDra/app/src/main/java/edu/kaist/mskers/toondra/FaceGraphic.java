/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.kaist.mskers.toondra;

import com.google.android.gms.vision.face.Face;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
  private static final float FACE_POSITION_RADIUS = 10.0f;
  private static final float ID_TEXT_SIZE = 40.0f;
  private static final float ID_Y_OFFSET = 50.0f;
  private static final float ID_X_OFFSET = -50.0f;
  private static final float BOX_STROKE_WIDTH = 5.0f;

  private static final int[] COLOR_CHOICES = {
      Color.BLUE,
      Color.CYAN,
      Color.GREEN,
      Color.MAGENTA,
      Color.RED,
      Color.WHITE,
      Color.YELLOW
  };
  private static int currentColorIndex = 0;

  private Paint facePositionPaint;
  private Paint idPaint;
  private Paint boxPaint;

  private volatile Face face;
  private int faceId;

  FaceGraphic(GraphicOverlay overlay) {
    super(overlay);

    currentColorIndex = (currentColorIndex + 1) % COLOR_CHOICES.length;
    final int selectedColor = COLOR_CHOICES[currentColorIndex];

    facePositionPaint = new Paint();
    facePositionPaint.setColor(selectedColor);

    idPaint = new Paint();
    idPaint.setColor(selectedColor);
    idPaint.setTextSize(ID_TEXT_SIZE);

    boxPaint = new Paint();
    boxPaint.setColor(selectedColor);
    boxPaint.setStyle(Paint.Style.STROKE);
    boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
  }

  void setId(int id) {
    faceId = id;
  }


  /**
   * Updates the face instance from the detection of the most recent frame.  Invalidates the
   * relevant portions of the overlay to trigger a redraw.
   */
  void updateFace(Face face) {
    this.face = face;
    postInvalidate();
  }

  /**
   * Draws the face annotations for position on the supplied canvas.
   */
  @Override
  public void draw(Canvas canvas) {
    Face face = this.face;
    if (face == null) {
      return;
    }

    // Draws a circle at the position of the detected face, with the face's track id below.
    float xcoord = translateX(face.getPosition().x + face.getWidth() / 2);
    float ycoord = translateY(face.getPosition().y + face.getHeight() / 2);
    canvas.drawCircle(xcoord, ycoord, FACE_POSITION_RADIUS, facePositionPaint);
    canvas.drawText("id: " + faceId, xcoord + ID_X_OFFSET, ycoord + ID_Y_OFFSET, idPaint);
    canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()),
        xcoord - ID_X_OFFSET, ycoord - ID_Y_OFFSET, idPaint);
    canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()),
        xcoord + ID_X_OFFSET * 2, ycoord + ID_Y_OFFSET * 2, idPaint);
    canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()),
        xcoord - ID_X_OFFSET * 2, ycoord - ID_Y_OFFSET * 2, idPaint);

    // Draws a bounding box around the face.
    float xoffset = scaleX(face.getWidth() / 2.0f);
    float yoffset = scaleY(face.getHeight() / 2.0f);
    float left = xcoord - xoffset;
    float top = ycoord - yoffset;
    float right = xcoord + xoffset;
    float bottom = ycoord + yoffset;
    canvas.drawRect(left, top, right, bottom, boxPaint);
  }
}
