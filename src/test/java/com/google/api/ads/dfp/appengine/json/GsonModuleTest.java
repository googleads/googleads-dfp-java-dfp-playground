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

package com.google.api.ads.dfp.appengine.json;

import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link GsonModule}.
 *
 * @author Jeff Sham
 */
@RunWith(JUnit4.class)
public class GsonModuleTest {

  private Gson gson;

  /**
   * Constructor.
   */
  public GsonModuleTest() {}

  @Before
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new GsonModule());
    gson = injector.getInstance(Gson.class);
  }

  /**
   * Test method for JSON serialization with nulls in object.
   */
  @Test
  public void testToJson_withValuesInObject() {
    String json = gson.toJson(new TestObject("abc"));
    assertTrue(json.contains("abc"));
  }

  /**
   * Test method for JSON serialization with nulls in object.
   */
  @Test
  public void testToJson_withNullsInObject() {
    String json = gson.toJson(new TestObject());
    assertTrue(json.contains("null"));
  }

  /** An object for testing. */
  public class TestObject {
    private String field;

    /** Default constructor. */
    public TestObject() {
    }

    /** Constructor with field value. */
    public TestObject(String field) {
      this.field = field;
    }
  }
}
