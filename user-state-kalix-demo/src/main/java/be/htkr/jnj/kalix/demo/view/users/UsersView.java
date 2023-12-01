package be.htkr.jnj.kalix.demo.view.users;

import be.htkr.jnj.kalix.demo.entity.singlelevel.SingleLevelGroupingEntity;
import be.htkr.jnj.kalix.demo.entity.user.UserDemographic;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.entity.user.UserStateEntity;
import be.htkr.jnj.kalix.demo.entity.user.events.DemographicMovement;
import be.htkr.jnj.kalix.demo.entity.user.events.UserStatusMovement;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Query;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.annotations.Table;
import kalix.javasdk.annotations.ViewId;
import kalix.javasdk.view.View;
import org.springframework.web.bind.annotation.GetMapping;


@ViewId("users")
@Table("users")
@Subscribe.EventSourcedEntity(UserStateEntity.class)
public class UsersView extends View<UserData> {


    public UpdateEffect<UserData> onStatusIncrement(UserStatusMovement.UserStatusIncrement event) {
        return effects().updateState(new UserData(event.userId()));
    }

    public UpdateEffect<UserData> onStatusDecrement(UserStatusMovement.UserStatusDecrement event) {
        return effects().updateState(new UserData(event.userId()));
    }

    public UpdateEffect<UserData> onDemographicIncrement(DemographicMovement.DemographicIncrement event) {
        return effects().updateState(new UserData(event.userId()));
    }

    public UpdateEffect<UserData> onDemographicDecrement(DemographicMovement.DemographicDecrement event) {
        return effects().updateState(new UserData(event.userId()));
    }

    @GetMapping("/view/users")
    @Query("SELECT * as users FROM users" )
    public UserDataResponse getAllUsers() {
        return null;
    }
}
