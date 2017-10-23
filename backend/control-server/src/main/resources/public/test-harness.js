let testJSON = {
    "nodes": [
        {
            "id": "Reseller2@10.1.21.39:1099\/JADE",
            "group": 2,
            "agent": {
                "name": "Reseller2",
                "type": "Reseller Agent",
                "current_Buy_Price": 1,
                "current_Sales_Volume": 238,
                "current_Purchase_Volume": 357,
                "current_Sell_Price": 1
            },
            "links": [
                {
                    "source": "PowerPlant1@10.1.21.39:1099\/JADE",
                    "target": "Reseller2@10.1.21.39:1099\/JADE",
                    "value": 4
                },
                {
                    "source": "PowerPlant2@10.1.21.39:1099\/JADE",
                    "target": "Reseller2@10.1.21.39:1099\/JADE",
                    "value": 4
                },
                {
                    "source": "Home2@10.1.21.39:1099\/JADE",
                    "target": "Reseller2@10.1.21.39:1099\/JADE",
                    "value": 2
                },
                {
                    "source": "Home1@10.1.21.39:1099\/JADE",
                    "target": "Reseller2@10.1.21.39:1099\/JADE",
                    "value": 2
                }
            ]
        },
        {
            "id": "PowerPlant2@10.1.21.39:1099\/JADE",
            "group": 1,
            "agent": {
                "name": "PowerPlant2",
                "current_Sell_Price": 1.8,
                "current_Production": 0
            },
            "links": [
                {
                    "source": "Reseller1@10.1.21.39:1099\/JADE",
                    "target": "PowerPlant2@10.1.21.39:1099\/JADE",
                    "value": 2
                },
                {
                    "source": "Reseller2@10.1.21.39:1099\/JADE",
                    "target": "PowerPlant2@10.1.21.39:1099\/JADE",
                    "value": 8
                }
            ]
        },
        {
            "id": "PowerPlant1@10.1.21.39:1099\/JADE",
            "group": 1,
            "agent": {
                "name": "PowerPlant1",
                "current_Sell_Price": 1.8,
                "current_Production": 0
            },
            "links": [
                {
                    "source": "Reseller1@10.1.21.39:1099\/JADE",
                    "target": "PowerPlant1@10.1.21.39:1099\/JADE",
                    "value": 1
                },
                {
                    "source": "Reseller2@10.1.21.39:1099\/JADE",
                    "target": "PowerPlant1@10.1.21.39:1099\/JADE",
                    "value": 7
                }
            ]
        },
        {
            "id": "Reseller1@10.1.21.39:1099\/JADE",
            "group": 2,
            "agent": {
                "name": "Reseller1",
                "type": "Reseller Agent",
                "current_Buy_Price": 1,
                "current_Sales_Volume": 120,
                "current_Purchase_Volume": 240,
                "current_Sell_Price": 1
            },
            "links": [
                {
                    "source": "PowerPlant1@10.1.21.39:1099\/JADE",
                    "target": "Reseller1@10.1.21.39:1099\/JADE",
                    "value": 1
                },
                {
                    "source": "PowerPlant2@10.1.21.39:1099\/JADE",
                    "target": "Reseller1@10.1.21.39:1099\/JADE",
                    "value": 1
                },
                {
                    "source": "Home2@10.1.21.39:1099\/JADE",
                    "target": "Reseller1@10.1.21.39:1099\/JADE",
                    "value": 2
                },
                {
                    "source": "Home1@10.1.21.39:1099\/JADE",
                    "target": "Reseller1@10.1.21.39:1099\/JADE",
                    "value": 2
                }
            ]
        }
    ]
};

this.validateLink = function (link, nodes) {
    console.log(link);
    console.log(nodes);
    console.log(Array.of(link));
    let result = Array.of(link)
        .filter(element => nodes.some(node => node === element.source))
        .filter(element => nodes.some(node => node === element.target))
        .shift();

    return !!result; 
};

let testHarness = (() => {
    let links = testJSON.nodes.flatMap((x) => {
        return x.links;
    });
    let agents = testJSON.nodes.map((x) => {
        return x.agent;
    });
    let ids = testJSON.nodes.map(x => {
        return x.id;
    }) ;
    
    links.forEach((link) => {
        console.log(validateLink(link, ids));
    });

})();

let arrayTestHarness = (() => {
    let array = [0,1,2,3,4];
    let element = 5;
    let elementNew = 4;
    console.log("Array.prototype.none(x)");
    console.log("Expecting: true");
    console.log("Got: " + array.none(element));
    console.log("");
    console.log("Expecting: false");
    console.log("Got: " + array.none(elementNew));
    console.log("");

    let nestedArray = [1, 2, [3, [4, 5], 6], 7];
    let flattenedArray = [1, 2, 3, [4, 5], 6, 7];
    console.log("flatMap test");
    console.log("Expected: ");
    console.log(flattenedArray);
    console.log("Actual: ");
    console.log(nestedArray.flatMap(x => { return x; }));
    console.log("");


    let array1 = [1, 2, 3, 4, 5, 6, 7];
    let array2 = [2, 4, 5, 8, 9, 10];
    let array1Added = array1.added(array2);
    let array1Removed = array1.removed(array2);
    console.log("added test");
    console.log("Expected: " + "8,9,10");
    console.log("Actual: " + array1Added.toString());
    console.log("");
    console.log("removed test");
    console.log("Expected: " + "1,3,6,7");
    console.log("Actual: " + array1Removed.toString());

})();
