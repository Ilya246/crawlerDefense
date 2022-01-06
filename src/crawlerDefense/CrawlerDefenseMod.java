package crawleDefense;

import arc.Core;
import arc.Events;
import arc.math.Mathf;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.types.FlyingAI;
import mindustry.content.Blocks;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.abilities.UnitSpawnAbility;
import mindustry.entities.bullet.SapBulletType;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.mod.Plugin;
import mindustry.net.Administration;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.BuildPayload;

import static mindustry.Vars.*;
import static crawlerDefense.CVars.*;

public class CrawlerDefenseMod extends Plugin {
    public static boolean gameIsOver = true, waveIsOver = false, firstWaveLaunched = false;

    @Override
    public void init(){

        Events.on(WorldLoadEvent.class, event -> {

        });

        Events.on(PlayerJoin.class, event -> {

        });

        Events.run(Trigger.update, () -> {

        });

        Log.info("Crawler Defense loaded.");
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("info", "commands.info.description", (args, player) -> Bundle.bundled(player, "commands.info"));
    }

    public void registerServerCommands(CommandHandler handler){
        handler.register("kill", "Kill all enemies in the current wave.", args -> Groups.unit.each(u -> u.team == state.rules.waveTeam, Unitc::kill));
    }
}
