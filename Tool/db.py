import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

# Use a service account
cred = credentials.Certificate('secret/navibee-unimelb.json')
firebase_admin.initialize_app(cred)

db = firestore.client()

# mark all exists conversation as private
def update1():
    docs = db.collection('conversations').get()
    for doc in docs:
        print(doc.id)
        db.collection('conversations').document(doc.id).update({'type': 'private'})
update1()
