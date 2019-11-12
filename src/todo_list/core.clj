(ns todo-list.core
  (:require [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :as resp]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5]]
            [todo-list.mydb :refer :all]
            [todo-list.diff :refer :all]
            [bidi.bidi :as bidi]
            [bidi.ring :as br]
            [org.httpkit.server :as http]))

(defn the-handler
  "When you send a request to the webapp, the ring adaptor (-main) converts
   the request to a map and sends it to the specified handler. This the handler
   functyion that takes the request map as its argument and returns a response map. "
  
  [_request]
  
  
  {:status 200
   :body   (html5 {}
             [:h1 " Hello there"]
             
             [:div {:style "margin-bottom:1em;background-color:#cccccc; border-radius:1em;padding:1em;font-size: 20px"}
              [:ul
               [:li "We have a basic calculator here that can be accessed via <b>`/calculator/operator/first-digit/second-digit`</b>"]
               [:li "We also have a differentiator that can does your univariate polynomial differention, which can be accessed via <b>`/diff/first-variable/second-variable`</b>"]]])})


(defn html-response
  "Wraps responses in a map "
  [body]
  (-> {:status 200
       :body   body}
      
      (resp/content-type "text/html")))

(defn thediff
  "Route for differenting"
  [request]
  ;(println thediff)
  (html-response
    (html5 {}
     [:h1 "Your differentiated univariate polynomial is:"]
     [:div {:style "margin-bottom:1em;background-color:#cccccc; border-radius:1em;padding:1em;font-size: 30px"} 
      [:p (todo-list.diff/differentiator request)]])))

(defn req-info
  "Gives you information about the request that has been sent"
  [_request]
  (-> {:status 200
       :body   (pr-str _request)}
      (resp/content-type "text/plain")))



(def OPERATORS {"+" + "-" - ":" / "*" *})

(defn calculator
  "Gives a calculated result from the request's parameters"
  [request]
  (println calculator)
  ;; You could/should add error handling here
  (let [a (Integer/parseInt (get-in request [:route-params :a]))
        b (Integer/parseInt (get-in request [:route-params :b]))
        op (get-in request [:route-params :op])
        ;; Always use kebab-case in clojure, never snake_case (except for contstants)
        ; the_op (get OPERATORS op)
        the-op (get OPERATORS op)
        ]
    ;; Neither of these responses are valid HTML or set to plain text
    (if the-op
      (html-response (html5 (str "Your result: " (the-op a b))))
      (html-response (html5 ("I am not farmiliar with that operand. Try +,-,* or /")))  
     
)))

;; You shouldn't call functions in a `def`.  Loading an application should not cause any code to run
;; automatically except for the `-main` function.  You could call this in a `let` block inside the
;; `tasks` function.
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
      [:th (get-in mylist [3 2])]]]))

;; You can iterate over the tasks programatically:
;; (I haven't actually run this, let me know if it doesn't work...)
(defn tasks2
  "Gets your priority task list from the database."
  [_request]
  (let [tasks (todo-list.mydb/thetasks)]
    (html-response
      (html5
        [:h2 "Hello, here is your to do list for today:"]
        [:table
         ;; header row should be enclosed in a thead tag
         [:thead
          [:tr
           [:th "Name"]
           [:th "Type"]
           [:th "Estimate (mins)"]]]
         ;; body should be in a tbody
         (into [:tbody]
               (->> tasks
                    (map (fn [[title kind estimate]]        ; Destructuring syntax, see https://clojure.org/guides/destructuring
                           [:tr
                            [:td title]
                            [:td kind]
                            [:td estimate]]))))]))))

(defn not-found
  [_req]
  (-> (resp/response
        (html5 {}
          [:h1 "This is not the page you are looking for"]
          [:p "Sorry, the page you requested was not found!"]))
      (resp/status 404)))

;---------------------------------------------------------------------------

(def ROUTES
  ["/" [["" {:get ::home}]
        ["req-info" {:get ::req-info}]
        [["calculator/" [#"[\+\-\:\*]" :op] "/" [#"\d+" :a] "/" [#"\d+" :b]] {:get ::calculator}]
        ["mytasks" {:get ::tasks}]
        [["diff/" [#".+" :poly]] {:get ::diff}]
        [true ::not-found]]])

(comment
  (bidi/match-route ROUTES "/mytasks")
  (bidi/path-for ROUTES ::calculator :op "+" :a "1" :b "2")
  (bidi/path-for ROUTES ::diff :args "1/2/")
  )

(def HANDLERS
  {::home       the-handler
   ::req-info   req-info
   ::calculator calculator
   ::diff       thediff
   ::tasks      tasks
   ::not-found  not-found})

(def app-handler
  (br/make-handler ROUTES (fn [handler] (get HANDLERS handler))))

(defn -main
  "The -main function takes a port number as an argument which we pass when running the application. 
  The Ring Jetty adaptor is used to run an instance of Jetty. 
  The -main function contains an anonymous function that takes any request and returns a response map."
  [pn]                                                      ;The port number
  (http/run-server app-handler
                   {:port (Integer/parseInt pn)}))

(defn -dev-main
  "This one allows us to make changes and save then it pushes all those to the server."
  [pn]                                                      ;The port number

  (http/run-server (-> #'app-handler
                       wrap-reload)
                   {:port (Integer/parseInt pn)}))
