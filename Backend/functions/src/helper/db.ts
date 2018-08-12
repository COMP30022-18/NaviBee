import { config } from 'firebase-functions';
import { initializeApp, firestore } from 'firebase-admin';

initializeApp(config().firebase);

const settings = {/* your settings... */ timestampsInSnapshots: true};

const fs = firestore();
fs.settings(settings);

export const db = fs;
