# Pedestal Samples

This is a collection of examples of applications and services using the
[pedestal](http://pedestal.io) framework. They range from [very
simple](./helloworld_app) to [quite complex](./chat).

If you're new to pedestal, we recommend that you have a look at [our
documentation](http://pedestal.io/documentation) and then start with the
examples. You could also have a look at some [slides introducing
motivations and concepts](https://raw.github.com/pedestal/samples/master/slides/pedestal-intro-slides.pdf).

## Samples

* [helloworld-app](./helloworld-app), a simple pedestal app (a counter)
    that demonstrates how state is handled, how templates work (a bit)
    and how updates and rendering are handled
* [auto-reload-server](./auto-reload-server), a pedestal service that
    uses [ns-tracker](https://github.com/weavejester/ns-tracker) to reload
    changed code
* [cors](./cors)
* [jboss](./jboss), a pedestal service that can be deployed in
    [JBoss](http://jboss.org)
* [ring-middleware](./ring-middleware), shows how to use
    [ring](https://github.com/ring-clojure/ring) middlewares with pedestal
    services
* [server-sent-events](./server-sent-events)
* [server-with-links](./server-with-links), a pedestal service that
    demonstrates links generated from routes
* [square-root](./square-root), a pedestal app that implements the
    square root in terms of pedestal's primitives
* [template-server](./template-server), a pedestal service that shows
    how to use different template engines
* [chat](./chat), a client-server chat that demonstrates many features
    of pedestal, it consists of both a pedestal service (message
    federation) and a pedestal app (ui)
