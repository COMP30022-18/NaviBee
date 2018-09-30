package au.edu.unimelb.eng.navibee.social;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class GroupConversation extends Conversation {

    private String name;
    private String icon;
    private ArrayList<String> members =  new ArrayList<>();

    public GroupConversation(String id, Date readTimestamp, Date createTimestamp, String name, String icon, Map<String, Boolean> users) {
        super(id, readTimestamp, createTimestamp);
        this.name = name;
        this.icon = icon;
        for (String user:users.keySet()){
            members.add(user);
        }
    }

    public String getName(){
        return this.name;
    }

    public String getIcon(){
        return this.icon;
    }

    public ArrayList<String> getMembers(){ return this.members; }

    @Override
    protected void newUnreadMsg(Message msg) {
    }
}
