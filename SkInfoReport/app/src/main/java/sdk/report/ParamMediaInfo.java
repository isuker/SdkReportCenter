package sdk.report;

/**
 * Created by suker on 16-4-13.
 */
public class ParamMediaInfo {
    private int frameSize;
    private int width;
    private int height;
    private int fps;
    private int bitRates;

    public int getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getBitRates() {
        return bitRates;
    }

    public void setBitRates(int bitRates) {
        this.bitRates = bitRates;
    }
}
