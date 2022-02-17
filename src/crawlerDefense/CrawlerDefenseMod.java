package crawlerDefense;

import arc.Core;
import arc.Events;
import arc.math.Mathf;
import arc.struct.*;
import arc.util.*;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.net.Administration;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.mod.Plugin;
import mindustry.type.UnitType;
import mindustry.world.Block;

import static mindustry.Vars.*;

public class CrawlerDefenseMod extends Plugin {
    public float timer;
    public float worldWidth;
    public float worldHeight;
    public OrderedMap<Float, UnitType> unitRequirements = new OrderedMap<>();
    public ObjectFloatMap<Block> blockCosts = new ObjectFloatMap<>();
    public ObjectFloatMap<String> buildPoints = new ObjectFloatMap<>();
    public ObjectMap<String, ObjectIntMap<Block>> buildAllowances = new ObjectMap<>();

    public static float startPeace = 60f * 60f;
    public static float timeOffset = 5f * 60f * 60f;
    public static float baseSpawnChance = 1f / 20000f;
    public static float pointsMultiplier = 1f / toPower(60f) / 60f;

    public enum Config{
        placeholder("Placeholder.", 1);

        public static final Config[] all = values();

        public final Object defaultValue;
        public String description;

        Config(String description, Object value){
            this.description = description;
            this.defaultValue = value;
        }
        public int i(){
            return Core.settings.getInt(name(), (int)defaultValue);
        }
        public float f(){
            return Core.settings.getFloat(name(), (float)defaultValue);
        }
        public boolean b(){
            return Core.settings.getBool(name(), (boolean)defaultValue);
        }
        public String s(){
            return Core.settings.get(name(), defaultValue).toString();
        }
        public void set(Object value){
            Core.settings.put(name(), value);
        }
    }

    @Override
    public void init(){
        unitRequirements.putAll(0f, UnitTypes.crawler,
        toPower(2f * 60f), UnitTypes.dagger,
        toPower(5f * 60f), UnitTypes.mace,
        toPower(10f * 60f), UnitTypes.atrax);

        Events.on(WorldLoadEvent.class, event -> {
            Core.app.post(() -> {
                state.rules.modeName = "Crawler Defense";
                state.rules.waveTimer = false;
                worldWidth = world.width() * 8f;
                worldHeight = world.height() * 8f;
                timer = 0f;
                buildPoints.clear();
                buildAllowances.clear();
            });
        });

        /*Events.on(PlayerJoin.class, event -> {

        });*/

        Events.run(Trigger.update, () -> {
            if(!state.isGame() || state.isPaused()){
                return;
            }
            timer += Time.delta;
            float ramp = Mathf.sqrt(timer + timeOffset);
            Groups.player.each(p -> {
                buildPoints.put(p.uuid(), buildPoints.get(p.uuid(), 0f) + ramp * pointsMultiplier);
            });
            if(timer > startPeace){
                if(Mathf.chance(baseSpawnChance * Groups.player.size() * ramp)){
                    float enemyPower = Mathf.random(ramp);
                    UnitType type = UnitTypes.crawler;
                    for(ObjectMap.Entry<Float, UnitType> e : unitRequirements){
                        if(enemyPower > e.key){
                            type = e.value;
                        }else{
                            break;
                        }
                    }
                    float angle = Mathf.random(Mathf.PI2);
                    type.spawn(state.rules.waveTeam, (worldWidth + Mathf.cos(angle) * worldWidth) / 2f, (worldHeight + Mathf.sin(angle) * worldHeight) / 2f);
                }
            }
        });

        netServer.admins.addActionFilter(action -> {
            if(action.type == Administration.ActionType.breakBlock){
                
            }
        });

        Log.info("Crawler Defense loaded.");
    }

    public float toPower(float seconds){
        return Mathf.sqrt(seconds * 60f + timeOffset);
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("info", "Shows information about the crawler defense gamemode.", (args, player) -> player.sendMessage("WIP"));
    }

    public void registerServerCommands(CommandHandler handler){
        handler.register("defenseconfig", "[name] [value]", "Configure crawler defense plugin settings. Run with no arguments to list values.", args -> {
            if(args.length == 0){
                Log.info("All config values:");
                for(Config c : Config.all){
                    Log.info("&lk| @: @", c.name(), "&lc&fi" + c.s());
                    Log.info("&lk| | &lw" + c.description);
                    Log.info("&lk|");
                }
                return;
            }
            try{
                Config c = Config.valueOf(args[0]);
                if(args.length == 1){
                    Log.info("'@' is currently @.", c.name(), c.s());
                }else{
                    if(args[1].equals("default")){
                        c.set(c.defaultValue);
                    }else{
                        try{
                            if(c.defaultValue instanceof Integer){
                                c.set(Integer.parseInt(args[1]));
                            }else if(c.defaultValue instanceof Float){
                                c.set(Float.parseFloat(args[1]));
                            }else{
                                c.set(Boolean.parseBoolean(args[1]));
                            }
                        }catch(NumberFormatException e){
                            Log.err("Not a valid number: @", args[1]);
                            return;
                        }
                    }
                    Log.info("@ set to @.", c.name(), c.s());
                    Core.settings.forceSave();
                }
            }catch(IllegalArgumentException e){
                Log.err("Unknown config: '@'. Run the command with no arguments to get a list of valid configs.", args[0]);
            }
        });
    }
}
