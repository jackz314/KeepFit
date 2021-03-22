/* eslint-disable max-len,require-jsdoc */
"use strict";

const functions = require("firebase-functions");
const jwt = require("jsonwebtoken");
const algoliasearch = require("algoliasearch").default;


const zoomSdkConfig = functions.config().zoomsdk;

const zoomSdkKey = zoomSdkConfig.key;
const zoomSdkSecret = zoomSdkConfig.secret;

// The admin Admin SDK to access Firestore.
const admin = require("firebase-admin");
admin.initializeApp();

// const db = admin.firestore();

// run everytime a new user is created
// exports.userCreated = functions.auth.user().onCreate(async (user) => {
//   const uid = user.uid;
//   const userRef = db.collection("users").doc(uid);

//   await userRef.set({
//     name: user.displayName,
//     email: user.email,
//     profile_pic: user.photoURL,
//   });
// });

// from https://github.com/firebase/functions-samples/blob/c516eec31997e494346dead87e675dd63dccda87/fulltext-search-firestore/functions/index.js
const algoliaConfig = functions.config().algolia;
const ALGOLIA_ID = algoliaConfig.app_id;
const ALGOLIA_ADMIN_KEY = algoliaConfig.api_key;
const ALGOLIA_SEARCH_KEY = algoliaConfig.search_key;

const ALGOLIA_INDEX_NAME = "Firestore";
const client = algoliasearch(ALGOLIA_ID, ALGOLIA_ADMIN_KEY);

const createIndexForUser = (doc, userId, index) => {
  // Get the user document
  const userData = doc.data();

  // Remove fields
  for (const field of ["biography", "birthday", "height", "profile_pic", "sex", "weight"]) {
    delete userData[field];
  }
  // Add an 'objectID' field which Algolia requires
  userData.objectID = userId;
  userData.type = "user";

  // Write to the algolia index
  return index.partialUpdateObject(userData, {createIfNotExists: true});
};

// Update the search index every time a user is changed/created.
exports.onUserWrite = functions.firestore.document("/users/{userId}").onWrite((snap, context) => {
  const algoliaIndex = client.initIndex(ALGOLIA_INDEX_NAME);
  const doc = snap.after;
  if (!doc.exists) {
    // delete event
    algoliaIndex.deleteObject(context.params.userId);
  } else {
    // create & update event
    return createIndexForUser(doc, context.params.userId, algoliaIndex);
  }
});

const createIndexForMedia = (doc, mediaId, index) => {
  // Get the media document
  const mediaData = doc.data();

  // Remove fields
  for (const field of ["creator", "is_livestream", "link", "start_time", "view_count", "thumbnail"]) {
    delete mediaData[field];
  }
  // Add an 'objectID' field which Algolia requires
  mediaData.objectID = mediaId;
  mediaData.type = "media";

  // Write to the algolia index
  return index.partialUpdateObject(mediaData, {createIfNotExists: true});
};

// Update the search index every time a media is changed/created.
exports.onMediaWrite = functions.firestore.document("/media/{mediaId}").onWrite((snap, context) => {
  const algoliaIndex = client.initIndex(ALGOLIA_INDEX_NAME);
  const doc = snap.after;
  if (!doc.exists) {
    // delete event
    algoliaIndex.deleteObject(context.params.mediaId);
  } else {
    // create & update event
    return createIndexForMedia(doc, context.params.mediaId, algoliaIndex);
  }
});

// Finally, pass our ExpressJS app to Cloud Functions as a function
// called 'getSecureSearchKey';
exports.getAlgoliaSearchKey = functions.https.onCall(((data, context) => {
  if (!context.auth) {
    // Throwing an HttpsError so that the client gets the error details.
    throw new functions.https.HttpsError("failed-precondition", "User is not authenticated.");
  }

  // @ts-ignore
  const uid = context.auth.uid;

  // Create the params object as described in the Algolia documentation:
  // https://www.algolia.com/doc/guides/security/api-keys/#generating-api-keys
  const params = {
    // This filter ensures that only documents where author == uid will be readable
    // filters: `author:${uid}`,
    // We also proxy the uid as a unique token for this key.
    userToken: uid,
  };

  // Call the Algolia API to generate a unique key based on our search key
  return client.generateSecuredApiKey(ALGOLIA_SEARCH_KEY, params);
}));

exports.reindexDatastore = functions.https.onRequest(async (req, res) => {
  const auth = {login: "jackzhang", password: "A_FjBSzc7(9`XnNb"}; // change this

  // parse login and password from headers
  const b64auth = (req.headers.authorization || "").split(" ")[1] || "";
  const [login, password] = Buffer.from(b64auth, "base64").toString().split(":");

  // Verify login and password are set and correct
  if (login && password && login === auth.login && password === auth.password) {
    // Access granted...
    const algoliaIndex = client.initIndex(ALGOLIA_INDEX_NAME);
    algoliaIndex.clearObjects();
    const db = admin.firestore();
    db.collection("users").get().then((snap) => {
      snap.forEach((doc) => {
        createIndexForUser(doc, doc.id, algoliaIndex);
      });
    });

    db.collection("media").get().then((snap) => {
      snap.forEach((doc) => {
        createIndexForMedia(doc, doc.id, algoliaIndex);
      });
      res.status(200).send("Indexing finished");
    });
  } else {
    res.status(403).send("Unauthorized");
  }
});

exports.getZoomJWTToken = functions.https.onCall((data, context) => {
  // Checking that the user is authenticated.
  if (!context.auth) {
    // Throwing an HttpsError so that the client gets the error details.
    throw new functions.https.HttpsError("failed-precondition", "User is not authenticated.");
  }
  const expTime = Math.floor(Date.now() / 1000) + 24 * (60 * 60); // 24 hrs expiration
  const token = jwt.sign({
    appKey: zoomSdkKey,
    exp: expTime, // iat is included automatically
    tokenExp: expTime, // not sure why, but zoom also requires tokenExp
  }, zoomSdkSecret, {algorithm: "HS256"});

  console.log("Generated token:", token);

  return token;
});
