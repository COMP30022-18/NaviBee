import * as functions from 'firebase-functions'
import { messaging } from 'firebase-admin'; // used for type only
import { db, msg} from './../helper/admin';

export const newMessageNotification = functions.firestore
    .document('/conversations/{conversationID}/messages/{messageID}')
    .onCreate(async (snap, context) => {
        const convID = context.params.conversationID;
        const doc = snap.data();

        console.log("convID: " + convID + " msgID: " +  context.params.messageID);

        // get sender and receiver
        const sender = doc.sender;
        const convDoc = (await db.collection('conversations').doc(convID).get()).data();
        let receiver = "";
        for (let key in convDoc.users) {
            // do something with key|value
            if (key!=sender) {
                receiver = key;
            }
        }
        // console.log("receiver: " + receiver);

        // get sender name
        const senderDoc = (await db.collection('users').doc(sender).get()).data();
        const senderName = senderDoc.name;

        // generate notification text
        let content = "";
        if (doc.type == "text") {
            content = doc.data;
            if (content.length>40) {
                content = content.substring(0, 40) + "...";
            }
        } else if (doc.type == "image") {
            content = "[Photo]"
        }

        // android payload
        // not declare type will case error (?)
        let android: messaging.AndroidConfig = {
            priority: 'high',
            notification: {
                sound: 'default',
                title: senderName,
                body: content
            }
        }

        // find all tokens and send notification
        let tokens = await db.collection('fcmTokens')
                        .where('uid', '==', receiver).get();

        tokens.forEach( tokenDoc => {
            let token = tokenDoc.id;
            try {
                msg.send({token: token, android: android});
            } catch(Error) {
                // token is invalid
            }
        });

    });

export const addFriend = functions.https.onCall(
    async (data, context) => {
        const uid = context.auth.uid;

        if (uid==null) {
            return {code:-1, msg:"need login"};
        }

        const targetUid = data.targetUid;

        if (uid==targetUid) {
            return {code:-1, msg:"can not add yourself as a friend"};
        }

        let doc = await db.collection('users').doc(targetUid).get();
        if (!doc.exists) {
            return {code:-1, msg:"target user not exists"};
        }

        let convDoc = await db.collection('conversations')
                            .where('users.' + uid, '==', true)
                            .where('users.' + targetUid, '==', true)
                            .where('type', '==', 'private')
                            .get();

        if (!convDoc.empty) {
            return {code:-1, msg:"have been friends already"};
        }

        // set up private conversations
        // private conversation holds relationship
        let conv = {};
        conv['users'] = {};
        conv['users'][uid] = true;
        conv['users'][targetUid] = true;
        conv['type'] = 'private';
        conv['readTimestamps'] = {};
        conv['readTimestamps'][uid] = new Date();
        conv['readTimestamps'][targetUid] = new Date();
        db.collection('conversations').add(conv);

        return {code: 0};
    });

export const createGroupChat = functions.https.onCall(
    async (data, context) => {
        const uid = context.auth.uid;

        if (uid==null) {
            return {code:-1, msg:"need login"};
        }

        let conv = {};
        conv['type'] = 'group';
        conv['creator'] = uid;
        conv['name'] = data.name;
        conv['icon'] = data.icon
        // add all users
        conv['users'] = {};
        conv['readTimestamps'] = {};
        conv['users'][uid] = true;
        conv['readTimestamps'][uid] = new Date();
        for (let entry of data.users) {
            conv['users'][entry] = true;
            conv['readTimestamps'][entry] = new Date();
        }
        db.collection('conversations').add(conv);
        return {code: 0};
    });
