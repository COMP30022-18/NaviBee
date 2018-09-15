package au.edu.unimelb.eng.navibee.social;

import java.util.Date;

public class GroupConversation extends Conversation {

    private String name;
    private String icon;

    public GroupConversation(String id, Date readTimestamp, String name, String icon) {
        super(id, readTimestamp);
        this.name = name;
        this.icon = icon;
    }

    public String getName(){
        return this.name;
    }

    public String getIcon(){
        return this.icon;
    }

    @Override
    protected void newUnreadMsg(Message msg) {
    }
}