// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.dfp.appengine.fetcher;

import com.google.api.ads.dfp.appengine.DfpTags;
import com.google.api.ads.dfp.appengine.DfpTypeOverrides;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import java.util.Map;

/**
 * Factory for creating different types of API object fetchers.
 *
 * @author Jeff Sham
 */
public class FetcherFactory {

  private final Map<String, Fetcher> fetchers;

  @Inject
  public FetcherFactory(Map<String, Fetcher> fetchers) {
    this.fetchers = fetchers;
  }

  /**
   * Returns a fetcher for an API object.
   *
   * @param tag the panel to return results to
   * @param typeOverride determines the type of object to return for panels with multiple types
   * @return an API object fetcher
   * @throws IllegalArgumentException if a fetcher cannot be found
   */
  public Fetcher getInstance(String tag, String typeOverride) {
    Preconditions.checkNotNull(tag);
    Preconditions.checkNotNull(typeOverride);

    if (tag.equals(DfpTags.ORDERS)) {
      if (typeOverride.equals(DfpTypeOverrides.ORDERS)) {
        return fetchers.get(DfpTags.ORDERS);
      } else if (typeOverride.equals(DfpTypeOverrides.LINE_ITEMS)) {
        return fetchers.get(DfpTags.LINE_ITEMS);
      } else {
        return fetchers.get(DfpTags.ORDERS_LINE_ITEMS);
      }
    } else if (tag.equals(DfpTags.CUSTOM_TARGETING)) {
      if (typeOverride.equals(DfpTypeOverrides.KEYS)) {
        return fetchers.get(DfpTags.CUSTOM_TARGETING_KEYS);
      } else if (typeOverride.equals(DfpTypeOverrides.VALUES)) {
        return fetchers.get(DfpTags.CUSTOM_TARGETING_VALUES);
      } else {
        return fetchers.get(DfpTags.CUSTOM_TARGETING);
      }
    } else {
      Fetcher fetcher = fetchers.get(tag);
      if (fetcher != null) {
        return fetcher;
      } else {
        throw new IllegalArgumentException("Tag does not have a matching fetcher.");
      }
    }
  }
}
