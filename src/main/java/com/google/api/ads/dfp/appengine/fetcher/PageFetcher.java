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

import com.google.api.ads.dfp.jaxws.v201306.ApiException_Exception;
import com.google.api.ads.dfp.jaxws.v201306.Statement;

/**
 * An interface for fetching a single page.
 *
 * @author Jeff Sham
 */
public interface PageFetcher {

  /**
   * Fetch a page of entity results filtered by the filter statement criteria.
   *
   * @param statement the statement to filter entities by
   */
  public abstract Object getByStatement(Statement statement) throws ApiException_Exception;
}
