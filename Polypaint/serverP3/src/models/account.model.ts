import Stats from "./stats.model"

export default interface Account {
    firstName: string,
    lastName: string,
    username: string,
    password: string,
    imageData: any[],
    avatarUrl: string,
    firstTimeUserFatClient: Boolean;
    firstTimeUserThinClient: Boolean;
    joinedRooms: String[],
    stats: Stats
}