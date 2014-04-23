/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @fileoverview Main javascript file for the application.
 * @author api.ekoleda@gmail.com (Eric Koleda)
 * @author api.shamjeff@gmail.com (Jeff Sham)
 */

// Declare namespace.
var dfpwebapp = dfpwebapp || {};

/**
 * Class used to manage panels.
 * @param {Object.<string, Object>} options Map of configuration options.
 * @constructor
 */
dfpwebapp.PanelManager = function(options) {
  /**
   * The class to apply when the panel is loading.
   * @type {string}
   */
  this._loadingClass = options.loadingClass || 'dfp-loading';

  /**
   * The class that identifies the panel's content div.
   * @type {string}
   */
  this._panelContentClass = options.panelContentClass || 'dfp-panel-content';

  /**
   * The class that identifies the panel's div.
   * @type {string}
   */
  this._panelClass = options.panelClass || 'dfp-panel';

  /**
   * The class that identifies the panel's reload link.
   * @type {string}
   */
  this._reloadLinkClass = options.reloadLinkClass || 'dfp-reload';

  /**
   * The class that identifies the panel's cancel link.
   * @type {string}
   */
  this._cancelLinkClass = options._cancelLinkClass || 'dfp-cancel';

  /**
   * The class that identifies the panel's expand link.
   * @type {string}
   */
  this._expandLinkClass = options.expandLinkClass || 'dfp-expand';

  /**
   * The class that identifies an element in the menu bar with a drop down menu.
   * @type {string}
   */
  this._menuHeaderClass = options.menuHeaderClass || 'dfp-menu-header';


  /**
   * The class that identifies the panel's filter button.
   * @type {string}
   */
  this._filterButtonClass = options.filterButtonClass || 'dfp-filter-button';

  /**
   * The class to apply when displaying an error message.
   * @type {string}
   */
  this._errorClass = options.errorClass || 'dfp-error';

  /**
   * The class to apply when displaying an info message.
   * @type {string}
   */
  this._infoClass = options.infoClass || 'dfp-info';

  /**
   * The class that identifies the filter panel.
   * @type {string}
   */
  this._filterPanelClass = options.filterPanelClass || 'dfp-panel-filter';

  /**
   * The class that identifies the 'content' section of the filter panel.
   * @type {string}
   */
  this._filterPanelContentClass = options.filterPanelContentClass ||
      'dfp-panel-filter-content';

  /**
   * The class that identifies the filter type select.
   * @type {string}
   */
  this._filterSelectClass = options.filterSelectClass || 'dfp-filter-select';

  /**
   * The class that identifies the filter text area.
   * @type {string}
   */
  this._filterTextAreaClass = options.filterSelectClass ||
      'dfp-filter-textarea';

  /**
   * The class that identifies the details link on each item
   * @type {string}
   */
  this._detailsLinkClass = options.detailsLinkClass || '.dfp-details-link';

  /**
   * The class that identifies a unrounded corner.
   * @type {string}
   */
  this._uncornerClass = options.uncornerClass || 'uncorner';

  /**
   * The title for the expanded publisher query language table dialog.
   * @type {string}
   */
  this._publisherQueryLanguageDialogTitle =
      options._publisherQueryLanguageDialogTitle ||
      'Publisher Query Language Results';

  /**
   * A map of panel ids to urls.
   * @type {Object.<string, string>}
   */
  this._panels = {};

  /**
   * A map of the panel ID to the ID of the last request made for that panel.
   * @type {Object.<string, string>}
   */
  this._panelLastRequestId = {};
};

/**
 * Attach events to panel objects.
 */
