import { accountDB } from "../database/accountDB";
import * as fs from 'fs';
import * as path from 'path';
import { ai, AIPersonnality } from "../services/players/Player";

export var avatarsDictionnary: Map<string, string>;
export async function initializeMap(ioserver: any) {
    clearImageFolder();
    avatarsDictionnary = new Map();
    let avatarsImageData: Map<string, string> =  await accountDB.getAllAvatars();
    avatarsImageData.forEach((dataImage: string, username: string) => {
        updateAvatarUrl(username, dataImage);
    });
    console.log("all avatars images fetched");   
    ai.forEach((personnality: AIPersonnality) => avatarsDictionnary.set(personnality.username, personnality.url))
}

export function saveImage(avatarPath: string, data: any) {
    var bitmap = new Buffer(data, 'base64');
    fs.writeFileSync("src/public/" + avatarPath, bitmap);
}

export function clearImageFolder() {
    const directory = 'src/public/images';

    fs.readdir(directory, (err, files) => {
    if (err) throw err;
    for (const file of files) {
        if (file.substr(file.lastIndexOf('.') + 1) !== "gitignore") {
            fs.unlink(path.join(directory, file), err => {
                if (err) throw err;
                });
            }
        }
    });
}

export function updateAvatarUrl(username: string, dataImage: any) {
    if (dataImage != null) {
        let avatarPath = "images/" + username + Date.now() + ".jpg";
        try {
            saveImage(avatarPath, dataImage);
            avatarsDictionnary.set(username, avatarPath);
        } catch (e) {
            console.log(e)
        }
    } else {
        avatarsDictionnary.set(username, "imagesStatic/defaultAvatar.png");
    }
}

export function removeImage(username: string) {
    if (avatarsDictionnary.get(username) != null) {
        fs.unlink('src/public/' + avatarsDictionnary.get(username), err => {
            if (err) throw err;
            });
    }
    
}
