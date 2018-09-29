import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import datetime

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
# update1()

# clean contacts and emergency from users
def update2():
    docs = db.collection('users').get()
    for doc in docs:
        print(doc.id)
        db.collection('users').document(doc.id).update({'emergency': firestore.DELETE_FIELD, 'contacts': firestore.DELETE_FIELD})
# update2()


# add createtime in conversations
def update3():
    docs = db.collection('conversations').get()
    for doc in docs:
        print(doc.id)
        db.collection('conversations').document(doc.id).update({'createTimestamp': datetime.datetime.now()})
# update3()

# add isDeleted flag in conversations
def update4():
    docs = db.collection('conversations').get()
    for doc in docs:
        print(doc.id)
        db.collection('conversations').document(doc.id).update({'isDeleted': False})
update4()