dfpwebapp.PanelManager.prototype.init = function() {
  var self = this;

  // Use text from local storage if available.
  if (window.localStorage) {
    $('.' + self._panelClass).each(function() {
      var savedText = window.localStorage.getItem(this.id);
      if (savedText) {
        $('#' + this.id + ' .' + self._filterTextAreaClass).val(savedText);
      }
    });
  }

  // Attach click event to reload links.
  $('.' + self._reloadLinkClass).click(function() {
    self.loadPanel($(this).parents('.' + self._panelClass).attr('id'),
        'default');
    return false;
  });

  // Attach click event to expand links.
  $('.' + self._expandLinkClass).click(function() {
    var contentPanel = $(this).siblings('.' + self._panelContentClass).html();
    var $dialog = $('<div>').html(contentPanel).dialog({
      width: 600,
      height: 600,
      title: self._publisherQueryLanguageDialogTitle,
      modal: true,
      autoOpen: false,
      draggable: false
     });
    $dialog.dialog('open');
    return false;
  });

  // Attach click event to cancel links.
  $('.' + self._cancelLinkClass).click(function() {
    self.cancelRequest($(this).parents('.' + self._panelClass).attr('id'));
    return false;
  });

  // Hide all loading links to start.
  $('.' + self._loadingClass).hide();


  // First toggle closes all the menus.
  $('.menu').toggle();
  // Attach click event to open menus when the headers are clicked.
  $('.' + self._menuHeaderClass).click(function() {
    $('#' + $(this).attr('id') + '-options').toggle();
  });

  // Allows clicking anywhere outside the menu to close the menu.
  $('body').click(function() {
    if ($(event.target).parents('.' + self._menuHeaderClass).length === 0) {
      $('.menu').hide();
    }
  });

  // Attach click event to filter button.
  $('.' + self._filterButtonClass).click(function() {
    var filterPanelClass = $(this).parents('.' + self._filterPanelClass);
    var filterText = filterPanelClass.find('.' +
        self._filterTextAreaClass).val();
    var typeOverride = filterPanelClass.find('.' +
        self._filterSelectClass).val();
    self.loadPanel($(this).parents('.' + self._panelClass).attr('id'), 'list',
        typeOverride);

    // Save filter text to browser local storage if supported
    if (window.localStorage) {
      window.localStorage.setItem(
          $(this).parents('.' + self._panelClass)[0].id, filterText);
    }
    return false;
  });

  // Attach click event to the filter header.
  $('.' + self._filterPanelClass + ' > h3').click(function() {
    if ($(this).is('.active')) {
      $(this).removeClass(self._uncornerClass);
    } else {
      $(this).addClass(self._uncornerClass);
    }

    var contentPanel = $(this).parent().siblings('.' + self._panelContentClass);
    // The -10px, +10px is a hack to avoid a layout stutter when animating.
    var fudgeFactor = 10;
    var fudgeHeight = 88;
    $(contentPanel).height(($(contentPanel).height() - fudgeFactor) + 'px');
    $(contentPanel).animate(
        {'height': ($(this).is('.active') ? '+' : '-') + '=' + fudgeHeight +
            'px'},
        'fast', function() {
            $(this).height(($(contentPanel).height() +
                            fudgeFactor) + 'px')});
    var filterContentPanel =
        $(this).siblings('.' + self._filterPanelContentClass);
    filterContentPanel.slideToggle('fast');
    $(this).toggleClass('active');
  });
};

/**
 * Register a panel id and url with the PanelManager.
 * @param {string} id The id of the panel's div.
 * @param {string} url The url that should be loaded into the panel's content.
 */
dfpwebapp.PanelManager.prototype.registerPanel = function(id, url) {
  this._panels[id] = url;
};

/**
 * Find the div of a given panel, by panel id.
 * @param {string} id The id of the panel.
 * @return {Element} The div of the panel.
 */
dfpwebapp.PanelManager.prototype.findPanel = function(id) {
  return $('#' + id).get(0);
};

/**
 * Find the content div for of a given panel, by panel id.
 * @param {string} id The id of the panel.
 * @return {Element} The div of the panel's content.
 */
dfpwebapp.PanelManager.prototype.findPanelContent = function(id) {
  return $(this.findPanel(id)).children('.' + this._panelContentClass).get(0);
};

/**
 * Find the filter text area for of a given panel, by panel id.
 * @param {string} id The id of the panel.
 * @return {Element} The filter text area of the panel.
 */
dfpwebapp.PanelManager.prototype.findPanelTextArea = function(id) {
  return $(this.findPanel(id)).find('.' + this._filterTextAreaClass).get(0);
};

/**
 * Load all registered panels.
 */
dfpwebapp.PanelManager.prototype.loadAllPanels = function() {
  for (var id in this._panels) {
    this.loadPanel(id, 'default');
  }
};

/**
 * Load a panel with the content from its associated url.
 * @param {string} id The id of the panel.
 * @param {string} displayStyle The display style for the panel.
 * @param {string} typeOverride The type override used when panel can display
 *     when multiple types exist in the panel.
 */
