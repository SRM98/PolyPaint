import "reflect-metadata";
import express from "express";
import { injectable } from "inversify";
import * as MainConfig from "./mainConfig.json";
import * as http from "http";
import morgan from "morgan";

import { useExpressServer } from "routing-controllers";
import { useSocketServer } from "socket-controllers";

import { startSockets } from "./controllers/sockets/sockets"
import AccountController from "./controllers/http/accountController";

@injectable()
export class Server {
    private server: http.Server;
    private app : express.Application;

    public constructor() {
        this.app = express();
        var bodyParser = require('body-parser');
        this.app.use(bodyParser.json({limit: "50mb"}));
        this.app.use(bodyParser.urlencoded({limit: "50mb", extended: true, parameterLimit:50000}));
        this.app.use(morgan("dev"));
        this.app.use(express.static('src/public'));
    }

    public init(): void {
        this.app.set("port", MainConfig.port);
        this.server = http.createServer(this.app);
        this.server.timeout = MainConfig.REQUEST_TIMEOUT;
        this.server.on("error", (error) => console.log(error)
        );
        const io: SocketIO.Server = require("socket.io")(this.server);

        startSockets(io);

        useExpressServer(this.app, {
            controllers: [AccountController] 
        });
        this.server.listen(MainConfig.port);
        console.log("Listening ... ");
    }
}
