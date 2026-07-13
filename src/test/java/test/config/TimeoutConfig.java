package test.config;

public class TimeoutConfig {


    private TimeoutConfig() {
    }

    public static final long BRANDS_PAGE_LOAD = 10_000;
    public static final long BRANDS_LIST_LOAD = 15_000;

    public static final long AI_CHAT_FIRST_RESPONSE = 20_000;
    public static final long AI_CHAT_FULL_RESPONSE = 60_000;

    public static final long LOGIN_REDIRECT = 10_000;

}
