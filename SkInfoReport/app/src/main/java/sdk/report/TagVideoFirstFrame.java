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
            strCtx.put("framesize", getFrameSize());  // get from app local - nono
            strCtx.put("width", getResoWidth());      // get from app local - nono
            strCtx.put("height", getResoHeight());    // get from app local - nono
            strCtx.put("fps", getVidFps());           // get from app local - nono
            strCtx.put("bitrates", getBitRates());    // get from app local - nono
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
