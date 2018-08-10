const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);
var db = admin.firestore();


exports.initNewUser = functions.auth.user().onCreate(event => {
    // use event instead of event.data
    const uid = event.uid;

    // write init data for a new user
    let data = {
        name: event.displayName,
        email: event.email,
        photoURL: event.photoURL,
        contacts: []
    };

    let userRef = db.collection('users').doc(uid); // user doc reference
    userRef.set(data);

});
