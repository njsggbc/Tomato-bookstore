package cn.edu.nju.TomatoMall.enums;

public enum PaymentMethod {
    ALIPAY("支付宝");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public boolean isValidAccount(String account) {
        switch (this) {
            case ALIPAY:
                return account != null
                        && (account.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$") ||
                            account.matches("^1[3-9]\\d{9}$"));
            default:
                return false;
        }
    }
}
