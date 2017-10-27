let requestTypes = Object.freeze({
    properties: {

    }
});

var agentApi = (() => {
    const baseUrl = location.protocol +
        "//" +
        location.hostname +
        (location.port ? ":" + 4567 : "");

    /**
     * @param {string} agentId
     * @param {string} message, a whitespace delimited list of arguments
     * @returns {Promise} the fetch request
     */
    this.changeBehaviour = (agentName, message) => {
        const postInit = {
            headers: new Headers(),
            method: "POST",
            mode: "CORS",
            body: {
                "agentId": agentName,
                "message": message
            }
        };

        return fetch(baseUrl + "/api/agent/change-behaviour", postInit);
    };


    return this;
})();