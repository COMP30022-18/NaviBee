import * as functions from 'firebase-functions'
import { db } from './../helper/db';

export const getFriendList = functions.https.onCall(
    async (data, context) => {
    const uid = context.auth.uid;
    const usersRef = db.collection('users');
    let queryRef = usersRef.where('contacts.'+uid, '==', true);
    const snapshot = await queryRef.get();
    let list = [];
    snapshot.forEach(doc => {
        list.push({
            id: doc.id,
            name: doc.data().name,
            email: doc.data().email,
            photoURL: doc.data().photoURL
        });
    });

    return {list: list};
});
