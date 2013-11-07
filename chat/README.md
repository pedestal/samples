# Chat Sample

This is a chat sample, which demonstrates the use of Pedestal with...

- Server-Sent Events (SSE)
- Cookies
- Routing
- Interceptors


## Usage

To run from the `chat` directory...

```sh
cd chat-client
lein repl
(dev)
(watch :development)
```
Open another terminal, then:

```sh
cd chat-server

mkdir resources
cd resources

# Linux only:
ln -s ../../chat-client/out/public

cd ..

# Windows only:
xcopy /e ..\chat-client\out\public .\resources\public

lein run # Launch webserver on port 8080.
```

In a browser, navigate to <http://localhost:8080/chat-client-dev.html> where you will see the chat client interface.

Use the interface to send a chat message, which you will see echoed to your local screen.

If you play with a friend, you will both see each other's messages.

**Note:** Your chat session with the server is cookie-based, so **you cannot chat with yourself with multiple browser tabs**. As an alternative, you **can** chat with yourself by using multiple browsers or Chrome's "Incognito Mode."


License
-------
Copyright 2013 Relevance, Inc.

The use and distribution terms for this software are covered by the
Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
which can be found in the file epl-v10.html at the root of this distribution.

By using this software in any fashion, you are agreeing to be bound by
the terms of this license.

You must not remove this notice, or any other, from this software.
