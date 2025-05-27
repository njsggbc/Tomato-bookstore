package cn.edu.nju.TomatoMall.events.employment;

import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import lombok.Getter;

@Getter
public class EmployeeDismissedEvent extends EmploymentEvent{
    private final Store store;
    private final User user;
    private final String reason;

    public EmployeeDismissedEvent(Store store, User user, String reason) {
        super(null);
        this.store = store;
        this.user = user;
        this.reason = reason;
    }
}
