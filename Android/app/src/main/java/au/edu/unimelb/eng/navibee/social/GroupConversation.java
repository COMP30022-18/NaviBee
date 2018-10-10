package au.edu.unimelb.eng.navibee.social;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import au.edu.unimelb.eng.navibee.utils.HashUtilitiesKt;
import jdenticon.Jdenticon;

public class GroupConversation extends Conversation {

    private String name;
    private String icon; // not using
    private Bitmap iconBitmap;
    private ArrayList<String> members =  new ArrayList<>();
    private String creator;
    private Date createDate;

    public GroupConversation(String id, Date readTimestamp, Date createTimestamp, String name, String icon, Map<String, Boolean> users, String creator) {
        super(id, readTimestamp, createTimestamp);
        this.name = name;
        this.icon = icon;
        for (String user:users.keySet()){
            members.add(user);
        }
        this.creator = creator;
        this.createDate = createTimestamp;

        // generate icon using conversation id
        try {
            String hash = HashUtilitiesKt.sha256String(id);
            String svgString = Jdenticon.Companion.toSvg(hash, 256, 0.08f);
            iconBitmap = imageFromString(svgString);
        } catch (SVGParseException e) {
            iconBitmap = null;
        }

    }

    public String getName(){
        return this.name;
    }

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    public ArrayList<String> getMembers(){ return this.members; }

    public String getCreator() { return this.creator; }

    public Date getCreateDate() { return this.createDate; }


    private static Bitmap imageFromString(String svgAsString) throws SVGParseException {

        SVG svg = SVG.getFromString(svgAsString);

        // Create a bitmap and canvas to draw onto
        float   svgWidth = (svg.getDocumentWidth() != -1) ? svg.getDocumentWidth() : 500f;
        float   svgHeight = (svg.getDocumentHeight() != -1) ? svg.getDocumentHeight() : 500f;

        Bitmap  newBM = Bitmap.createBitmap(Math.round(svgWidth),
                Math.round(svgHeight),
                Bitmap.Config.ARGB_8888);
        Canvas bmcanvas = new Canvas(newBM);

        // Clear background to white if you want
        bmcanvas.drawRGB(255, 255, 255);

        // Render our document onto our canvas
        svg.renderToCanvas(bmcanvas);

        return newBM;
    }


    @Override
    protected void newUnreadMsg(Message msg) {
    }
}
