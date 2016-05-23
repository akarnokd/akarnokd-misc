package hu.akarnokd.reactiverpc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;

import org.reactivestreams.*;

import rsc.util.DeferredSubscription;
import rx.internal.util.RxJavaPluginUtils;

public enum RpcServiceMapper {
    ;
    
    public static Map<String, Object> serviceMap(Class<?> api) {
        Map<String, Object> result = new HashMap<>();
        
        for (Method m : api.getMethods()) {
            if (m.isAnnotationPresent(RsRpc.class)) {
                RsRpc a = m.getAnnotation(RsRpc.class);
                
                String name = m.getName();
                
                String aname = a.name();
                if (!aname.isEmpty()) {
                    name = aname;
                }
                
                Class<?> rt = m.getReturnType();
                
                if (rt == Void.TYPE) {
                    int pc = m.getParameterCount();
                    if (pc != 1) {
                        throw new IllegalStateException("RsRpc annotated methods require exactly one parameter: " + m);
                    }
                    if (Function.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        result.put(name, new RpcUmap());
                    } else
                    if (Publisher.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        result.put(name, new RpcSend());
                    } else {
                        throw new IllegalStateException("RsRpc annotated methods require a Function or Publisher as a parameter: " + m);
                    }
                } else
                if (Publisher.class.isAssignableFrom(rt)) {
                    int pc = m.getParameterCount();
                    if (pc > 1) {
                        throw new IllegalStateException("RsRpc annotated methods returning a Publisher require 0 or 1 parameter: " + m);
                    }
                    if (pc == 0) {
                        result.put(name, new RpcReceive());
                    } else {
                        if (Publisher.class.isAssignableFrom(m.getParameterTypes()[0])) {
                            result.put(name, new RpcMap());
                        } else {
                            throw new IllegalStateException("RsRpc annotated methods returning a Publisher allows only Publisher as parameter: " + m);
                        }
                    }
                } else {
                    throw new IllegalStateException("Unsupported RsRpc annotated return type: " + m);
                }
            }
        }
        
        return result;
    }
    
    public static Object dispatch(String name, Object action, Object[] args, RpcIOManager io) {
        if (action instanceof RpcSend) {
            RpcSend rpcSend = (RpcSend) action;
            rpcSend.send(name, (Publisher<?>)args[0], io);
            return null;
        } else
        if (action instanceof RpcReceive) {
            RpcReceive rpcReceive = (RpcReceive) action;
            return rpcReceive.receive(name, io);
        } else
        if (action instanceof RpcMap) {
            RpcMap rpcMap = (RpcMap) action;
            return rpcMap.map(name, (Publisher<?>)args[0], io);
        } else
        if (action instanceof RpcUmap) {
            RpcUmap rpcUmap = (RpcUmap) action;
            @SuppressWarnings("unchecked")
            Function<Publisher<?>, Publisher<?>> f = (Function<Publisher<?>, Publisher<?>>)args[0];
            rpcUmap.umap(name, f, io);
            return null;
        }
        throw new IllegalStateException("Unsupported action class: " + action.getClass());
    }
    
    public static final class RpcSend {
        
        public void send(String function, Publisher<?> values, RpcIOManager io) {
            long streamId = io.newStreamId();
            
            SendSubscriber s = new SendSubscriber(io, streamId);
            io.registerSubscription(streamId, s);
            io.sendNew(streamId, function);
            
            values.subscribe(s);
        }
        
        static final class SendSubscriber 
        extends DeferredSubscription
        implements Subscriber<Object>, Subscription {
            
            final RpcIOManager io;
            
            final long streamId;
            
            boolean done;
            
            volatile Subscription s;
            static final AtomicReferenceFieldUpdater<SendSubscriber, Subscription> S =
                    AtomicReferenceFieldUpdater.newUpdater(SendSubscriber.class, Subscription.class, "s");
            
            public SendSubscriber(RpcIOManager io, long streamId) {
                this.io = io;
                this.streamId = streamId;
            }
            
            @Override
            public void onSubscribe(Subscription s) {
                super.set(s);
            }
            
            @Override
            public void onNext(Object t) {
                if (done) {
                    return;
                }
                try {
                    io.sendNext(streamId, t);
                } catch (IOException ex) {
                    cancel();
                    onError(ex);
                }
            }
            
            @Override
            public void onError(Throwable t) {
                if (done) {
                    RxJavaPluginUtils.handleException(t);
                    return;
                }
                done = true;
                io.sendError(streamId, t);
            }
            
            @Override
            public void onComplete() {
                if (done) {
                    return;
                }
                done = true;
                io.sendComplete(streamId);
            }
        }
    }
    
    public static final class RpcReceive {
        
        public Publisher<?> receive(String function, RpcIOManager io) {
            // TODO
            return null;
        }
    }
    
    public static final class RpcMap {
        
        public Publisher<?> map(String function, Publisher<?> values, RpcIOManager io) {
            // TODO
            return null;
        }
    }
    
    public static final class RpcUmap {
        public void umap(String function, Function<Publisher<?>, Publisher<?>> mapper, RpcIOManager io) {
         // TODO
        }
    }
}
