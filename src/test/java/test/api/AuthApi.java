package test.api;

import io.restassured.response.Response;
import test.config.TestConfig;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.restassured.RestAssured.given;

public class AuthApi {

    private static final String AUTH_ENDPOINT =
            "/api/rest/v2/security/tokens/authentication";

    private static final String ACCESS_ENDPOINT =
            "/api/rest/v2/security/tokens/access";


    public AuthSession authenticate() {

        String password = TestConfig.getPassword();

        if (password.startsWith(":")) {
            password = password.substring(1);
        }

        String credentials =
                TestConfig.getUsername()
                        + "::"
                        + password;


        String basicAuth = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));


        Response authResponse =
                given()
                        .baseUri(TestConfig.getUrl())
                        .queryParam("forever", true)
                        .queryParam("requestId", System.currentTimeMillis())

                        .header("Authorization", "Basic " + basicAuth)
                        .header("Accept", "application/json, text/plain, */*")
                        .header("Referer", TestConfig.getUrl() + "/")
                        .header("Origin", TestConfig.getUrl())
                        .header("Content-Type", "application/x-www-form-urlencoded")

                        .log().all()

                        .when()
                        .post(AUTH_ENDPOINT)

                        .then()
                        .log().all()
                        .extract()
                        .response();


        authResponse.then().statusCode(200);


        String authenticationToken = authResponse.asString();


        String sigmaCookie =
                authResponse.getCookie("sigma_authorization");


        if (sigmaCookie == null) {
            throw new RuntimeException(
                    "sigma_authorization cookie was not returned"
            );
        }


        System.out.println(
                "sigma_authorization cookie: " + sigmaCookie
        );


        Response accessResponse =
                given()
                        .baseUri(TestConfig.getUrl())

                        .cookie(
                                "sigma_authorization",
                                sigmaCookie
                        )

                        .header(
                                "Authorization",
                                "Bearer " + authenticationToken
                        )

                        .header(
                                "Accept",
                                "application/json, text/plain, */*"
                        )

                        .header(
                                "Referer",
                                TestConfig.getUrl() + "/"
                        )

                        .header(
                                "Origin",
                                TestConfig.getUrl()
                        )

                        .header(
                                "Content-Type",
                                "application/x-www-form-urlencoded"
                        )

                        .log().all()

                        .when()
                        .post(ACCESS_ENDPOINT)

                        .then()
                        .log().all()
                        .extract()
                        .response();


        accessResponse.then().statusCode(200);


        return new AuthSession(
                accessResponse.asString(),
                sigmaCookie
        );
    }
}