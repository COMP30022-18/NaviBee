import { config } from 'firebase-functions';
import { initializeApp, firestore, messaging } from 'firebase-admin';

initializeApp(config().firebase);

const settings = {/* your settings... */ timestampsInSnapshots: true};

const fs = firestore();
fs.settings(settings);

export const db = fs;
export const msg = messaging();
export const msgType = messaging;
