/*
 * This file contains b-threads that model requirements and assumptions.
 */

// Requirement b-thread: the walker should not fall into the trap.
bp.registerBThread("don't hit target", function(){
	bp.sync({waitFor:TARGET_FOUND_EVENT});
	bp.ASSERT(false,"Maze walker fell into the trap.");
});

// Assumption b-thread: we're visiting each cell at most once.
// This is reduces the search space, but does not affect correctness.
bp.registerBThread("onlyOnce", function(){
    var visited = [];
    while (true) {
        var evt = bp.sync({waitFor: anyEntrance, block: visited});
        visited.push(evt);
    }
});

/*
// Ensure no diagonal movement is happenning

function getCoords(eventName) {
    var coord = eventName.split(" ")[1];	
    coord = coord.replace("(","").replace(")","");
    coord = coord.split(",");
    return {col:Number(coord[0]), row:Number(coord[1])};
}

bp.registerBThread("no-diagonal", function(){
    while (true) {
        var crd=getCoords(bp.sync({waitFor:anyEntrance}).name);
	noEntryNextMove(crd.col-1, crd.row-1);
	noEntryNextMove(crd.col-1, crd.row+1);
	noEntryNextMove(crd.col+1, crd.row-1);
	noEntryNextMove(crd.col+1, crd.row-+1);
    }
});

function noEntryNextMove(col, row){
    bp.registerBThread("nenv(" + col + "," + row + ")", function(){
        var evt = bp.sync({waitFor:anyEntrance});
        var newCoords = getCoords(evt.name);
        bp.ASSERT( newCoords.col !== col || newCoords.row !== row, "Diagonal Movement detected");
    });
}
*/