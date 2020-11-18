import sort from 'fast-sort';

export default class PanoramiqueImageCreator {
    private static async applyPanoramique(svg: any, index: string, checkForMin: boolean): Promise<any> {
        
        return checkForMin ? sort(svg).asc((path) => {  if(path.path[0][index] == undefined ) console.log("bru");
         ; return Math.pow(path.path[0][index], 2);  }) : 
                             sort(svg).desc((path) => Math.pow(path.path[0][index], 2)) ;
    }

    public static async applyPanoramiqueImageTop(svg: any) {
        const indexToCheck = "y";
        const checkForMin = true;
        
        return await this.applyPanoramique(svg, indexToCheck, checkForMin);
    }
    public static async applyPanoramiqueImageBottom(svg: any) {
        const indexToCheck = "y";
        const checkForMin = false;
        return await this.applyPanoramique(svg, indexToCheck, checkForMin);
    }
    public static async applyPanoramiqueImageLeft(svg: any) {
        const indexToCheck = "x";
        const checkForMin = true;
        return await this.applyPanoramique(svg, indexToCheck, checkForMin);
    }
    public static async applyPanoramiqueImageRight(svg: any) {
        const indexToCheck = "x";
        const checkForMin = false;
        return await this.applyPanoramique(svg, indexToCheck, checkForMin);
    }
}