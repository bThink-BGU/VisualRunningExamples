/*
 * This file contains b-threads that model requirements and assumptions.
 */

bp.registerBThread("onlyOnce", function(){
    var block = [];
    while (true) {
        var evt = bp.sync({waitFor: anyEntrance, block: block});
        block.push(evt);
    }
});
