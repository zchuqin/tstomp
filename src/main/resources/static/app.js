var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
//    var socket = new SockJS('http://10.10.27.119:8082/ws');
    var socket = new SockJS('http://127.0.0.1:8087/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/public', function (greeting) {
            showGreeting(JSON.parse(greeting.body).name);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    var name = $("#name").val();
    stompClient.subscribe('/topic/' + name, function (data) {
        showGreeting(JSON.parse(data.body).name);
    });
    stompClient.send("/app/good/" + name, {}, JSON.stringify({'name': $("#target").val()}));
}

function sendName2() {
    var name = $("#name").val();
    stompClient.send("/app/good/all/" + name, {}, JSON.stringify({'name': $("#target").val()}));
}

function sendName3() {
    var name = $("#name").val();
    stompClient.send("/app/good/one/" + name, {}, JSON.stringify({'name': $("#target").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
    $( "#send2" ).click(function() { sendName2(); });
    $( "#send3" ).click(function() { sendName3(); });
});

