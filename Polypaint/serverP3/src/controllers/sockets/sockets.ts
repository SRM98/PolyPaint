import { ChatController } from "./chatController";
import { MatchMakingController } from "./matchmakingController";
import { useSocketServer } from "socket-controllers";
import { InMatchController } from "./inmatchController";
import GameController from "./gameController";
import LobbyController from "./lobbyController";

export var io: SocketIO.Server;
export function startSockets(ioserver: any) {
    io = ioserver;
    useSocketServer(io, {
        controllers: [ MatchMakingController, InMatchController, GameController, LobbyController ],
    });
    useSocketServer(io, {
        controllers: [ ChatController ],
    });
}

/* 


********************MATCHMAKING***********************

################## SERVER RECEIVES #################

createMatch
    {
        creator: string,
        name: string,
        nbRounds: number,
        type: MatchTypes, (0 = Classic, 1 = Coop, 2 = Duel, 3 = Solo)
    }

editMatch
    TBM
    
getMatches
    {
        type: MatchTypes, (0 = Classic, 1 = Coop, 2 = Duel, 3 = Solo)
    }

joinMatch
    {
        username: string,
        id: string (name of the match)
    }


startMatch
    {
        id: string (id of the match)
    }
    
  
leaveMatch
    {
        username: string,
        id: string (name of the match)
    }

matchCreated
    {
        type: MatchTypes,
        content: Classic | Coop | Duel (depending on type)
    }

################## SERVER SENDS #################

getMatches
    answer of a client sending getMatches
    {
        type: MatchTypes 
        content: [ 
                    {
                        name : string
                        nbRounds : int
                        type : MatchTypes
                        creator : string
                        playerCount: int

                        *** extra attributes for Classic ***
                        teamA: string[] (includes AI player)
                        teamB: string[] (includes AI player)

                        *** extra attributes for Coop ***
                        players: string[] 
                        aiplayer: string
                        
                        *** extra attributes for Duel ***
                        player1 : string 
                        player2 : string
                    }
                ...
                ]
    }

matchmakingError
{
    description: string
}

matchEdit
    Somethings has changed in a match, all the infos from the match are resend
    {
        type: MatchTypes
        content: Classic | Coop | Duel (depending on type)
    }


matchStarted
    the match has started
    {
        type: MatchTypes
        content: string (match id)
    }



***************************************IN MATCH*************************************
only sent to sockets in the match room

Server Recieves:
draw 
    TBM

guess
    {
        guess: string
    }

leaveInMatch
    {
        username: string,
        id: string (name of the match)
    }

Server Sends:

badGuess
Message that is send when a guess is bad, if a guess is good, server sends RoundEnd
{
    triesLeft: int
}

roundStart
    {
        drawer: string       (username of the one who is drawing this round)
        roundDuration: number   
        isGuessing: bool
    }

reply
**Same structure as roundStart

roundEnd
    {
        message: string
        teamA: int (points of teamA in Classic, or points of team in Coop, or points of player1 in Duel)
        teamB: int (points of teamB in Classic, always 0 in Coop, or points of player2 in Duel)
    }

matchEnd
    {
        reason: number  ( 0 = player left, 1 = normal end)
        winner: string  (winner's username or empty game ended early)
    }

matchEndedWinner
    the match has ended
    {
        message: string
    }

matchEndedLooser
    the match has ended
    {
        message: string
    }


*/