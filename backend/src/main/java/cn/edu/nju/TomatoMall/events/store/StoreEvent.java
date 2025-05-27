package cn.edu.nju.TomatoMall.events.store;

import cn.edu.nju.TomatoMall.models.po.Store;
import lombok.Getter;

@Getter
public abstract class StoreEvent {
    private final Store store;

    public StoreEvent(Store store) {
        this.store = store;
    }
}
