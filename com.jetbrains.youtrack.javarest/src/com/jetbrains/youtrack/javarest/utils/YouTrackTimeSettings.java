package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "timesettings")
public class YouTrackTimeSettings {

  @XmlElement(name = "hoursADay")
  private int hoursPerDay;

  public int getHoursSettings() {
    return hoursPerDay;
  }

  @XmlElement(name = "daysAWeek")
  private int daysInWeek;

  public int getDaysSettings() {
    return daysInWeek;
  }


}
