import sort from 'fast-sort';

export default class CenteredImageCreator {

    private static async applyCenteredImage(svg: any, goingOut: boolean) {
        let distanceFromCenter = (pt: any, width: number, height: number) => {
            return Math.pow(pt.x - width/2.0, 2) + Math.pow(pt.y - height/2.0, 2);
        }
        return goingOut ? sort(svg).asc((stroke) => distanceFromCenter(stroke.path[0], stroke.imageWidth, stroke.imageHeight))  : 
                             sort(svg).desc((stroke) => distanceFromCenter(stroke.path[0], stroke.imageWidth, stroke.imageHeight)) ;
    }

    public static async applyCenteredImageGoingOut(svg: any) {
        return await this.applyCenteredImage(svg, true);
    }

    public static async applyCenteredImageGoingIn(svg: any) {
        return await this.applyCenteredImage(svg, false);
    }

}