dfpwebapp.PanelManager.prototype.loadPanel = function(id, displayStyle,
    typeOverride) {
  // Preserve reference to this PanelManager object.
  var self = this;
  var url = this._panels[id];
  var panelDiv = this.findPanel(id);
  var panelContentDiv = this.findPanelContent(id);
  var data = {};
  var textArea = this.findPanelTextArea(id);

  if (textArea) {
    data['filterText'] = textArea.value;
  } else {
    data['filterText'] = '';
  }

  if (displayStyle != null) {
    data['displayStyle'] = displayStyle;
  } else {
    data['displayStyle'] = '';
  }

  if (typeOverride != null) {
    data['typeOverride'] = typeOverride;
  } else {
    data['typeOverride'] = '';
  }

  data['reqId'] = this.getRequestId(id);

  // Set the network code on the outgoing request.
  data['networkCode'] = $('#network').val();


  // Apply the loading class.
  this.showPanelLoadingView(panelDiv);
  // Clear old content and load content from the url.
  $(panelContentDiv).html('');
  $.get(url, data).error(function() {
      $(panelDiv).removeClass(self._loadingClass);
      // Request failed, show error message.
      $(panelContentDiv).html('<p class="' + self._errorClass +
          '">Error Loading Panel.</p>');
    }
  );
};


/**
 * Bind actions to the links in the loaded panel HTML.
 * @param {string} id The id of the panel.
 */
dfpwebapp.PanelManager.prototype.bind = function(id) {
  var panelContentDiv = this.findPanelContent(id);

  // Bind details links.
  $(panelContentDiv).find(this._detailsLinkClass).click(function() {
    reference = $(this).attr('rel');
    // isOpen may return the element if the dialog was never opened, so test
    // for something other than the boolean true.
    if ($('#' + reference).dialog('isOpen') !== true) {
      $('#' + reference).dialog({
        width: 600,
        height: 600,
        title: 'Details',
        modal: true,
        autoOpen: false,
        draggable: false
      });
    }
    $('#' + reference).dialog('open');
    return false;
  });

};

/**
 * Add new data into the content area.
 *
 * @param {string} id The id of the panel to add to.
 * @param {string} type The type of the objects to be added.
 * @param {Array} objects The object to add to the content area.
 * @param {function} insertCallback The callback method to insert HTML into the
 *     content area.
 */
dfpwebapp.PanelManager.prototype.addContent = function(id, type, objects,
    insertCallback) {
  var panelContentDiv = this.findPanelContent(id);
  var panelDiv = this.findPanel(id);
  if ($(panelContentDiv).children().length === 0) {
    this.hidePanelLoadingView(panelDiv);
  }
  insertCallback($(panelContentDiv), type, objects);
  // Bind loaded links.
  this.bind(id);
};

/**
 * Add elements for loading panel.
 *
 * @param {Element} panelDiv The panel to add the elements.
 */
dfpwebapp.PanelManager.prototype.showPanelLoadingView = function(panelDiv) {
  $('#' + panelDiv.id + ' .' + this._loadingClass).show();
};

/**
 * Remove elements for loading panel.
 *
 * @param {Element} panelDiv The panel to add the elements.
 */
dfpwebapp.PanelManager.prototype.hidePanelLoadingView = function(panelDiv) {
  $('#' + panelDiv.id + ' .' + this._loadingClass).hide();
};

/**
 * Print an error message when fetch fails.
 *
 * @param {string} id The id of the panel to display message.
 * @param {string} message The message to present.
 */
dfpwebapp.PanelManager.prototype.printInfo = function(id, message) {
  var panelContentDiv = this.findPanelContent(id);
  var panelDiv = this.findPanel(id);
  this.hidePanelLoadingView(panelDiv);
  $(panelContentDiv).html(
      '<p class="' + this._infoClass + '">' + message + '</p>');
};

/**
 * Print an error message when fetch fails.
 *
 * @param {string} id The id of the panel to add to.
 * @param {string} message The error message.
 */
dfpwebapp.PanelManager.prototype.printError = function(id, message) {
  var panelContentDiv = this.findPanelContent(id);
  var panelDiv = this.findPanel(id);
  this.hidePanelLoadingView(panelDiv);
  // Request failed, show error message.
  $(panelContentDiv).html('<p class="' + this._errorClass +
      '">Error Loading Panel:\n' + message + '</p>');
};

/**
 * Get a new request ID for the panel.
 *
 * @param {string} id The id of the panel making the request.
 * @return {string} The request ID.
 */
dfpwebapp.PanelManager.prototype.getRequestId = function(id) {
  this._panelLastRequestId[id] = new Date().getTime().toString();
  return this._panelLastRequestId[id];
};

/**
 * Cancels last request for the panel.
 *
 * @param {string} id The id of the panel that made the request.
 */
dfpwebapp.PanelManager.prototype.cancelRequest = function(id) {
  this._panelLastRequestId[id] = null;
  this.hidePanelLoadingView($('#' + id)[0]);
};

