package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.dto.EdrDTO;

public class Caching {

    private Caching(){

    }

    private static Caching instance;
    
    public static Caching getInstance() {
        if(instance == null){
            instance = new Caching();
        }
        return instance;
    }

    private Cache<DSPRequest, AuthorizationContext> negotiationCache =
            Caffeine.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build();


    
    private Cache<AuthorizationContext, EdrDTO> tokenCache =
            Caffeine.newBuilder()
                    .expireAfter(
                            new Expiry<AuthorizationContext, EdrDTO>() {
                                @Override
                                public long expireAfterCreate(
                                        AuthorizationContext key, EdrDTO value, long currentTime) {
                                    if (value.expiresIn() != null) {
                                        long seconds =
                                                Long.parseLong(value.expiresIn())
                                                        - 10; // 10s for all subsequent operations
                                        // until
                                        // token expires
                                        return TimeUnit.SECONDS.toNanos(seconds);
                                    }
                                    return 0; // no expiration info, expire immediately
                                }

                                @Override
                                public long expireAfterUpdate(
                                        AuthorizationContext key,
                                        EdrDTO value,
                                        long currentTime,
                                        long currentDuration) {
                                    return currentDuration;
                                }

                                @Override
                                public long expireAfterRead(
                                        AuthorizationContext key,
                                        EdrDTO value,
                                        long currentTime,
                                        long currentDuration) {
                                    return currentDuration;
                                }
                            })
                    .build();



    public EdrDTO getToken(AuthorizationContext context){
        return tokenCache.getIfPresent(context);
    
    }

    public void putToken(AuthorizationContext context, EdrDTO token){
        tokenCache.put(context, token);
    }

    public AuthorizationContext getNegotiation(DSPRequest accessRequest) {
        return negotiationCache.getIfPresent(accessRequest);
    }

    public void putNegotiation(DSPRequest accessRequest, AuthorizationContext context){
        negotiationCache.put(accessRequest, context);
    }

}
