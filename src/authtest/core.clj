(ns authtest.core
  (:use [ring.util.response])
  (:require [aleph.http :as http]
            [ring.middleware.cookies :refer :all]
            [ring.middleware.params :refer :all]
            [ring.middleware.keyword-params :refer :all]
            [ring.middleware.json :refer :all]
            [ring.middleware.reload :refer :all]
            [ring.util.response :refer :all]
            [bidi.bidi :refer :all]
            [authtest.auth :as auth]
            [authtest.db :refer :all]
            [bidi.ring :refer (make-handler)]))

(defn parse-int
  [s]
  (try (Integer. (re-find  #"^\d+$" s)) (catch Exception e false)))

(defn access-denied
  [body]
  {:status 403 :headers {} :body body})

(defn illegal-route
  [request]
  (not-found "no route found."))

(defn index-handler
  [request]
  (response "authentication service"))

(defn authentication-handler
  [request]
  (let [params (:params request)
        email (:email params)
        password (:password params)
        auth-response (auth/validate-user email password)]
    (if auth-response
      (response auth-response)
      (access-denied "Incorrect credentials."))))

(defn create-user
  [request]
  (let [params (:params request)
        email (:email params)
        password (:password params)
        generate-user-response (auth/generate-user email password)]
    (if generate-user-response
      (response generate-user-response)
      (access-denied "User already exists."))))

(defn change-password
  [request]
  (let [params (:params request)
        uid (:uid params)
        password (:password params)
        new-password (:new_password params)
        change-password-response (auth/change-password uid password new-password)]
    (if change-password-response
      (response {:status "ok"})
      (access-denied "Invalid request."))))

(def route-map ["/" [["" index-handler]
                     ["authenticate" authentication-handler]
                     ["create" create-user]
                     ["change" change-password]
                     [true illegal-route]]])

(def entrypoint (make-handler route-map))

(def app
  (-> entrypoint
      wrap-reload
      wrap-keyword-params
      wrap-json-params
      wrap-json-response
      wrap-cookies
      wrap-params))

(defonce socket (java.net.InetSocketAddress. "127.0.0.1" (let [p (System/getenv "AUTHTEST_PORT")] (or (and p (parse-int p)) 9999))))
(defonce server (atom nil))

(defn stop-server
  []
  (if (not (nil? @server))
    (.close @server)))

(defn start-server
  []
  (reset! server (http/start-server #(app %) {:socket-address socket})))
