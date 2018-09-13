package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;

import java.util.Date;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;

public class PrivateConversation extends Conversation {

    private String targetUid;

    public PrivateConversation(String id, Date readTimestamp, String targetUid) {
        super(id, readTimestamp);
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
            if (dif < VoiceCallActivity.VOCIECALL_EXPIRE) {
                // new voice call coming

                if (!VoiceCallActivity.isWorking()) {
                    Intent intent = new Intent(NaviBeeApplication.getInstance().getApplicationContext(),
                            VoiceCallActivity.class);
                    intent.putExtra("INITIATOR", msg.getSender().equals(uid));
                    intent.putExtra("CONV_ID", conversationId);
                    intent.putExtra("MSG_ID", msg.getId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    NaviBeeApplication.getInstance().startActivity(intent);
                } else {
                    // todo: handle busy case
                }

            }
        }
    }

}
