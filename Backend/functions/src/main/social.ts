import * as functions from 'firebase-functions'
import { db } from './../helper/db';

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

        if (doc.data().contacts[uid]) {
            return {code:-1, msg:"have been friends already"};
        }

        // set contacts
        let up = {};
        up['contacts.'+uid] = true;
        db.collection('users').doc(targetUid).update(up);
        up = {};
        up["contacts."+targetUid] = true;
        db.collection('users').doc(uid).update(up);

        // set up conversations
        let conv = {};
        conv['users'] = {};
        conv['users'][uid] = true;
        conv['users'][targetUid] = true;
        db.collection('conversations').add(conv);

        return {code: 0};
    });