/**
 * Get a new request ID for the panel.
 *
 * @param {string} id The id of the panel to check against.
 * @param {string} reqId The request ID for the incoming data.
 * @return {boolean} True if the data returned is from the latest request from
 *     that panel.
 */
dfpwebapp.PanelManager.prototype.isLatestRequest = function(id, reqId) {
  return this._panelLastRequestId[id] === reqId;
};

/**
 * Gets the request ID and removes it from the data object.
 *
 * @param {Object} data The object to get the request ID from.
 * @return {string} reqId The request for the incoming data.
 */
dfpwebapp.popRequestId = function(data) {
  var reqId = data['reqId'];
  delete data['reqId'];
  return reqId;
};

/**
 * Function for getting the keys of a JSON object.
 *
 * Sample input:
 * {
 *   'key1': 'value1',
 *   'key2': 'value2'
 * }
 *
 *Sample output:
 *['key1', 'key2']
 *
 * @param {Object} object The object to examine.
 * @return {Array} An array of the keys in the object.
 */
dfpwebapp.getKeys = function(object) {
  var keys = [];
  for (var key in object) {
     if (object.hasOwnProperty(key)) {
       keys.push(key);
     }
  }
  return keys;
};

/**
 * Callback for a channel message. This function expects a JSON object as input
 * and sends the message to the correct formatter to display the results.
 *
 * @param {string} msg The message from the server.
 */
dfpwebapp.handleChannelMessage = function(msg) {
 var data = JSON.parse(msg.data);
 var requestId = dfpwebapp.popRequestId(data);
 var keys = dfpwebapp.getKeys(data);
 if (keys.length == 1) {
   var key = keys[0];
   var id = 'dfp-panel-' + key;
   var type = key;
   var formatter = null;
   switch (key) {
     case 'error':
       var tag = 'dfp-panel-' + data[key]['tag'];
       if (panelManager.isLatestRequest(tag, requestId)) {
         panelManager.printError(tag, data[key]['message']);
         return;
       }
     case 'info':
       var tag = 'dfp-panel-' + data[key]['tag'];
       if (panelManager.isLatestRequest(tag, requestId)) {
         panelManager.printInfo(tag, data[key]['message']);
         return;
       }
     case 'networks':
       formatter = dfpwebapp.formatter.network;
       break;
     case 'pql':
       formatter = dfpwebapp.formatter.pql;
       break;
     case 'licas':
       formatter = dfpwebapp.formatter.lica;
       break;
     case 'custom-targeting-value':
       id = 'dfp-panel-custom-targeting';
       type = 'custom-targeting';
       formatter = dfpwebapp.formatter.nestedValue;
       break;
     case 'orders-li':
       id = 'dfp-panel-orders';
       type = 'orders';
       formatter = dfpwebapp.formatter.nestedValue;
       break;
     default:
       formatter = dfpwebapp.formatter.standard;
       break;
   }

   if (panelManager.isLatestRequest(id, requestId)) {
     panelManager.addContent(id, type, data[key], formatter);
   }
 }
};

/**
 * Display a dialog.
 *
 * @param {string} selector The selector of the element to create dialog from.
 * @param {string} title The dialog title.
 */
dfpwebapp.showDialog = function(selector, title) {
  $(selector).dialog({
    width: 600,
    height: 300,
    title: title,
    modal: true,
    autoOpen: true,
    draggable: false
  });
};

/**
 * Defines formatter functions to handle presentation of different objects.
 */
