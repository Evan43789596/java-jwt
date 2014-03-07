package com.auth0.jwt;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.security.SignatureException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JWTVerifierTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldFailOnNullSecret() {
        new JWTVerifier(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldFailOnEmptySecret() {
        new JWTVerifier("");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOn1Segments() throws Exception {
        new JWTVerifier("such secret").verify("crypto");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOn2Segments() throws Exception {
        new JWTVerifier("such secret").verify("much.crypto");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOn4Segments() throws Exception {
        new JWTVerifier("such secret").verify("much.crypto.so.token");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOnEmptyStringToken() throws Exception {
        new JWTVerifier("such secret").verify("");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOnNullToken() throws Exception {
        new JWTVerifier("such secret").verify(null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfAlgorithmIsNotSetOnToken() throws Exception {
        new JWTVerifier("such secret").getAlgorithm(Collections.<String, String>emptyMap());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfAlgorithmIsNotSupported() throws Exception {
        new JWTVerifier("such secret").getAlgorithm(Collections.singletonMap("alg", "doge-crypt"));
    }

    @Test
    public void shouldWorkIfAlgorithmIsSupported() throws Exception {
       new JWTVerifier("such secret").getAlgorithm(Collections.singletonMap("alg", "HS256"));
    }

    @Test(expected = SignatureException.class)
    public void shouldFailOnInvalidSignature() throws Exception {
        final String jws = "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9" +
                "." +
                "eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFt" +
                "cGxlLmNvbS9pc19yb290Ijp0cnVlfQ" +
                "." +
                "suchsignature_plzvalidate_zomgtokens";
        String secret = "AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow";
        new JWTVerifier(secret, "audience").verifySignature(jws.split("\\."), "HmacSHA256");
    }

    @Test
    public void shouldVerifySignature() throws Exception {
        final String jws = "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9" +
                "." +
                "eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFt" +
                "cGxlLmNvbS9pc19yb290Ijp0cnVlfQ" +
                "." +
                "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        final String secret = "AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow";
        new JWTVerifier(secret, "audience")
                .verifySignature(jws.split("\\."), "HmacSHA256");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenExpired1SecondAgo() throws Exception {
        new JWTVerifier("such secret").verifyExpiration(
                Collections.singletonMap("exp", Long.toString(System.currentTimeMillis() / 1000L - 1L)));
    }

    @Test
    public void shouldVerifyExpiration() throws Exception {
        new JWTVerifier("such secret").verifyExpiration(
                Collections.singletonMap("exp", Long.toString(System.currentTimeMillis() / 1000L + 50L)));
    }

    @Test
    public void shouldVerifyIssuer() throws Exception {
        new JWTVerifier("such secret", "amaze audience", "very issuer")
                .verifyIssuer(Collections.singletonMap("iss", "very issuer"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIssuer() throws Exception {
        new JWTVerifier("such secret", "amaze audience", "very issuer")
                .verifyIssuer(Collections.singletonMap("iss", "wow"));
    }

    @Test
    public void shouldVerifyIssuerWhenNotFoundInClaimsSet() throws Exception {
        new JWTVerifier("such secret", "amaze audience", "very issuer")
                .verifyIssuer(Collections.<String, String>emptyMap());
    }

    @Test
    public void shouldVerifyAudience() throws Exception {
        new JWTVerifier("such secret", "amaze audience")
                .verifyAudience(Collections.singletonMap("aud", "amaze audience"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailAudience() throws Exception {
        new JWTVerifier("such secret", "amaze audience")
                .verifyAudience(Collections.singletonMap("aud", "wow"));
    }

    @Test
    public void shouldVerifyAudienceWhenNotFoundInClaimsSet() throws Exception {
        new JWTVerifier("such secret", "amaze audience")
                .verifyAudience(Collections.<String, String>emptyMap());
    }

    @Test
    public void decodeAndParse() throws Exception {
        final Base64 encoder = new Base64(true);
        final String encodedJSON = new String(encoder.encode("{\"some\": \"json\", \"number\": 123}".getBytes()));
        final JWTVerifier jwtVerifier = new JWTVerifier("secret", "audience");

        final Map<String,String> decodedJSON = jwtVerifier.decodeAndParse(encodedJSON);

        assertEquals("json", decodedJSON.get("some"));
        assertEquals(null, decodedJSON.get("unexisting_property"));
        assertEquals("123", decodedJSON.get("number"));
    }


}
