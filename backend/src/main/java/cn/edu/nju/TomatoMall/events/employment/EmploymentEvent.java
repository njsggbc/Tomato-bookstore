package cn.edu.nju.TomatoMall.events.employment;

import cn.edu.nju.TomatoMall.models.po.Employment;
import lombok.Getter;

@Getter
public abstract class EmploymentEvent {
    private final Employment employment;

    public EmploymentEvent(Employment employment) {
        this.employment = employment;
    }
}
