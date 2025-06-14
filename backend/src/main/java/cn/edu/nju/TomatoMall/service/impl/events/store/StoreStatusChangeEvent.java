package cn.edu.nju.TomatoMall.service.impl.events.store;

import cn.edu.nju.TomatoMall.models.po.Store;
import lombok.Getter;

@Getter
public class StoreStatusChangeEvent extends StoreEvent {
    public StoreStatusChangeEvent(Store store) {
        super(store);
    }
}
