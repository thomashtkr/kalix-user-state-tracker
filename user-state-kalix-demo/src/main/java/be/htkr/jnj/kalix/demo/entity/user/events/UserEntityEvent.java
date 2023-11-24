package be.htkr.jnj.kalix.demo.entity.user.events;

import java.time.Instant;

public interface UserEntityEvent {

    String userId();
    Integer movement();
    Instant timestamp();

}





