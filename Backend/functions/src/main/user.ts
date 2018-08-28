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
