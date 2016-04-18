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
        JSONObject jsnObj = new JSONObject();
        ParamMediaInfo mediaIf = rcCtx.getmediaPara();
        try {
            jsnObj.put("framesize", mediaIf.getFrameSize());  // get from app local - nono
            jsnObj.put("width", mediaIf.getWidth());      // get from app local - nono
            jsnObj.put("height", mediaIf.getHeight());    // get from app local - nono
            jsnObj.put("fps", mediaIf.getFps());           // get from app local - nono
            jsnObj.put("bitrates", mediaIf.getBitRates());    // get from app local - nono
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsnObj;
    }
}
