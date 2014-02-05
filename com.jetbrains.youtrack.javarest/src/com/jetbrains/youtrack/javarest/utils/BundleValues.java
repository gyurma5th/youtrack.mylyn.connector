package com.jetbrains.youtrack.javarest.utils;

import java.util.LinkedList;

public class BundleValues {

  private LinkedList<? extends BundleValue> bundleValues;

  /*
   * public LinkedList<String> getValues() { LinkedList<String> values = new LinkedList<>();
   * if(bundleValues != null){ for(BundleValue value : bundleValues){ values.add(value.getValue());
   * } } return values; }
   * 
   * public void setBundleValues(LinkedList<? extends BundleValue> bv) { this.bundleValues = bv; }
   */
  public LinkedList<String> getValues() {
    return null;
  }

  public LinkedList<? extends BundleValue> getBundleValues() {
    return bundleValues;
  }
}
