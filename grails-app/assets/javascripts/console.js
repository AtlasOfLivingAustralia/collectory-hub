/*
 * Copyright (C) 2016 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 * 
 * Created by Temi on 31/07/2016.
 */
/**
 * get messages from jenkin's server.
 */
function fetchMessages() {
    var interimUrl = url + nextStart,
        delay = 2000
    $.ajax({
        url: interimUrl,
        accepts: {
            json: 'application/json'
        },
        dataType: 'json',
        success: function (data, resp) {
            $("#console-output").append(data.text)
            followLog();
            if (data.isMoreData) {
                nextStart = data.nextStart
                $("#console-loading").removeClass('hide')
                setTimeout(fetchMessages, delay)
            } else {
                $("#console-loading").addClass('hide')
            }
        }
    });
}

/**
 * go to the last log line
 */
function followLog() {
    var position = $("#last-line").offset().top - $(window).height()
    follow && $('html,body').animate({scrollTop: position}, 'slow');
}

/**
 * Top button's click event handler.
 * @returns {boolean}
 */
function reachTop() {
    $('html,body').animate({scrollTop: $(".console-top").offset().top - headerHeight}, 'slow');
    return false
}

/**
 * Follow log button's click event handler.
 * @returns {boolean}
 */
function setFollow() {
    follow = true
    followLog()
    return false
}

/**
 * Position the tail button to the top of screen so that it is visible to user
 * @returns {*}
 */
function followButtonPosition() {
    var limit, top, follow;
    follow = $('#follow');
    if (follow.length) {
        top = $(window).scrollTop() + headerHeight - $('#console-output').offset().top;
        limit = $('#console-output').height() - $('#follow').height() + 5;
        if (top > limit) {
            top = limit;
        }

        if (top > 0) {
            return follow.css({ top: top });
        } else {
            return follow.css({ top: 0 });
        }
    }
}

/**
 * position the top button to the bottom of screen but visible to users
 */
function topButtonPosition() {
    var console, consoleHeight, bottom, winHeight;
    var el = $('#top');
    console = $('#console-content');
    if (el.length) {
        consoleHeight = console.outerHeight();
        winHeight = $(window).height();
        bottom = console.offset().top + consoleHeight - ( $(window).scrollTop() + winHeight);

        if (bottom > 0) {
            el.css({bottom: bottom});
        } else {
            el.css({bottom: 0});
        }
    }
};