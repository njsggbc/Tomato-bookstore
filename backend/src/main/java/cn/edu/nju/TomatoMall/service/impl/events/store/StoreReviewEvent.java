package cn.edu.nju.TomatoMall.service.impl.events.store;

import cn.edu.nju.TomatoMall.models.po.Store;
import lombok.Getter;

@Getter
public class StoreReviewEvent extends StoreEvent {
    private final boolean pass;
    private final String comment;

    public StoreReviewEvent(Store store, boolean pass, String comment) {
        super(store);
        this.pass = pass;
        this.comment = comment;
    }
}
