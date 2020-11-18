import "reflect-metadata";
import { container } from "./inversify.config";
import { Server } from "./server";
import Types from "./types";

export const server: Server = container.get<Server>(Types.Server);

try {
    server.init();
} catch (e) {
    console.log(e);
}