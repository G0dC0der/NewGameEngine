/*
 * Core
 */
var Game = {
    extend: Java.extend,
    type: Java.type
};
var impl = function(type, implementation) {
    var Instance = Game.extend(type, implementation);
    return new Instance();
};

/*
 * JDK
 */
var Array = Game.type("java.lang.reflect.Array")
var List = Game.type('java.util.ArrayList');

/*
 * Static Types
 */
var CameraEffects = Game.type('pojahn.game.essentials.CameraEffects');
var Factory = Game.type('pojahn.game.essentials.Factory');
var WayPoints = Game.type('pojahn.game.essentials.Waypoints');
var Utils = Game.type('pojahn.game.essentials.Utils');
var Collisions = Game.type('pojahn.game.core.Collisions');

/*
 * Interfaces
 */
var Unit = Game.type('pojahn.game.essentials.geom.Unit');
var EarthBound = Game.type('pojahn.game.essentials.geom.EarthBound');
var HudMessage = Game.type('pojahn.game.essentials.HUDMessage');
var Event = Game.type('pojahn.game.events.Event');
var ActionEvent = Game.type('pojahn.game.events.ActionEvent');
var CloneEvent = Game.type('pojahn.game.events.CloneEvent');
var RenderEvent = Game.type('pojahn.game.events.RenderEvent');
var TaskEvent = Game.type('pojahn.game.events.TaskEvent');
var TileEvent = Game.type('pojahn.game.events.TileEvent');


 /*
  * Types
  */
var TileBasedLevel = Game.type('pojahn.game.essentials.stages.TileBasedLevel');
var PixelBasedLevel = Game.type('pojahn.game.essentials.stages.PixelBasedLevel');
var Animation = Game.type('pojahn.game.essentials.Animation');
var Bounds = Game.type('pojahn.game.essentials.Bounds');
var CheckPointHandler = Game.type('pojahn.game.essentials.CheckPointHandler');
var Controller = Game.type('pojahn.game.essentials.Controller');
var EntityBuilder = Game.type('pojahn.game.essentials.EntityBuilder');
var Image2D = Game.type('pojahn.game.essentials.Image2D');
var ResourceManager = Game.type('pojahn.game.essentials.ResourceManager');
var Size = Game.type('pojahn.game.essentials.Size');
var SoundEmitter = Game.type('pojahn.game.essentials.SoundEmitter');

/*
 * Enums
 */
var Direction = Game.type('pojahn.game.essentials.Direction');
var GameState = Game.type('pojahn.game.essentials.GameState');
var HitBox = Game.type('pojahn.game.essentials.Hitbox');
var LaserBeam = Game.type('pojahn.game.essentials.LaserBeam');
var Vitality = Game.type('pojahn.game.essentials.Vitality');

/*
 * Level testing
 */

 function provide() {
    var CustomLevel = Game.extend(TileBasedLevel, {
        init: function() {
            java.lang.System.out.println(CustomLevel.getTileWidth());
//            CustomLevel.addz(impl(Event, {
//                eventHandling: function() {
//                    java.lang.System.out.println("I am run!");
//                }
//            }));

        },
        build: function() {

        },
        dispose: function() {

        }
    });

    return new CustomLevel();
 }