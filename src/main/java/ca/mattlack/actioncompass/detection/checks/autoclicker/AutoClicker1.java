package ca.mattlack.actioncompass.detection.checks.autoclicker;

import ca.mattlack.actioncompass.detection.Check;
import ca.mattlack.actioncompass.detection.packetevents.PacketEventDig;
import ca.mattlack.actioncompass.detection.util.EventSubscription;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockDig;

import java.util.UUID;

public class AutoClicker1 extends Check {

    int state = 0;
    long time = System.currentTimeMillis();

    public AutoClicker1(UUID playerId) {
        super(playerId, "AC-1");
    }

    @EventSubscription
    private void onMinePacket(PacketEventDig e) {

        PacketPlayInBlockDig dig = e.packet;
        if(!e.playerId.equals(this.getPlayerId()))
            return;

        if(dig.d() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
            time = System.currentTimeMillis();
        } else if(dig.d() == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
            long t = System.currentTimeMillis() - time;
            if(t < 20) {
                if(++state >= 10) {
                    recordViolation(1);
                    state = 0;
                }
            } else {
                state = 0;
            }
        }
    }

}
