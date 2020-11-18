import "reflect-metadata";
import { Container } from "inversify";
import Types from "./types";
import { Server } from "./server";

const container: Container = new Container();

container.bind(Types.Server).to(Server);

export { container };
