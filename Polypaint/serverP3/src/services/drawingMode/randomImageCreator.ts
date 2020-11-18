
export default class RandomImageCreator {

    public static applyRandomImage(svg: any) {
        let paths = svg;
        for (let i = 0; i < paths.length; ++i) {
            let j = Math.floor(Math.random() * (paths.length - i - 1));
            let temp = paths[i];
            paths[i] = paths[j];
            paths[j] = temp;
        }
        return paths;
    }
}