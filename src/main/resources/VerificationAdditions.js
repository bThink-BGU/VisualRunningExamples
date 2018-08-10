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
    var block = [];
    while (true) {
        var evt = bp.sync({waitFor: anyEntrance, block: block});
        block.push(evt);
    }
});
