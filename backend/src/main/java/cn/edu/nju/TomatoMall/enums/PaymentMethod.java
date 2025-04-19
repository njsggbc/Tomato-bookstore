package cn.edu.nju.TomatoMall.enums;

public enum PaymentMethod {
    ALIPAY("支付宝"),
    WECHAT_PAY("微信支付"),
    UNION_PAY("银联支付");

    private final String method;

    PaymentMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
