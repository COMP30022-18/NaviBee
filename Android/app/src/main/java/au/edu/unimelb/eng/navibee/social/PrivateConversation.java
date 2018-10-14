package au.edu.unimelb.eng.navibee.social;

import java.util.Date;

public class PrivateConversation extends Conversation {

    private String targetUid;

    public PrivateConversation(String id, Date readTimestamp, Date createTimestamp, String targetUid) {
        super(id, readTimestamp, createTimestamp);
        this.targetUid = targetUid;
    }

    public String getTargetUid() {
        return targetUid;
    }

    @Override
    protected void newUnreadMsg(Message msg) {
        // check new voice call
        if (msg.getType().equals("voicecall")) {
            long dif = new Date().getTime() - msg.getTime_().getTime();
            if (dif < VoiceCallService.VOCIECALL_EXPIRE) {
                // new voice call coming
                VoiceCallService.getInstance().setup(this, msg, msg.getSender().equals(uid));
            }
        }
    }

}
