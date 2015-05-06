# Grassfed

@dpp's exploration of Scala, Lift, Clojure, ClojureScript, and Reagent

To run, you'll need 3 different terminals...

Terminal 1:

```
lein zinc cc
```

This will recompile all the Scala code

Terminal 2:

```
lein figwheel
```

This gives you Figwheel browser REPL stuff.

Terminal 3:

```
lein

(require 'grassfed.server)

(grassfed.server/-main) ;; start the web server
```

Point your browser to http://localhost:8080

