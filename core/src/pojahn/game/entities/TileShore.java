package pojahn.game.entities;

//import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import pojahn.game.core.Entity;

//import pojahn.game.essentials.stages.TileBasedLevel;
//
public class TileShore extends Entity {
//
//    private static class DetailedCell extends TileBasedLevel.PositionedCell {
//
//        Entity shore;
//
//        DetailedCell(TileBasedLevel.PositionedCell pCell) {
//            super(pCell.cell, pCell.x, pCell.y);
//        }
//    }
//
//    private boolean shoreEmptyCellsOnly;
// Behviour: If false, check if the given cell have any borders of same type. If true, check if given cell have any cell bordering. The latter is good for example water effects,
// where you only want water at the top.
//
//    public TileShore() {
//
//    }
//
//    @Override
//    public void init() {
//        if (!(getLevel() instanceof TileBasedLevel))
//            throw new IllegalStateException("Class can only be used in conjunction with TileBasedLevel.");
//
//        super.init();
//    }

    /*
     * Still need to think about this one. Should the shores be cells or entites? Cells works better and are solid and fast. Entities offer more logics. Maybe implement both
     * Also, do we need to scan every frame in logicits? Maybe have manual trigger? Maybe configurable?
     * Further more, if it's cell we place, should it add one "above" or replace the curret one?
     */
}
