import * as functions from 'firebase-functions'
import { db } from './../helper/admin';

export const initNewUser = functions.auth.user().onCreate((user) => {
    // write init data for a new user
    let data = {
        name: user.displayName,
        email: user.email,
        photoURL: user.photoURL,
        contacts: {}
    };

    let userRef = db.collection('users').doc(user.uid); // user doc reference
    userRef.set(data);
    return 0;
});


export const getUserInfoFromUidList = functions.https.onCall(
    async (data, context) => {
        const result = {};

        for (let uid of data.uidList) {
            let userDoc = (await db.collection('users').doc(uid).get()).data();
            result[uid] = {};
            result[uid]['name'] = userDoc.name;
            result[uid]['photoURL'] = userDoc.photoURL;
        }

        return result;
    });
