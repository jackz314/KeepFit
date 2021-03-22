/* eslint-disable max-len */
"use strict";

const functions = require("firebase-functions");

const jwt = require("jsonwebtoken");

const zoomSdkConfig = functions.config().zoomsdk;

const zoomSdkKey = zoomSdkConfig.key;
const zoomSdkSecret = zoomSdkConfig.secret;

// The admin Admin SDK to access Firestore.
// const admin = require("firebase-admin");
// admin.initializeApp();

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
