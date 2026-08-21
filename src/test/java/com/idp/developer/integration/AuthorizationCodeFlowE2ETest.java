package com.idp.developer.integration;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.idp.developer.properties.OAuth2ClientProperties;
import com.idp.developer.properties.UserProperties;
import com.idp.developer.repository.UserRepository;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthorizationCodeFlowE2ETest extends AbstractIdpIntegrationMockMvcTest {

    private static final String SCOPES = "openid profile email";
    private static final Pattern INPUT_PATTERN = Pattern.compile("<input\\b([^>]*)>", Pattern.CASE_INSENSITIVE);

    @LocalServerPort
    private int serverPort;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        this.baseUrl = "http://localhost:" + serverPort;
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    private HttpRequest.Builder request(URI uri) {
        return HttpRequest.newBuilder().uri(uri);
    }

    @Test
    @DisplayName("Authorization Code + PKCE (S256) end-to-end: login, consent, token, refresh, JWKS signature")
    void fullFlow_withPkce_verifiesTokensAndRefreshRotation() throws Exception {
        String username1 = configProperties.getUsers().getFirst().getUsername();
        Long expectedSub = userRepository.findByUsername(username1)
                .orElseThrow(() -> new IllegalStateException(username1 + " user not found"))
                .getId();

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateRandomState();
        String nonce = generateRandomState();

        // 1. authorize -> expect 302 to /login
        String authQuery = "response_type=code"
                + "&client_id=" + getOauth2Client().getClientId()
                + "&redirect_uri=" + URLEncoder.encode(getOauth2Client().getRedirectUris().getFirst(), StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8)
                + "&state=" + state
                + "&nonce=" + nonce
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";
        HttpResponse<String> authResponse = getAuthResponse(authQuery, true);

        String loginLocation = locationOf(authResponse);
        assertThat(loginLocation).contains("/login");

        HttpResponse<String> loginResponse = getLoginResponse(resolve(loginLocation));
        String consentLocation = locationOf(loginResponse);
        assertThat(consentLocation).contains("/oauth2/authorize");

        // follow redirect to consent page -> 200
        HttpResponse<String> consentPage = httpClient.send(
                request(resolve(consentLocation)).header("Accept", "text/html").GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(consentPage.statusCode()).isEqualTo(getOauth2Client().isAuthorizationConsent() ? HttpStatus.OK.value() : HttpStatus.FOUND.value());

        // extract state + scope checkboxes from consent page
        //    (the consent POST is handled by the authorization-server filter chain, which has CSRF disabled)
        //    NOTE: SAS 1.2.4 echoes the PKCE code_challenge in the consent form's "state" field, so the
        //    value that round-trips through the redirect is the echoed form state, not the originally sent one.
        String consentState = extractInputValue(consentPage.body(), "state");
        List<String> scopes = extractScopeValues(consentPage.body());
        // openid is implicit in OIDC and is never rendered as a consent checkbox;
        // the consent page only lists the non-openid scopes that require approval.
        assertThat(scopes).containsExactlyInAnyOrder("profile", "email");

        // POST /oauth2/authorize consent -> 302 to redirect_uri with code
        StringBuilder consentBody = new StringBuilder();
        consentBody.append("client_id=").append(getOauth2Client().getClientId());
        consentBody.append("&state=").append(consentState);
        for (String scope : scopes) {
            consentBody.append("&scope=").append(scope);
        }

        String codeLocation = getConsentLocation(consentBody);
        assertThat(codeLocation).startsWith(getOauth2Client().getRedirectUris().getFirst());
        String code = extractParam(codeLocation, "code");
        assertThat(code).isNotNull().isNotEmpty();
        // The final redirect's `state` must equal the OAuth `state` originally sent on the
        // authorize request. (Note: SAS 1.2.4 renders the PKCE code_challenge inside the
        // consent form's "state" field, but the authorization state round-trips correctly.)
        assertThat(extractParam(codeLocation, "state")).isEqualTo(state);

        // token exchange -> 200
        String tokenBody = "grant_type=authorization_code"
                + "&code=" + code
                + "&redirect_uri=" + URLEncoder.encode(getOauth2Client().getRedirectUris().getFirst(), StandardCharsets.UTF_8)
                + "&code_verifier=" + codeVerifier;
        String tokensJson = postToken(tokenBody);
        Map<String, Object> tokens = parseJson(tokensJson);
        assertThat(tokens)
                .containsKeys("access_token", "refresh_token", "id_token", "token_type", "expires_in")
                .containsEntry("token_type", "Bearer");

        String accessToken = (String) tokens.get("access_token");
        String idToken = (String) tokens.get("id_token");
        String refreshToken = (String) tokens.get("refresh_token");

        // JWKS for signature verification
        JWKSet jwkSet = fetchJwkSet();

        // access token checks
        SignedJWT accessJwt = SignedJWT.parse(accessToken);
        verifySignature(accessJwt, jwkSet);

        JWTClaimsSet accessClaims = accessJwt.getJWTClaimsSet();
        assertAccessClaims(accessClaims, expectedSub);

        // id token checks
        SignedJWT idJwt = SignedJWT.parse(idToken);
        verifySignature(idJwt, jwkSet);

        JWTClaimsSet idClaims = idJwt.getJWTClaimsSet();
        assertIdClaims(idClaims, expectedSub, nonce, username1);

        // refresh token checks
        String refreshedJson = postToken("grant_type=refresh_token&refresh_token=" + refreshToken);
        Map<String, Object> refreshed = parseJson(refreshedJson);
        checkRefreshed(refreshed, accessToken, refreshToken);

        // old refresh token must be invalidated (reuseRefreshTokens=false)
        HttpResponse<String> replayResponse = httpClient.send(
                request(URI.create(baseUrl + "/oauth2/token"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Authorization", basicAuth())
                        .POST(HttpRequest.BodyPublishers.ofString("grant_type=refresh_token&refresh_token=" + refreshToken))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(replayResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        Map<String, Object> replayError = parseJson(replayResponse.body());
        assertThat(replayError).containsEntry("error", "invalid_grant");
    }

    private void checkRefreshed(Map<String, Object> refreshed, String accessToken, String refreshToken) {
        assertThat(refreshed)
                .containsEntry("token_type", "Bearer")
                .containsKey("access_token")
                .extractingByKey("access_token")
                .isNotEqualTo(accessToken);

        // refresh scope is space-delimited String
        Object refreshedScope = refreshed.get("scope");
        assertThat(List.of(((String) refreshedScope).split(" ")))
                .containsExactlyInAnyOrder("openid", "profile", "email");

        String newRefreshToken = (String) refreshed.get("refresh_token");
        assertThat(newRefreshToken).isNotEqualTo(refreshToken);
    }

    private @NonNull String getConsentLocation(StringBuilder consentBody) throws IOException, InterruptedException {
        HttpResponse<String> consentResponse = httpClient.send(
                request(URI.create(baseUrl + "/oauth2/authorize"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(consentBody.toString())).build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(consentResponse.statusCode()).isEqualTo(HttpStatus.FOUND.value());
        return locationOf(consentResponse);
    }

    private void assertAccessClaims(JWTClaimsSet accessClaims, Long expectedSub) throws ParseException {
        assertThat(accessClaims.getIssuer()).isEqualTo(configProperties.getAuthorizationServer().getIssuerUrl());
        assertThat(accessClaims.getAudience()).containsExactly(getOauth2Client().getClientId());
        assertThat(accessClaims.getSubject()).isEqualTo(String.valueOf(expectedSub));
        assertThat(accessClaims.getStringListClaim("scope")).containsExactlyInAnyOrder("openid", "profile", "email");
        long lifetime = accessClaims.getExpirationTime().toInstant().getEpochSecond()
                - accessClaims.getIssueTime().toInstant().getEpochSecond();
        assertThat(lifetime).isEqualTo(300);
    }

    private void assertIdClaims(JWTClaimsSet idClaims, Long expectedSub, String nonce, String username1) throws ParseException {
        assertThat(idClaims.getIssuer()).isEqualTo(configProperties.getAuthorizationServer().getIssuerUrl());
        assertThat(idClaims.getAudience()).containsExactly(getOauth2Client().getClientId());
        assertThat(idClaims.getSubject()).isEqualTo(String.valueOf(expectedSub));
        assertThat(idClaims.getStringClaim("nonce")).isEqualTo(nonce);
        assertThat(idClaims.getStringClaim("azp")).isEqualTo(getOauth2Client().getClientId());
        assertThat(idClaims.getStringClaim("preferred_username")).isEqualTo(username1);
        assertThat(idClaims.getExpirationTime().toInstant().getEpochSecond())
                .isGreaterThan(idClaims.getIssueTime().toInstant().getEpochSecond());
    }

    private HttpResponse<String> getLoginResponse(URI loginUri) throws IOException, InterruptedException {

        // GET /login -> 200, extract _csrf
        HttpResponse<String> loginPage = httpClient.send(
                request(loginUri).header("Accept", "text/html").GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(loginPage.statusCode()).isEqualTo(HttpStatus.OK.value());
        String loginCsrf = extractInputValue(loginPage.body(), "_csrf");

        // POST /login with username/password/_csrf -> 302 to /oauth2/authorize
        UserProperties user = configProperties.getUsers().getFirst();
        String loginBody = "username=" + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8) + "&password=" + URLEncoder.encode(user.getPassword(), StandardCharsets.UTF_8) + "&_csrf=" + URLEncoder.encode(loginCsrf, StandardCharsets.UTF_8);
        HttpResponse<String> loginResponse = httpClient.send(
                request(loginUri)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(loginBody)).build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(loginResponse.statusCode()).isEqualTo(HttpStatus.FOUND.value());
        return loginResponse;
    }

    private HttpResponse<String> getAuthResponse(String authQuery, boolean requireProofKey) throws IOException, InterruptedException {
        HttpResponse<String> authResponse = httpClient.send(
                request(URI.create(baseUrl + "/oauth2/authorize?" + authQuery))
                        .header("Accept", "text/html").GET().build(),
                HttpResponse.BodyHandlers.ofString());
        int expectedResult = requireProofKey ? HttpStatus.FOUND.value() : HttpStatus.OK.value();
        assertThat(authResponse.statusCode()).isEqualTo(expectedResult);
        return authResponse;
    }

    @Test
    @DisplayName("Authorization Code + PKCE: wrong code_verifier returns invalid_grant")
    void fullFlow_wrongCodeVerifier_returnsInvalidGrant() throws Exception {
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateRandomState();

        String code = performAuthorizeAndConsent(codeChallenge, state, getOauth2Client().getRedirectUris().getFirst());

        // use a different verifier
        String wrongVerifier = generateCodeVerifier();
        HttpResponse<String> response = httpClient.send(
                request(URI.create(baseUrl + "/oauth2/token"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Authorization", basicAuth())
                        .POST(HttpRequest.BodyPublishers.ofString(
                                "grant_type=authorization_code"
                                        + "&code=" + code
                                        + "&redirect_uri=" + URLEncoder.encode(getOauth2Client().getRedirectUris().getFirst(), StandardCharsets.UTF_8)
                                        + "&code_verifier=" + wrongVerifier))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> error = parseJson(response.body());
        assertThat(error).containsEntry("error", "invalid_grant");
    }

    @Test
    @DisplayName("Authorize senza PKCE dopo login restituisce invalid_request (400) se requireProofKey=true")
    void fullFlow_missingPkceAfterLogin_returnsInvalidRequest() throws Exception {
        // Authenticate via the login form to obtain a session cookie.
        getLoginResponse(URI.create(baseUrl + "/login"));

        // Authenticated authorize WITHOUT code_challenge must be rejected if requireProofKey=true.
        String query = "response_type=code"
                + "&client_id=" + getOauth2Client().getClientId()
                + "&redirect_uri=" + URLEncoder.encode(getOauth2Client().getRedirectUris().getFirst(), StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8)
                + "&state=" + generateRandomState();
        HttpResponse<String> authResponse = getAuthResponse(query, getOauth2Client().isRequireProofKey());

        if (getOauth2Client().isRequireProofKey()) {
            // SAS 1.2.4 rejects a missing PKCE after authentication with a redirect back to the
            // redirect_uri carrying error=invalid_request (no authorization code is issued).
            String location = locationOf(authResponse);
            assertThat(location)
                    .isNotNull()
                    .contains("error=invalid_request")
                    .doesNotContain("code=");
        }
    }

    @Test
    @DisplayName("Logout con id_token reale reindirizza a post_logout_redirect_uri")
    void fullFlow_logoutWithRealIdToken_redirectsToPostLogoutUri() throws Exception {
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateRandomState();

        String code = performAuthorizeAndConsent(codeChallenge, state, getOauth2Client().getRedirectUris().getFirst());

        String tokenBody = "grant_type=authorization_code"
                + "&code=" + code
                + "&redirect_uri=" + URLEncoder.encode(getOauth2Client().getRedirectUris().getFirst(), StandardCharsets.UTF_8)
                + "&code_verifier=" + codeVerifier;
        Map<String, Object> tokens = parseJson(postToken(tokenBody));
        String idToken = (String) tokens.get("id_token");

        String logoutBody = "id_token_hint=" + idToken
                + "&post_logout_redirect_uri=" + URLEncoder.encode(getOauth2Client().getPostLogoutRedirectUris().getFirst(), StandardCharsets.UTF_8)
                + "&client_id=" + getOauth2Client().getClientId();
        HttpResponse<String> logoutResponse = httpClient.send(
                request(URI.create(baseUrl + "/connect/logout"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(logoutBody)).build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(logoutResponse.statusCode()).isEqualTo(HttpStatus.FOUND.value());
        assertThat(locationOf(logoutResponse)).isEqualTo(getOauth2Client().getPostLogoutRedirectUris().getFirst());
    }

    private String performAuthorizeAndConsent(String codeChallenge, String state, String redirectUri) throws Exception {
        String authQuery = "response_type=code"
                + "&client_id=" + getOauth2Client().getClientId()
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8)
                + "&state=" + state
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";
        HttpResponse<String> authResponse = getAuthResponse(authQuery, true);
        HttpResponse<String> loginResponse = getLoginResponse(resolve(locationOf(authResponse)));

        HttpResponse<String> consentPage = httpClient.send(
                request(resolve(locationOf(loginResponse))).header("Accept", "text/html").GET().build(),
                HttpResponse.BodyHandlers.ofString());

        // If consent was already granted in a prior run, the consent endpoint short-circuits
        // and returns the authorization code directly (302 to the redirect_uri). Extract it.
        if (consentPage.statusCode() == HttpStatus.FOUND.value()) {
            String codeLocation = locationOf(consentPage);
            assertThat(codeLocation).startsWith(redirectUri);
            return extractParam(codeLocation, "code");
        }

        String consentState = extractInputValue(consentPage.body(), "state");
        List<String> scopes = extractScopeValues(consentPage.body());

        StringBuilder consentBody = new StringBuilder();
        consentBody.append("client_id=").append(getOauth2Client().getClientId());
        consentBody.append("&state=").append(consentState);
        for (String scope : scopes) {
            consentBody.append("&scope=").append(scope);
        }
        String codeLocation = getConsentLocation(consentBody);
        assertThat(codeLocation).startsWith(redirectUri);
        String code = extractParam(codeLocation, "code");
        assertThat(code).isNotNull().isNotEmpty();
        return code;
    }

    private String postToken(String body) throws Exception {
        HttpResponse<String> response = httpClient.send(
                request(URI.create(baseUrl + "/oauth2/token"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Authorization", basicAuth())
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        return response.body();
    }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString((getOauth2Client().getClientId() + ":" + getOauth2Client().getClientSecret()).getBytes(StandardCharsets.UTF_8));
    }

    private JWKSet fetchJwkSet() throws Exception {
        HttpResponse<String> response = httpClient.send(
                request(URI.create(baseUrl + "/oauth2/jwks")).header("Accept", "application/json").GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        return JWKSet.parse(response.body());
    }

    private void verifySignature(SignedJWT jwt, JWKSet jwkSet) throws Exception {
        JWSHeader header = jwt.getHeader();
        assertThat(header.getKeyID()).isNotNull();

        JWK jwk = jwkSet.getKeyByKeyId(header.getKeyID());
        assertThat(jwk)
                .as("JWK with kid '%s' not found", header.getKeyID())
                .isNotNull();

        assertThat(jwt.verify(new RSASSAVerifier(jwk.toRSAKey().toRSAPublicKey()))).isTrue();
    }

    private String locationOf(HttpResponse<String> response) {
        String location = response.headers().firstValue("Location").orElse(null);
        assertThat(location).as("expected a Location header").isNotNull();
        return location;
    }

    private URI resolve(String location) {
        return location.startsWith("http") ? URI.create(location) : URI.create(baseUrl + location);
    }

    private String extractParam(String url, String name) {
        String query = URI.create(url).getRawQuery();
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String extractInputValue(String html, String name) {
        Map<String, String> inputs = parseInputFields(html);
        assertThat(inputs).as("input field '%s' not found in HTML", name).containsKey(name);
        return inputs.get(name);
    }

    private List<String> extractScopeValues(String html) {
        List<String> scopes = new ArrayList<>();
        if (html != null) {
            Matcher matcher = INPUT_PATTERN.matcher(html);
            while (matcher.find()) {
                String tag = matcher.group(1);
                if (!"scope".equals(attr(tag, "name"))) {
                    continue;
                }
                // A consenting scope checkbox carries its value in `value`; scopes already
                // granted are rendered as disabled checkboxes with no `value` (name in `id`).
                // Fall back to `id` so the consent POST re-grants the full set and the flow
                // stays isolated across test runs.
                String scope = attr(tag, "value");
                if (scope == null || scope.isEmpty()) {
                    scope = attr(tag, "id");
                }
                if (scope != null && !scope.isEmpty()) {
                    scopes.add(scope);
                }
            }
        }
        assertThat(scopes).as("no scope input fields found in consent page").isNotEmpty();
        return scopes;
    }

    private Map<String, String> parseInputFields(String html) {
        Map<String, String> inputs = new LinkedHashMap<>();
        if (html == null) {
            return inputs;
        }
        Matcher matcher = INPUT_PATTERN.matcher(html);
        while (matcher.find()) {
            String tag = matcher.group(1);
            String name = attr(tag, "name");
            String value = attr(tag, "value");
            if (name != null) {
                inputs.put(name, value == null ? "" : value);
            }
        }
        return inputs;
    }

    private String attr(String tag, String attribute) {
        Pattern p = Pattern.compile(attribute + "\\s*=\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(tag);
        if (m.find()) {
            return m.group(1);
        }
        Pattern p2 = Pattern.compile(attribute + "\\s*=\\s*'([^']*)'", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(tag);
        if (m2.find()) {
            return m2.group(1);
        }
        return null;
    }

    @Test
    @DisplayName("Public client + PKCE: Authorization Code Flow senza autenticazione client")
    void publicClient_withPkce_completesAuthorizationCodeFlow() throws Exception {

        OAuth2ClientProperties publicClient = configProperties.getOauth2Clients()
                .stream()
                .filter(client -> "public-client".equals(client.getClientId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("public-client not configured"));

        assertThat(publicClient.getClientSecret()).isEmpty();
        assertThat(publicClient.getClientAuthenticationMethods()).containsExactly("none");
        assertThat(publicClient.isRequireProofKey()).isTrue();
        assertThat(publicClient.getAuthorizationGrantTypes()).contains("authorization_code");

        String redirectUri = publicClient.getRedirectUris().getFirst();

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateRandomState();

        // 1. Authorization request
        String authQuery = "response_type=code"
                + "&client_id=" + publicClient.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode("openid profile email", StandardCharsets.UTF_8)
                + "&state=" + state
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";

        HttpResponse<String> authResponse = httpClient.send(
                request(URI.create(baseUrl + "/oauth2/authorize?" + authQuery))
                        .header("Accept", "text/html")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(authResponse.statusCode()).isEqualTo(HttpStatus.FOUND.value());
        assertThat(locationOf(authResponse)).contains("/login");

        // 2. Login
        HttpResponse<String> loginResponse = getLoginResponse(resolve(locationOf(authResponse)));
        assertThat(locationOf(loginResponse)).contains("/oauth2/authorize");

        // 3. Consent
        HttpResponse<String> consentPage = httpClient.send(
                request(resolve(locationOf(loginResponse)))
                        .header("Accept", "text/html")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(consentPage.statusCode()).isEqualTo(HttpStatus.OK.value());

        String consentState = extractInputValue(consentPage.body(), "state");
        List<String> scopes = extractScopeValues(consentPage.body());

        assertThat(scopes) .containsExactlyInAnyOrder("profile", "email");

        StringBuilder consentBody = new StringBuilder()
                .append("client_id=").append(publicClient.getClientId())
                .append("&state=").append(consentState);

        for (String scope : scopes) {
            consentBody.append("&scope=").append(scope);
        }

        HttpResponse<String> consentResponse = httpClient.send(
                request(URI.create(baseUrl + "/oauth2/authorize"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                consentBody.toString()))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(consentResponse.statusCode()).isEqualTo(HttpStatus.FOUND.value());

        String codeLocation = locationOf(consentResponse);
        assertThat(codeLocation).startsWith(redirectUri);
        assertThat(extractParam(codeLocation, "state")).isEqualTo(state);

        String code = extractParam(codeLocation, "code");

        assertThat(code)
                .isNotNull()
                .isNotEmpty();

        // 4. Token exchange.
        //
        // IMPORTANT:
        // no Authorization header and no client_secret are sent.
        String tokenBody = "grant_type=authorization_code"
                + "&client_id=" + URLEncoder.encode(
                publicClient.getClientId(), StandardCharsets.UTF_8)
                + "&code=" + code
                + "&redirect_uri="
                + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&code_verifier=" + codeVerifier;

        HttpResponse<String> tokenResponse = httpClient.send(
                request(URI.create(baseUrl + "/oauth2/token"))
                        .header("Content-Type",
                                "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(tokenBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(tokenResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

        Map<String, Object> tokens = parseJson(tokenResponse.body());

        assertThat(tokens)
                .containsKeys(
                        "access_token",
                        "id_token",
                        "token_type",
                        "expires_in")
                .containsEntry("token_type", "Bearer");
    }
}
