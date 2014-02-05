package com.jetbrains.mylyn.yt.ui;

import org.eclipse.swt.graphics.RGB;

public enum PriorityColorIndex {

  COLOR_0(0, 255, 255, 255, 0, 0, 0),
  COLOR_1(1, 204, 0, 0, 255, 255, 255),
  COLOR_2(2, 204, 102, 0, 255, 255, 255),
  COLOR_3(3, 51, 153, 51, 255, 255, 255),
  COLOR_4(4, 0, 102, 204, 255, 255, 255),
  COLOR_5(5, 167, 0, 126, 255, 255, 255),
  COLOR_6(6, 123, 53, 219, 255, 255, 255),
  COLOR_7(7, 0, 161, 180, 255, 255, 255),
  COLOR_8(8, 150, 24, 1, 255, 255, 255),
  COLOR_9(9, 242, 148, 255, 0, 0, 0),
  COLOR_10(10, 190, 246, 36, 0, 0, 0),
  COLOR_11(11, 255, 198, 0, 0, 0, 0),
  COLOR_12(12, 255, 204, 204, 0, 0, 0),
  COLOR_13(13, 255, 234, 115, 0, 0, 0),
  COLOR_14(14, 217, 255, 200, 0, 0, 0),
  COLOR_15(15, 204, 255, 255, 0, 0, 0),
  COLOR_16(16, 255, 255, 255, 100, 153, 44),
  COLOR_17(17, 235, 244, 221, 100, 153, 44),
  COLOR_18(18, 240, 242, 243, 204, 0, 0),
  COLOR_19(19, 255, 227, 227, 204, 0, 0),
  COLOR_20(20, 255, 227, 227, 204, 0, 0);


  private RGB backgroundColor;

  private RGB fontColor;

  private int index;

  PriorityColorIndex(int index, int backgroundRed, int backgroundGreen, int backgroundBlue,
      int fontRed, int fontGreen, int fontBlue) {
    this.index = index;
    this.backgroundColor = new RGB(backgroundRed, backgroundGreen, backgroundBlue);
    this.fontColor = new RGB(fontRed, fontGreen, fontBlue);
  }

  public RGB getBackground() {
    return this.backgroundColor;
  }

  public RGB getFont() {
    return this.fontColor;
  }


  public static PriorityColorIndex getColorByIndex(int index) {
    for (PriorityColorIndex type : values()) {
      if (type.index == index) {
        return type;
      }
    }
    return null;
  }

}
