package cn.edu.nju.TomatoMall.service.impl.listener;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.MessagePriority;
import cn.edu.nju.TomatoMall.enums.MessageType;
import cn.edu.nju.TomatoMall.service.impl.events.employment.EmployeeDismissedEvent;
import cn.edu.nju.TomatoMall.service.impl.events.employment.EmployeeHiredEvent;
import cn.edu.nju.TomatoMall.service.impl.events.employment.EmployeeResignedEvent;
import cn.edu.nju.TomatoMall.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EmploymentListener {
    private final MessageService messageService;

    @Autowired
    public EmploymentListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @EventListener
    public void handleEmployeeDismissedEvent(EmployeeDismissedEvent event) {
        messageService.sendNotification(
                MessageType.BUSINESS,
                event.getUser(),
                "您已被解雇",
                "商店：" + event.getStore().getName() + "\n" +
                        "原因：" + event.getReason() + "\n" +
                        "如有疑问，请联系商店经理或请求客服处理",
                EntityType.EMPLOYMENT,
                null,
                MessagePriority.MEDIUM
        );
    }

    @EventListener
    public void handleEmployeeResignedEvent(EmployeeResignedEvent event) {
        messageService.sendNotification(
                MessageType.BUSINESS,
                event.getStore().getManager(),
                "员工离职通知",
                "员工 " + event.getUser().getUsername() + " 已离职\n" +
                        "离职原因：" + event.getReason(),
                EntityType.EMPLOYMENT,
                null,
                MessagePriority.MEDIUM
        );
    }

    @EventListener
    public void handleEmployeeHiredEvent(EmployeeHiredEvent event) {
        messageService.sendNotification(
                MessageType.BUSINESS,
                event.getEmployment().getStore().getManager(),
                "员工入职通知",
                "员工 " + event.getEmployment().getEmployee().getUsername() + " 已成功入职\n" +
                        "认证令牌: " + event.getToken().getName() + " ID: " + event.getToken().getId(),
                EntityType.EMPLOYMENT,
                event.getEmployment().getId(),
                MessagePriority.MEDIUM
        );
    }
}
