package com.scoperetail.fusion.core.common;

/*-
 * *****
 * fusion-core
 * -----
 * Copyright (C) 2018 - 2021 Scope Retail Systems Inc.
 * -----
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * =====
 */

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@lombok.Data
public class PerformanceCounter {
  private final Map<String, Data> performanceDataMap = new LinkedHashMap<>();
  private final long startTime = System.currentTimeMillis();
  private final String parentFlow;

  public PerformanceCounter(final String parentFlow) {
    this.parentFlow = parentFlow;
  }

  public PerformanceCounter(final String parentFlow, final String userId) {
    this.parentFlow = parentFlow;
  }

  public void addSubFlow(final String flow) {
    final Data data = performanceDataMap.get(flow);
    if (data == null) {
      performanceDataMap.put(flow, new Data());
    } else {
      data.endTime = 0;
      data.startTime = System.currentTimeMillis();
    }
  }

  public void completeSubFlow(final String flow) {
    final Data data = performanceDataMap.get(flow);
    if (data != null) {
      data.endTime = System.currentTimeMillis();
      data.diff += data.endTime - data.startTime;
    }
  }

  @Override
  public String toString() {
    final String mapString =
        performanceDataMap
            .entrySet()
            .stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue())
            .collect(Collectors.joining("\n"));
    final StringBuilder sb =
        new StringBuilder(
            "Total time taken by parent flow : "
                + parentFlow
                + " ="
                + (System.currentTimeMillis() - startTime)
                + "\n"
                + "Below are the details of each sub flow :\n "
                + mapString);
    return sb.toString();
  }

  @lombok.Data
  public static class Data {
    private long startTime = System.currentTimeMillis();
    private long endTime;
    private long diff;

    @Override
    public String toString() {
      return "" + diff;
    }
  }
}
