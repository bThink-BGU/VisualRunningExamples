var ROBOT_TRAPPED_EVENT = bp.Event("Robot fell into trap");

function enterEvent(c, r) {
    return bp.Event("Enter (" + c + "," + r + ")");//, {col:c, row:r});
}

var anyEntrance = bp.EventSet("AnyEntrance", function(evt){
   return evt.name.indexOf("Enter") === 0;
});

function adjacentCellEntries(col, row) {
    return [enterEvent(col + 1, row), enterEvent(col - 1, row),
        enterEvent(col, row + 1), enterEvent(col, row - 1)];

}

function evt2coord(evt) {
    if (!evt.name.startsWith("Enter")) {
        return undefined;
    }
    var coordStr = evt.name.split("\\(")[1];
    var coord = coordStr.split(",");
    return {
        col: Number(coord[0]),
        row: Number(coord[1].split("\\)")[0])
    };
}

////////////////////////
///// functions 
function parseMaze(mazeLines) {
    for (var row = 0; row < mazeLines.length; row++) {
        for (var col = 0; col < mazeLines[row].length; col++) {
            var currentPixel = mazeLines[row].substring(col, col + 1);
            if (currentPixel === " " || currentPixel === "t" || currentPixel === "s") {
                if (currentPixel === "t") {
                    addTargetCell(col, row);
                }
                if (currentPixel === "s") {
                    addWalker(col, row);
                }
            } else {
                addWall(col, row);
            }
        }
    }
    addEnclosingWalls(mazeLines[0].length, mazeLines.length);
}

function addWall(col, row) {
    bp.registerBThread("wall(" + col + "," + row + ")", function(){
        bp.sync({block: enterEvent(col, row)});
    });
}

function addEnclosingWalls(cols, rows) {
    var topWall = bp.EventSet("externalWalls", function (e) {
        var coords = evt2coord(e);
        if (coords) {
            if (coords.row < 0) return true;
            if (coords.col < 0) return true;
            if (coords.row >= rows) return true;
            if (coords.col >= cols) return true;
        }
        return false;
    });
    bp.registerBThread("externalWallsBt", function () {
        bp.sync({block: topWall});
    });
}

/**
 * Waits for an event signaling the entrance to the 
 * target cell, then blocks everything.
 * @param {Number} col
 * @param {Number} row
 * @returns {undefined}
 */
function addTargetCell(col, row) {
    bp.registerBThread("Target(c:" + col + " r:" + row + ")", function () {
        bp.sync({
            waitFor: enterEvent(col, row)
        });

        bp.sync({
           request: ROBOT_TRAPPED_EVENT,
           block: bp.allExcept( ROBOT_TRAPPED_EVENT )
        });
    });
}

function addWalker(col, row) {
    bp.registerBThread("starter(c:" + col + " r:" + row + ")", function () {
        var curCol = col;
        var curRow = row;
        bp.sync({
            request: enterEvent(col, row)
        });
        while (true) {
            var evt = bp.sync({
                request: adjacentCellEntries(curCol, curRow)
            });
            var coords = evt2coord(evt);
            curCol = coords.col;
            curRow = coords.row;
        }
    });
}

parseMaze(maze);