dfpwebapp.formatter = function() {

  /**
   * Creates a div that contains the object details (fields) in JSON format.
   *
   * @param {string} type The type of the objects to be added.
   * @param {Array} object The object to add to the content area.
   * @return {Element} The details div.
   */
  function createDetailsDiv(type, object) {
    var detailsDiv = $('<div>')
        .addClass('dfp-details')
        .attr('id', 'dfp-details-' + type + '-' + object.id);
    var preTag = $('<pre>');
    preTag.text(JSON.stringify(object, null, '  '));
    detailsDiv.append(preTag);
    return detailsDiv;
  }

  /**
   * Creates a link to open the details div.
   *
   * @param {string} type The type of the objects to be added.
   * @param {Array} object The object to add to the content area.
   * @return {Element} The link to open the details div.
   */
  function createLink(type, object) {
    return $('<a>[Details]</a>')
        .addClass('dfp-details-link')
        .attr('rel', 'dfp-details-' + type + '-' + object.id)
        .attr('href', '#');
  }


  /**
   * Formatter for line item creative association.
   *
   * @param {Element} contentDiv The jQuery element to append HTML elements to.
   * @param {string} type The type of the objects to be added.
   * @param {Array} objects The object to add to the content area.
   */
  function lica(contentDiv, type, objects) {
    if (contentDiv.children('table').length === 0) {
      contentDiv.append($('<table><tbody></tbody></table>'));
    }
    var contentContainer = $(contentDiv.find('tbody'));
    if (contentContainer.children().length === 0) {
      var element = $('<tr>')
          .append('<th>Line Item ID</th>')
          .append('<th>Creative ID</th>')
          .append('<th>Status</th>')
          .append('<th>Details</th>');
      contentContainer.append(element);
    }
    for (var i = 0; i < objects.length; i++) {
      object = objects[i];
      var element = $('<tr>')
          .append('<td>' + object.lineItemId + '</td>')
          .append('<td>' + object.creativeId + '</td>')
          .append('<td>' + object.status + '</td>');
      var detailsCell = $('<td>');
      var detailsDiv = createDetailsDiv(type, object);
      var link = createLink(type, object);
      detailsCell.append(link).append(detailsDiv);
      element.append(detailsCell);
      contentContainer.append(element);
    }
  }

  /**
   * Formatter for line items nested under orders.
   *
   * @param {Element} contentDiv The jQuery element to append HTML elements to.
   * @param {string} type The type of the objects to be added.
   * @param {Array} objects The object to add to the content area.
   */
  function nestedValue(contentDiv, type, objects) {
    var parentObject = contentDiv.find('.' + type).last();
    if (parentObject.children('ul').length === 0) {
      parentObject.append($('<ul>'));
    }
    var contentContainer = $(parentObject.children('ul'));
    for (var i = 0; i < objects.length; i++) {
      object = objects[i];
      var element = $('<li>');
      var nameSpan = $('<span>' + object.name + '</span>');
      var detailsDiv = createDetailsDiv(type, object);
      var link = createLink(type, object);
      element.append(nameSpan).append(link).append(detailsDiv);
      contentContainer.append(element);
    }
  }

  /**
   * Formatter for the network panel.
   *
   * @param {Element} contentDiv The jQuery element to append HTML elements to.
   * @param {string} type The type of the objects to be added.
   * @param {Array} objects The object to add to the content area.
   */
  function network(contentDiv, type, objects) {
    if (contentDiv.children('ul').length === 0) {
      contentDiv.append($('<ul>'));
    }
    var contentContainer = $(contentDiv.children('ul'));
    for (var i = 0; i < objects.length; i++) {
      object = objects[i];
      var element = $('<li>');
      var nameSpan = $('<span>' + object.displayName + '</span>');
      var networkCode = $('<span>(' + object.networkCode + ')</span>');
      var detailsDiv = createDetailsDiv(type, object);
      var link = createLink(type, object);
      element.append(nameSpan).append(networkCode).append(link).append(
          detailsDiv);
      contentContainer.append(element);
    }
  }

  /**
   * Formatter for PQL tables.
   *
   * @param {Element} contentDiv The jQuery element to append HTML elements to.
   * @param {string} type The type of the objects to be added.
   * @param {Array} objects The object to add to the content area.
   */
  function pql(contentDiv, type, objects) {
    if (contentDiv.children('table').length === 0) {
      contentDiv.append($('<table><tbody></tbody></table>'));
    }
    var contentContainer = $(contentDiv.find('tbody'));
    for (var i = 0; i < objects.length; i++) {
      object = objects[i];
      var element = $('<tr>');
      for (var j = 0; j < object.length; j++) {
        if (contentContainer.children().length === 0) {
          element.append('<th>' + object[j] + '</th>');
        } else {
          element.append('<td>' + object[j] + '</td>');
        }
      }
      contentContainer.append(element);
    }
  }

  /**
   * Standard formatter with the format:
   *   name [Details]
   *
   * @param {Element} contentDiv The jQuery element to append HTML elements to.
   * @param {string} type The type of the objects to be added.
   * @param {Array} objects The object to add to the content area.
   */
  function standard(contentDiv, type, objects) {
    if (contentDiv.children('ul').length === 0) {
      contentDiv.append($('<ul>'));
    }
    var contentContainer = $(contentDiv.children('ul'));
    for (var i = 0; i < objects.length; i++) {
      object = objects[i];
      var element = $('<li>').addClass(type);
      var nameSpan = $('<span>' + object.name + '</span>');
      var detailsDiv = createDetailsDiv(type, object);
      var link = createLink(type, object);
      element.append(nameSpan).append(link).append(detailsDiv);
      contentContainer.append(element);
    }
  }
  return {
      lica: lica,
      nestedValue: nestedValue,
      network: network,
      pql: pql,
      standard: standard
  };
}();
