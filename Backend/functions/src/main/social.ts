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
        if (convDoc.isDeleted) {
            return;
        }

        // get sender name
        const senderDoc = (await db.collection('users').doc(sender).get()).data();
        const senderName = senderDoc.name;

        // android payload
        // not declare type will case error (?)
        let android: messaging.AndroidConfig = {
            priority: 'high',
        }

        const chatName = convDoc.type == "group" ? convDoc.name : "";

        let data = {
            'convID': convID,
            'convType': convDoc.type,
            'msgID': context.params.messageID,
            'type': doc.type,
            'senderName': senderName,
            'senderAvatar': senderDoc.photoURL,
            'chatName': chatName
        }

        if (doc.type == "text") {
            let content = doc.data;
            if (content.length > 40) {
                content = content.substring(0, 40) + "...";
            }
            data['content'] = content;
        } else if (doc.type == "image") {
            data['content'] = doc.data;
        }

        for (let key in convDoc.users) {
            // for all users of this conversation
            if (key != sender) {
                let receiver = key;

                // find all tokens and send notification
                let tokens = await db.collection('fcmTokens')
                                .where('uid', '==', receiver).get();

                tokens.forEach( tokenDoc => {
                    let token = tokenDoc.id;
                    try {
                        msg.send({token: token, android: android, data: data});
                    } catch(Error) {
                        // token is invalid
                    }
                });
            }
        }

    });

export const addFriend = functions.https.onCall(
    async (data, context) => {
        const uid = context.auth.uid;

        if (uid == null) {
            return {code: -1, msg: "need login"};
        }

        const targetUid = data.targetUid;

        if (uid == targetUid) {
            return {code: -1, msg: "can not add yourself as a friend"};
        }

        let doc = await db.collection('users').doc(targetUid).get();
        if (!doc.exists) {
            return {code: -1, msg: "target user not exists"};
        }

        let convDoc = await db.collection('conversations')
                            .where('isDeleted', '==', false)
                            .where('users.' + uid, '==', true)
                            .where('users.' + targetUid, '==', true)
                            .where('type', '==', 'private')
                            .get();

        if (!convDoc.empty) {
            return {code: -1, msg: "have been friends already"};
        }

        // set up private conversations
        // private conversation holds relationship
        let conv = {};
        conv['isDeleted'] = false;
        conv['users'] = {};
        conv['users'][uid] = true;
        conv['users'][targetUid] = true;
        conv['type'] = 'private';
        conv['readTimestamps'] = {};
        conv['readTimestamps'][uid] = new Date();
        conv['readTimestamps'][targetUid] = new Date();
        conv['createTimestamp'] = new Date();
        await db.collection('conversations').add(conv);

        return {code: 0};
    });

export const createGroupChat = functions.https.onCall(
    async (data, context) => {
        const uid = context.auth.uid;

        if (uid == null) {
            return {code: -1, msg: "need login"};
        }

        let conv = {};
        conv['isDeleted'] = false;
        conv['type'] = 'group';
        conv['creator'] = uid;
        conv['name'] = data.name;
        conv['icon'] = data.icon
        // add all users
        conv['users'] = {};
        conv['readTimestamps'] = {};
        conv['users'][uid] = true;
        conv['readTimestamps'][uid] = new Date();
        conv['createTimestamp'] = new Date();
        for (let entry of data.users) {
            conv['users'][entry] = true;
            conv['readTimestamps'][entry] = new Date();
        }
        await db.collection('conversations').add(conv);
        return {code: 0};
    });
