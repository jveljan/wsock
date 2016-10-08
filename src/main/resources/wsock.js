(function(window) {

  'use strict';

  var Wsock = function(path, token) {
    if(!path) {
      path = '/wsock';
    }
    var wsUrl = isWsUrl(path) ? path : getWsUrl(path);

    var messageId = 10001;
    var connection = null;
    var listeners = {};


    var HEARTBEATFREQ = 25000;
    var me = this;
    function sendHeardBeat() {
      me.send('$hb');
    }
    var hbtimer = null;
    function scheduleHeartBeat() {
      if(hbtimer) {
        clearTimeout(hbtimer);
      }
      hbtimer = setTimeout(function() {
        hbtimer = null;
        sendHeardBeat();
      }, HEARTBEATFREQ);
    }

    this.connect = function(onSuccess, onError) {
         connection = new WebSocket(wsUrl + '?token='+token);
         connection.onopen = function() {
           scheduleHeartBeat();
           onSuccess.apply(this, arguments);
         };
         connection.onerror = function (error) {
           //TODO: handle error
           console.log('WebSocket Error ', error);
           onError.apply(this, arguments);
         };
         connection.onclose = function(e) {
           //TODO: try to reconnect
           console.log('WebSocket Connection closed ', e);
           onError.apply(this, arguments);
         };
         // Log messages from the server
         connection.onmessage = function (e) {
           scheduleHeartBeat();
           var event = JSON.parse(e.data);
           var ls = listeners[event.channel];
           if(ls) {
             listeners[event.channel] = ls.filter(function(a) {
               a.handler(event.data);
               return !a.removeAfterRecv;
             });
             if(listeners[event.channel].length === 0) {
               delete listeners[event.channel];
             }
           } else {
             console.log('No handler for event: ', event);
           }
         };
      };
      this.on = function(path, cb, oneTime) {
        if(!listeners[path]) {
          listeners[path] = [];
        }
        listeners[path].push({
          handler: cb,
          removeAfterRecv: (oneTime === true)
        });
      },

      this.send = function(path, data, cb) {
        scheduleHeartBeat();
        if(path.startsWith('$')) {
          connection.send(path);
          return;
        }

        if(undefined === data) {
          data = {};
        }
        var req = {
          path: path,
          messageId: messageId++,
          data: data
        };
        if(cb) {
          this.on(path+'#'+req.messageId, cb, true);
        }
        connection.send(JSON.stringify(req));
      }
  }

  function isWsUrl(path) {
    return path.startsWith('ws://') || path.startsWith('wss://')
  }

  function getWsUrl(path) {
    var protocol = window.location.protocol==='http:' ? 'ws:' : 'wss:';
    var host = window.location.hostname;
    var port = window.location.port;
    var url = protocol + '//' + host;
    if(port) {
      url += ':' + port;
    }
    if(!path.startsWith('/')) {
      path = '/' + path;
    }
    return url + path;
  }

  //expose global Wsock
  var existingWsock = window.Wsock;
  Wsock.noConflict = function() {
   window.Wsock = existingWsock;
   return Wsock;
  }
  window.Wsock = Wsock;

})(window);