package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagVideoFirstFrame {
    private ReportCenter rcCtx = null;

    public TagVideoFirstFrame(ReportCenter rc) {
        rcCtx = rc;
    }

    public JSONObject toJson() {
        JSONObject strCtx = new JSONObject();
        ParamMediaInfo mediaIf = rcCtx.getmediaPara();
        try {
            //strCtx.put("framesize", rcCtx.getmediaPara().getFrameSize());  // get from app local - nono
            strCtx.put("width", mediaIf.getWidth());      // get from app local - nono
            strCtx.put("height", mediaIf.getHeight());    // get from app local - nono
            strCtx.put("fps", mediaIf.getFps());           // get from app local - nono
            strCtx.put("bitrates", mediaIf.getBitRates());    // get from app local - nono
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strCtx;
    }


}
