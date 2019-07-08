(ns todo-list.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [todo-list.mydb :refer :all]
))


(defn the-handler                                                                                                         
 "When you send a request to the webapp, the ring adaptor (-main) converts the request to a map and sends it to the specified handler.This the handler functyion that takes the request map as its argument and returns a response map. " 
   [request]                                                                                                           (html [:h1 " Hello Good Human"]
          [:ul [:li "Huslte in the rain like no one is watching"]                       
                     [:li "Get this bread no matter what"]]))                                                                                                    

(defn goodbye
   "A route for goodbye"
   [request]
   (html [:h1 "Thank you so much for your visit."]
         [:p "We are very grateful of your presence!!"] ))


(defn about 
  "It gives you info about the prog."
   [request]
  {:status 200
   :body "<h1>Nkosinathi's simple web service, not bad...</h1>"
   :headers {}})

(defn req-info
  "Gives you information about the request that has been sent"
  [request]
  
  {:status 200
   :body (pr-str request)
   :headers {}})

  (defn hello
  "Takes in a name from the request and display a personalised message"
  [request]
  (let [name (get-in request [:route-params :name])]
    {:status 200
     :body (str "Hello, "name ". How are you today ?")
     :headers {}}))

(def operators {"+" + "-" - ":" / "*" *})

(defn calculator
  "Gives a calculated result from the request's parameters"


  [request]


 (let [a  (Integer/parseInt (get-in request [:route-params :a]))
       b  (Integer/parseInt (get-in request [:route-params :b]))
       op (get-in request [:route-params :op])  
       the_op (get operators op)]
      
      
      
  (if the_op
    {:status 200
     :body  (str "Your result: " (the_op a b)) 
     :headers {}}
    {:status 404
     :body "I am not farmiliar with that operand. Try +,-,* or /"
     :headers {}})))

(def mylist

(todo-list.mydb/thetasks))

(defn tasks 
  "Gets your priority task list from the database."

  [request]

  (html 
         [:h2 "Hello, here is your to do list for today:"] 
         [:table    
                  [:tr [:th "Name"]
                       [:th "Type"]
                       [:th "Estimate (mins)"]]
                  [:tr [:th (get-in mylist [0 0])]
                       [:th (get-in mylist [0 1])]
                       [:th (get-in mylist [0 2])]
                       ]
                  [:tr [:th (get-in mylist [1 0])]
                       [:th (get-in mylist [1 1])]
                       [:th (get-in mylist [1 2])]
                       ]
                  [:tr [:th (get-in mylist [2 0])]
                       [:th (get-in mylist [2 1])]
                       [:th (get-in mylist [2 2])]
                       ]
                  [:tr [:th (get-in mylist [3 0])]
                       [:th (get-in mylist [3 1])]
                       [:th (get-in mylist [3 2])]
                       ]
                     ]
            ))



;---------------------------------------------------------------------------

(defroutes app

  (GET "/" [] the-handler)
  (GET "/goodbye" [] goodbye)
  (GET "/about" [] about)
  (GET "/req-info" [] req-info)
  (GET "/hello/:name" [] hello)
  (GET "/calculator/:op/:a/:b" [] calculator)
  (GET "/mytasks" [] tasks)
  (not-found  "<h1>This is not the page you are looking for</h1>
            <p>Sorry, the page you requested was not found!</p>")) 

(defn -main
  "The -main function takes a port number as an argument which we pass when running the application. 
  The Ring Jetty adaptor is used to run an instance of Jetty. 
  The -main function contains an anonymous function that takes any request and returns a response map."
  [pn]  ;The port number

  (jetty/run-jetty 
   app
   {:port (Integer/parseInt pn)})
  )

(defn -dev-main
  "This one allows us to make changes and save then it pushes all those to the server."
  [pn]  ;The port number

  (jetty/run-jetty 
   (wrap-reload #'app) ; Skip the evaluation of the function and use the name instead
   {:port (Integer/parseInt pn)})
  )
