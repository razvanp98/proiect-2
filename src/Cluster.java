public class Cluster {
    private int uid;
    private int pCount;
    private int red;
    private int green;
    private int blue;
    private int redSum;
    private int greenSum;
    private int blueSum;

    public Cluster() {
        this(0, 0);
    }

    public Cluster(int uid, int color) {
        int red =   (color) >> 16    & 0x000000FF;
        int green = (color) >> 8     & 0x000000FF;
        int blue =  (color)          & 0x000000FF;

        this.red = red;
        this.green = green;
        this.blue = blue;
        this.uid = uid;

        this.addColor(color);
    }

    public int getUid() {
        return this.uid;
    }

    public int getRGB() {
        int red = this.redSum / this.pCount;
        int green = this.greenSum / this.pCount;
        int blue = this.blueSum / this.pCount;

        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    public void addColor(int color) {
        int red =   (color >> 16)   & 0x000000FF;
        int green = (color >> 8)    & 0x000000FF;
        int blue =  (color)         & 0x000000FF;

        this.redSum += red;
        this.greenSum += green;
        this.blueSum += blue;

        this.pCount++;

        this.red   = this.redSum / this.pCount;
        this.green = this.greenSum / this.pCount;
        this.blue  = this.blueSum / this.pCount;
    }

    public void removeColor(int color) {
        int red =   (color >> 16)   & 0x000000FF;
        int green = (color >> 8)    & 0x000000FF;
        int blue =  (color)         & 0x000000FF;

        this.redSum -= red;
        this.greenSum -= green;
        this.blueSum -= blue;

        this.pCount--;

        this.red   = this.redSum / this.pCount;
        this.green = this.greenSum / this.pCount;
        this.blue  = this.blueSum / this.pCount;
    }

    public double getDistance(int color) {
        int red =   (color >> 16)   & 0x000000FF;
        int green = (color >> 8)    & 0x000000FF;
        int blue =  (color)         & 0x000000FF;

        double rx = Math.pow(this.red - red, 2);
        double gx = Math.pow(this.green - green, 2);
        double bx = Math.pow(this.blue - blue, 2);

        return Math.sqrt((rx + gx + bx));
    }
}