Copyright
---------
Copyright 2013 Relevance, Inc.

Cross Origin Resource Sharing Sample
====================================

# Current State

This sample app contains a working demonstration of both CORS and SSE
running off of one page. In the test case, a static page with embedded
javascript is served from http://localhost:8081/js, and it consumes an
SSE EventSource found at http://localhost:8080/ . The sample has some
rough edges that we'd like to improve before claiming Pedestal has a
canonical SSE + CORS solution.

1. Due to the client-side polyfill we're using to get SSE and CORS
working together, the HTTP response for the EventSource needs to have
CORS headers sent to the browser when it is requested. To achieve
this, we have exploited implementation details of the sse interceptor,
specifically around how and when it sends headers, to preempt the
sending of headers by the SSE interceptor and allow the CORS
interceptor to send its headers and the SSE headers.

2. The client-side SSE library we've used in this example does not
make use of CORS pre-flight requests. We have handling code in place
to work with the pre-flight mechanism, but it isn't getting tested due
to a lack of support on the client side.

3. The SSE library we're incorporating is pulled directly from the
HEAD of a Github repository we do not own or control. A malicious
actor could compromise the Github repo and inject javascript through
this vector.

4. The behavior on the server side of CORS requests which are not
authorized is to respond with the requested data as if it were
authorized, but without the accompanying authorizing HTTP headers. A
robust solution should have a different behavior when an unauthorized
client makes a request to these resources.

5. The example cors-interceptor is only useful for synchronous
responses that flow back out through the interceptor path. It cannot
perform any useful work on the response in cases where request
processing does not enter the leave stage.

# Desired Moves Forward

1. Create a similar cors-interceptor that is added to
default-interceptors if it is enabled via a key in your production or
development app-map.

2. Provide a way to set the set of whitelisted domains.

3. Provide a way to control what happens if requestor is not in list
of whitelisted domains (or doesn't include CORS headers). This should
be open in the sense that an app can specify what happens when a
request is rejected based on these settings.

4. Mechanism to specify set of headers to send on SSE response from
outside existing SSE plumbing, e.g., to add CORS headers.

5. Either make sse-interceptor more configurable, or build it out of
smaller pieces that pedestal clients can build sse-interceptors out
of.
