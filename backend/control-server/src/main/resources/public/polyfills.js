Array.prototype.flatMap = function (lambda) {
    return Array.prototype.concat.apply([], this.map(lambda));
};

/** Returns true if the new element was not found in the array */
Array.prototype.none = function (newElement) {
    return !this.some(element => element === newElement);
}

Array.prototype.unique = function () {
    return [...new Set(this)];
};

/** Diffs two arrays, finding out which elements have been removed in the second array */
Array.prototype.removed = function (newArray) {
    return this.filter((element) => {
        return newArray.none(element);
    });
};

/** Diffs two arrays, finding out which elements have been added in the second array */
Array.prototype.added = function (newArray) {
    return newArray.filter((element) => {
        return this.none(element);
    });
};
