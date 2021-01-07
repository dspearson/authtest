(ns authtest.auth
  (:require [caesium.magicnonce.secretbox :as msb]
            [caesium.crypto.box :as box]
            [caesium.byte-bufs :as bb]
            [caesium.crypto.pwhash :as pwhash]
            [buddy.core.codecs :as codecs]
            [clojure.java.io :as io]
            [authtest.db :refer :all])
  (:gen-class))

(def keyfile (io/file (or (System/getenv "AUTHTEST_KEY")
                          (str (System/getProperty "user.home") "/.authtest.key"))))

(defn uuid
  []
  (.toString (java.util.UUID/randomUUID)))

(defn generate-admin-key
  []
  (if (.exists keyfile)
    false
    (let [keypair (box/keypair!)
          public (codecs/bytes->hex (bb/->bytes (:public keypair)))
          secret (codecs/bytes->hex (bb/->bytes (:secret keypair)))]
      (spit keyfile public)
      {:public public
       :secret secret})))

(defn delete-admin-key
  []
  (if (.exists keyfile)
    (io/delete-file keyfile)))

(defn get-admin-public-key
  []
  (if-not (.exists keyfile)
    (throw (RuntimeException. "Key does not exist."))
    (codecs/hex->bytes (slurp keyfile))))

(defn encrypt-email
  [email]
  (let [email-bytes (.getBytes email "UTF-8")
        public-key (get-admin-public-key)
        encrypted-email (msb/secretbox-det email-bytes public-key)]
    encrypted-email))

(defn hash-password
  [password]
  (pwhash/pwhash-str password pwhash/opslimit-sensitive pwhash/memlimit-sensitive))

(defn generate-user
  [email password]
  (let [encrypted-email (encrypt-email email)
        hashed-password (hash-password password)
        uid (uuid)
        existing-user (get-user db {:email encrypted-email})]
    (if existing-user
      false
      (do
        (insert-user db {:email encrypted-email :password hashed-password :uid uid})
        {:uid uid}))))

(defn validate-user ; not constant-time
  [email password]
  (let [encrypted-email (encrypt-email email)
        existing-user (get-user db {:email encrypted-email})]
    (if-not existing-user
      false
      (and (= 0 (pwhash/pwhash-str-verify (:password existing-user) password))
           {:uid (:uid existing-user)}))))

(defn change-password
  [uid old-password new-password]
  (let [existing-user (get-user-by-uid db {:uid uid})]
    (if-not existing-user
      false
      (if (= 0 (pwhash/pwhash-str-verify (:password existing-user) old-password))
        (let [hashed-password (hash-password new-password)]
          (and (update-password db {:uid uid :password hashed-password})) true)
          false))))
