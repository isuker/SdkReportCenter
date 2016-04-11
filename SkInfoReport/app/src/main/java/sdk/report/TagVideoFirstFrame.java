package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagVideoFirstFrame {
    int frameSize;
    int resoWidth;
    int resoHeight;
    int vidFps;
    int bitRates;

    public int getFrameSize() {
        return frameSize;
    }

    public int getResoWidth() {
        return resoWidth;
    }

    public int getResoHeight() {
        return resoHeight;
    }

    public int getVidFps() {
        return vidFps;
    }

    public int getBitRates() {
        return bitRates;
    }

    public JSONObject toJson() {
        JSONObject strCtx = new JSONObject();
        try {
            strCtx.put("framesize", getFrameSize());
            strCtx.put("width", getResoWidth());
            strCtx.put("height", getResoHeight());
            strCtx.put("fps", getVidFps());
            strCtx.put("bitrates", getBitRates());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strCtx;
    }

    public void setData(int frameSz, int width, int height, int fps, int bitRt) {
        frameSize = frameSz;
        resoWidth = width;
        resoHeight = height;
        vidFps = fps;
        bitRates = bitRt;
    }
}
