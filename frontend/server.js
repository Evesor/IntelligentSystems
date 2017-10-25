var liveServer = require("live-server");

var params = {
    port: 8080,
    host: "0.0.0.0",
    root: "src/",
    open: false,
    file: "index.html",
    wait: 1000,
    logLevel: 2
};

liveServer.start(params);