package ca.mattlack.actioncompass.detection.util;

import ca.mattlack.actioncompass.detection.packetevents.PacketEventDig;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockDig;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;

public class EventSubscriptions implements Listener {

    @Getter
    private class RegisteredSubscription2 {
        private WeakReference<Object> weakObject;
        private List<MethodCaller> callers = new ArrayList<>();
        private List<Class<?>> classes = new ArrayList<>();

        RegisteredSubscription2(Object object) {
            this.weakObject = new WeakReference<>(object);
        }

        private boolean isValid() {
            return weakObject.get() != null;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof RegisteredSubscription2) {
                Object other = ((RegisteredSubscription2) o).weakObject.get();
                Object our = this.weakObject.get();
                return Objects.equals(other, our) && ((RegisteredSubscription2) o).getClasses().containsAll(this.getClasses());
            }
            return false;
        }
    }

    @AllArgsConstructor
    @Getter
    private class MethodCaller {
        private BiConsumer<Object, Object> action;
        private EventPriority priority;
    }

    public static EventSubscriptions instance;

    private final LockingList<RegisteredSubscription2> subscriptions = new LockingList<>();

    private final RegisteredListener registeredListenerLowest;
    private final RegisteredListener registeredListenerLow;
    private final RegisteredListener registeredListenerNormal;
    private final RegisteredListener registeredListenerHigh;
    private final RegisteredListener registeredListenerHighest;
    private final RegisteredListener registeredListenerMonitor;


    public EventSubscriptions(Plugin plugin) {
        instance = this;

        //Register for all events
        //this.registerEventsInPackage(Event.class.getPackage().getDescription(), Event.class.getClassLoader());
        registeredListenerLowest = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.LOWEST), EventPriority.LOWEST, plugin, false);
        registeredListenerLow = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.LOW), EventPriority.LOW, plugin, false);
        registeredListenerNormal = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.NORMAL), EventPriority.NORMAL, plugin, false);
        registeredListenerHigh = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.HIGH), EventPriority.HIGH, plugin, false);
        registeredListenerHighest = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.HIGHEST), EventPriority.HIGHEST, plugin, false);
        registeredListenerMonitor = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.MONITOR), EventPriority.MONITOR, plugin, false);
    }

    public synchronized void call(Object e) {
        this.callMethods(e, EventPriority.LOWEST);
        this.callMethods(e, EventPriority.LOW);
        this.callMethods(e, EventPriority.NORMAL);
        this.callMethods(e, EventPriority.HIGH);
        this.callMethods(e, EventPriority.HIGHEST);
        this.callMethods(e, EventPriority.MONITOR);
    }

    public void callMethods(Object e, EventPriority priority) {

        if (e == null)
            return;

        this.subscriptions.getLock().perform(() -> this.subscriptions.removeIf(s -> !s.isValid()));
        this.subscriptions.copy().forEach(s -> {
            Object object = s.getWeakObject().get();
            if (object == null)
                return;
            s.getCallers().forEach(c -> {
                if (c.getPriority().equals(priority)) {
                    c.getAction().accept(object, e);
                }
            });
        });


    }

    public synchronized void onDisable() {
    }

    public synchronized boolean isSubscribed(Object object) {
        return this.isSubscribed(object, object.getClass());
    }

    public <T> boolean isSubscribed(T object, Class<? extends T> clazz) {
        RegisteredSubscription2 subscription = new RegisteredSubscription2(object);
        subscription.getClasses().add(clazz);
        return this.subscriptions.contains(subscription);
    }

    /**
     * Register normal object
     *
     * @param o Object
     */
    public void subscribe(Object o) {
        this.subscribe(o, o.getClass());
    }

    /**
     * Register abstract object
     *
     * @param o     Object
     * @param clazz Class of the abstract object
     */
    public <T> void subscribe(T o, Class<? extends T> clazz) {
//        DuelObjectClass wrapper = new DuelObjectClass<>(o, clazz);
//        if (!this.abstractObjects.contains(wrapper)) {
//            this.abstractObjects.add(wrapper);
//        }
        RegisteredSubscription2 subscription = new RegisteredSubscription2(o);
        for (RegisteredSubscription2 subs : subscriptions) {
            if (subscription.equals(subs)) {
                if (subs.getClasses().contains(clazz))
                    return;
                subs.getClasses().add(clazz);
                subs.getCallers().addAll(getCallers(clazz));
                return;
            }
        }
        subscription.getClasses().add(clazz);
        subscription.getCallers().addAll(getCallers(clazz));
        subscriptions.add(subscription);
    }

    private List<MethodCaller> getCallers(Class<?> c) {
        List<MethodCaller> callers = new ArrayList<>();
        ArrayList<Method> methodsToCheck = Lists.newArrayList(c.getDeclaredMethods());
        for (Method meths : c.getMethods()) {
            if (!methodsToCheck.contains(meths)) {
                methodsToCheck.add(meths);
            }
        }
        for (Method methods : methodsToCheck) {
            if (methods.isAnnotationPresent(EventSubscription.class)) {
                if (methods.getParameterCount() > 0) {
                    Class<?> inType = methods.getParameterTypes()[0];

                    if (Event.class.isAssignableFrom(inType)) {
                        try {
                            Method method = inType.getMethod("getHandlerList");
                            synchronized (this) {
                                HandlerList list = (HandlerList) method.invoke(null);
                                List<RegisteredListener> listeners = Arrays.asList(list.getRegisteredListeners());
                                if (!listeners.contains(registeredListenerLowest)) {
                                    list.register(registeredListenerLowest);
                                    list.register(registeredListenerLow);
                                    list.register(registeredListenerNormal);
                                    list.register(registeredListenerHigh);
                                    list.register(registeredListenerHighest);
                                    list.register(registeredListenerMonitor);
                                }
                            }
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        EventSubscription annotation = methods.getAnnotation(EventSubscription.class);
                        String name = c.getName();
                        MethodCaller caller = new MethodCaller((o, e) -> {
                            if (inType.isInstance(e) || inType.isAssignableFrom(e.getClass())) {
                                methods.setAccessible(true);
                                try {
                                    methods.invoke(o, e);
                                } catch (IllegalAccessException e1) {
                                    e1.printStackTrace();
                                } catch (InvocationTargetException e1) {
                                    System.out.println("ERROR while handling event for " + name);
                                    e1.getTargetException().printStackTrace();
                                }
                                methods.setAccessible(false);
                            }
                        }, annotation.priority());
                        callers.add(caller);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return callers;
    }

    public synchronized void unSubscribe(Object o) {
        this.subscriptions.remove(new RegisteredSubscription2(o));
    }

    private static String HANDLER_NAME = "ABC_PL_INJECTED";

    public void injectPlayerPacketListener(Player p) {
        Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
        UUID id = p.getUniqueId();
        channel.eventLoop().submit(() -> {
            if (channel.pipeline().get(HANDLER_NAME) == null) {
                channel.pipeline().addBefore("packet_handler", HANDLER_NAME, new ChannelDuplexHandler() {

                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        super.write(ctx, msg, promise);
                        callMethods(getPacketEvent(msg, id), EventPriority.NORMAL);
//                        if (!msg.getClass().toString().contains("PacketPlayOutEntity") &&
//                                !msg.getClass().toString().contains("PacketPlayOutChat") &&
//                        !msg.getClass().toString().contains("Chunk") && !msg.getClass().toString().contains("Light"))
//                            System.out.println("Sent: " + msg.getClass().toString());
                    }

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        super.channelRead(ctx, msg);
                        callMethods(getPacketEvent(msg, id), EventPriority.NORMAL);
                    }
                });
            }
        });
    }

    public void unInjectPlayerPacketListener(Player p) {
        Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            if (channel.pipeline().get(HANDLER_NAME) != null) {
                channel.pipeline().remove(HANDLER_NAME);
            }
        });
    }

    public Object getPacketEvent(Object packet, UUID playerId) {
        if (packet instanceof PacketPlayInBlockDig) {
            PacketEventDig dig = new PacketEventDig();
            dig.packet = (PacketPlayInBlockDig) packet;
            dig.playerId = playerId;
            return dig;
        }
        return null;
    }
}