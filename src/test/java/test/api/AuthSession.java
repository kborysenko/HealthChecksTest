package test.api;

public class AuthSession {
    private final String token;
    private final String sigmaAuthorizationCookie;

    public AuthSession(String token, String sigmaAuthorizationCookie) {
        this.token = token;
        this.sigmaAuthorizationCookie = sigmaAuthorizationCookie;
    }

    public String getToken() {
        return token;
    }

    public String getSigmaAuthorizationCookie() {
        return sigmaAuthorizationCookie;
    }
}
