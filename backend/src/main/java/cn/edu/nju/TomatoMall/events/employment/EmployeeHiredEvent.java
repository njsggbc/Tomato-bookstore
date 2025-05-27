package cn.edu.nju.TomatoMall.events.employment;

import cn.edu.nju.TomatoMall.models.po.Employment;
import cn.edu.nju.TomatoMall.models.po.EmploymentToken;
import lombok.Getter;

@Getter
public class EmployeeHiredEvent extends EmploymentEvent{
    private final EmploymentToken token;

    public EmployeeHiredEvent(Employment employment, EmploymentToken token) {
        super(employment);
        this.token = token;
    }
}
