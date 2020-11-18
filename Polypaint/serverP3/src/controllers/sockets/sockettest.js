//client.js

var io = require('socket.io-client');
var socket = io.connect('http://localhost:4200', { reconnect: true});

var username;
// Add a connect listener
const readline = require('readline').createInterface({
    input: process.stdin,
    output: process.stdout
});

socket.on('connect', function (socket) {
    console.log('Connected!');
    readline.question('Enter Usename: ', (answer) => {
        // TODO: Log the answer in a database
        username = answer
        console.log("username is " + username);
        
      });
});


socket.on("message", (res) => {console.log(res.sender + " (" + res.room + ") " + Date(res.time) + " : " + res.text );
});
socket.on("roomMessages", (res) => {console.log(res); });
socket.on("getMatches", (res) => {console.log(res); });
socket.on("matchCreated", (res) => {console.log('gameCreated: ' + res); });
socket.on("badWord", () => {console.log("Hey you silly, silly bad boy"); });
socket.on("imageSearchResults", (res) => {console.log(res); });

socket.emit('createMatch', { name : "test",  nbRounds : 4, maxTime : 60, type : 0, creator : "bob1" } );
socket.emit('createMatch', { name : "test2", nbRounds : 4, maxTime : 60, type : 0, creator : "jean1" } );
socket.emit('createMatch', { name : "test3", nbRounds : 4, maxTime : 60, type : 0, creator : "jeanguy1" } );
socket.emit('createMatch', { name : "test4", nbRounds : 4, maxTime : 60, type : 0, creator : "roland1" } );
socket.emit('createMatch', { name : "test5", nbRounds : 4, maxTime : 60, type : 0, creator : "grostas1" } );
socket.emit('createMatch', { name : "test6", nbRounds : 4, maxTime : 60, type : 0, creator : "non toi tes gros1" } );

socket.emit('createMatch', { name : "test7",  nbRounds : 4, maxTime : 60, type : 1, creator : "bob2" } );
socket.emit('createMatch', { name : "test8", nbRounds : 4, maxTime : 60, type : 1, creator : "jean2" } );
socket.emit('createMatch', { name : "test9", nbRounds : 4, maxTime : 60, type : 1, creator : "jeanguy2" } );
socket.emit('createMatch', { name : "test10", nbRounds : 4, maxTime : 60, type : 1, creator : "roland2" } );
socket.emit('createMatch', { name : "test11", nbRounds : 4, maxTime : 60, type : 1, creator : "grostas2" } );
socket.emit('createMatch', { name : "test12", nbRounds : 4, maxTime : 60, type : 1, creator : "non2" } );

socket.emit('createMatch', { name : "test13",  nbRounds : 4, maxTime : 60, type : 2, creator : "bob3" } );
socket.emit('createMatch', { name : "test14", nbRounds : 4, maxTime : 60, type : 2, creator : "jean3" } );
socket.emit('createMatch', { name : "test15", nbRounds : 4, maxTime : 60, type : 2, creator : "jeanguy3" } );
socket.emit('createMatch', { name : "test16", nbRounds : 4, maxTime : 60, type : 2, creator : "roland3" } );
socket.emit('createMatch', { name : "test17", nbRounds : 4, maxTime : 60, type : 2, creator : "grostas3" } );
socket.emit('createMatch', { name : "test18", nbRounds : 4, maxTime : 60, type : 2, creator : "non3" } );


readline.on('line', (input) => {
    let splitted = input.split(" ");
    
    if (splitted[0] == "1" ) {
        socket.emit('joinMatch', { username: "roland bob", name: "test", type: 0 } );
    }

    if (splitted[0] == "2" ) {
        socket.emit('leaveMatch', { username: "roland bob", name: "test", type: 0 } );
    }

    if (splitted[0] == "3" ) {
        socket.emit('leaveMatch', { username: "bob1", name: "test", type: 0 } );
    }

});
  