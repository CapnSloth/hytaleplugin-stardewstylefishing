package com.capnsloth.stardewfishing.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jspecify.annotations.NonNull;

public class FishingMiniGame_UiBased extends BasicCustomUIPage {


    public FishingMiniGame_UiBased(@NonNull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CantClose);
    }

    @Override
    public void build(UICommandBuilder commandBuilder) {
        commandBuilder.append("Pages/SSF_FishingMiniGame.ui");
    }
}
