# Developer IDP – OAuth2 / OIDC

Identity Provider locale basato su **Spring Boot**, **Spring Security** e **Spring Authorization Server**, utilizzabile per simulare un Authorization Server OAuth2 / OpenID Connect durante lo sviluppo e il testing di applicazioni.

E' disponibile una console UI all'indirizzo:

```text
http://localhost:9080
```

> **Nota:** questo progetto è destinato esclusivamente a sviluppo e test. ***Non è adatto ad ambienti di produzione***.

---

## Stack tecnologico

* Java 21
* Spring Boot 4
* Spring Security 7
* Spring Authorization Server
* OAuth 2.0
* OpenID Connect (OIDC)
* Spring Data JPA
* H2 Database
* JWT
* RSA / JWKS

---

## Avvio

Avviare l'applicazione con Maven:

```bash
mvn spring-boot:run
```

oppure tramite il proprio IDE.

L'IdP viene avviato sulla porta:

```text
9080
```

Issuer:

```text
http://localhost:9080
```

---

# Endpoint principali

## OIDC Discovery

L'endpoint di discovery è:

```text
http://localhost:9080/.well-known/openid-configuration
```

Restituisce la configurazione OIDC dell'Identity Provider, inclusi gli endpoint OAuth2/OIDC supportati.

---

## Authorization Endpoint

```text
http://localhost:9080/oauth2/authorize
```

È l'endpoint utilizzato dal client per avviare l'Authorization Code Flow.

---

## Token Endpoint

```text
http://localhost:9080/oauth2/token
```

Utilizzato per ottenere gli access token, refresh token e ID token.

---

## JWKS Endpoint

```text
http://localhost:9080/oauth2/jwks
```

Espone le chiavi pubbliche RSA utilizzate per verificare la firma dei JWT emessi dall'IdP.

---

## UserInfo Endpoint

```text
http://localhost:9080/userinfo
```

Endpoint OIDC utilizzabile per ottenere informazioni sull'utente autenticato tramite access token.

---

## Logout Endpoint

```text
http://localhost:9080/connect/logout
```

Endpoint per il logout OIDC.

Può essere utilizzato insieme a:

```text
id_token_hint
client_id
post_logout_redirect_uri
```

---

# OAuth2 Client

Il progetto contiene un client OAuth2/OIDC di test configurato con:

```text
Client ID:     oidc-client
Client Secret: secret
```

Metodo di autenticazione:

```text
client_secret_basic
```

Grant supportati:

* Authorization Code
* Refresh Token
* Client Credentials

Per l'Authorization Code Flow è richiesto **PKCE**.

---

# Redirect URI

Sono configurati due redirect URI:

### Applicazione locale

```text
http://localhost:8080/login/oauth2/code/oidc-client
```

Questo URI è pensato per un'applicazione Spring Boot configurata come OAuth2 Client.

### Postman

```text
https://oauth.pstmn.io/v1/callback
```

Questo URI permette di utilizzare Postman per effettuare test manuali dell'Authorization Code Flow.

---

# Scope

Gli scope supportati dal client sono:

```text
openid
profile
email
address
phone
```

Lo scope:

```text
openid
```

abilita il comportamento OpenID Connect e permette l'emissione dell'ID token.

---

# Authorization Code + PKCE

Il flow principale da utilizzare per simulare un'autenticazione utente è:

```text
┌──────────────┐
│ OAuth2/OIDC  │
│    Client    │
└──────┬───────┘
       │
       │ Authorization Request
       ▼
┌──────────────┐
│     IdP      │
│  localhost   │
│    :9080     │
└──────┬───────┘
       │
       │ Login
       ▼
┌──────────────┐
│     User     │
└──────┬───────┘
       │
       │ Authorization Code
       ▼
┌──────────────┐
│ OAuth2/OIDC  │
│    Client    │
└──────┬───────┘
       │
       │ POST /oauth2/token
       │ + code_verifier
       ▼
┌──────────────┐
│     IdP      │
└──────┬───────┘
       │
       │ access_token
       │ refresh_token
       │ id_token
       ▼
┌──────────────┐
│ OAuth2/OIDC  │
│    Client    │
└──────────────┘
```

L'Authorization Code Flow utilizza:

```text
response_type=code
code_challenge
code_challenge_method=S256
```

Il `code_verifier` viene successivamente utilizzato dal client nella richiesta al token endpoint.

---

# Token

## Access Token

L'access token viene utilizzato dal client per accedere alle API protette del Resource Server.

Configurazione attuale:

```text
TTL: 5 minuti
```

Esempio:

```http
Authorization: Bearer <access_token>
```

---

## ID Token

L'ID token è un JWT OIDC contenente informazioni sull'utente autenticato.

Tra i claim presenti:

```text
sub
name
given_name
family_name
email
email_verified
address
phone_number
preferred_username
iss
aud
iat
exp
sid
```

L'ID token viene emesso durante l'Authorization Code Flow quando viene richiesto lo scope:

```text
openid
```

---

## Refresh Token

Il refresh token permette al client di ottenere un nuovo access token senza richiedere nuovamente l'autenticazione dell'utente.

Configurazione attuale:

```text
TTL: 30 giorni
```

È inoltre configurata la rotazione dei refresh token:

```text
reuseRefreshTokens(false)
```

---

# Utenti di test

Gli utenti di test vengono memorizzati nel database H2 e sono consultabili dalla console UI.

> Le credenziali sono registrate in chiaro e sono da considerarsi solo *a scopo di test e sviluppo*.

---

# JWT e JWKS

All'avvio, l'IdP carica la coppia di chiavi RSA da un keystore dell'applicazione.

La chiave pubblica viene esposta tramite:

```text
http://localhost:9080/oauth2/jwks
```

I token JWT emessi dall'IdP possono quindi essere verificati utilizzando la chiave pubblica corrispondente.

---

# Utilizzo con un'applicazione Spring Boot

Un'applicazione Spring Boot che vuole utilizzare questo IdP come OAuth2/OIDC Provider può configurare:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          oidc-client:
            client-id: oidc-client
            client-secret: secret
            scope:
              - openid
              - profile
              - email
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"

        provider:
          oidc-client:
            issuer-uri: http://localhost:9080
```

Con questa configurazione Spring Security può ottenere automaticamente la configurazione dell'IdP tramite:

```text
http://localhost:9080/.well-known/openid-configuration
```

---

# Utilizzo con Postman

Per effettuare un test manuale:

1. Creare una richiesta OAuth 2.0 in Postman.
2. Utilizzare il grant type `Authorization Code`.
3. Configurare:

```text
Auth URL:
http://localhost:9080/oauth2/authorize

Access Token URL:
http://localhost:9080/oauth2/token

Client ID:
oidc-client

Client Secret:
secret

Callback URL:
https://oauth.pstmn.io/v1/callback
```

4. Abilitare PKCE.
5. Utilizzare:

```text
Code Challenge Method:
S256
```

6. Richiedere gli scope:

```text
openid profile email
```

7. Effettuare il login tramite l'IdP.
8. Autorizzare il client.
9. Postman riceverà l'authorization code.
10. Postman effettuerà lo scambio del code tramite `/oauth2/token`.

La risposta dovrebbe contenere:

```json
{
  "access_token": "...",
  "refresh_token": "...",
  "scope": "openid profile email",
  "id_token": "...",
  "token_type": "Bearer",
  "expires_in": 300
}
```

*Nota*  
È possibile effettuare il login selezionando semplicemente l'utente, senza inserire la password, impostando a `true` il flag `free-login` nella configurazione dell'Authorization Server.

---

# Logout OIDC

Il logout può essere avviato tramite:

```text
http://localhost:9080/connect/logout
```

Utilizzando, quando necessario:

```text
id_token_hint
client_id
post_logout_redirect_uri
```

Esempio:

```text
http://localhost:9080/connect/logout
    ?id_token_hint=<ID_TOKEN>
    &client_id=oidc-client
    &post_logout_redirect_uri=http://localhost:8080/
```

Il `post_logout_redirect_uri` deve essere preventivamente registrato per il client.

---

# Struttura concettuale

Il progetto può essere utilizzato per simulare localmente una architettura composta da:

```text
                    ┌──────────────────────┐
                    │       Browser        │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │   OAuth2 / OIDC      │
                    │       Client         │
                    │   localhost:8080     │
                    └──────────┬───────────┘
                               │
                     OAuth2 / OIDC
                               │
                               ▼
                    ┌──────────────────────┐
                    │   Developer IDP     │
                    │   localhost:9080     │
                    │                      │
                    │ Spring Authorization │
                    │      Server          │
                    └──────────┬───────────┘
                               │
                               │ JWT
                               ▼
                    ┌──────────────────────┐
                    │   Resource Server    │
                    │                      │
                    │       REST API       │
                    └──────────────────────┘
```

Questo permette di sviluppare e testare applicazioni OAuth2/OIDC senza dipendere da un Identity Provider esterno.

---

# Finalità del progetto

Il progetto è pensato come **Identity Provider locale riutilizzabile** per:

* Proof of Concept OAuth2;
* Proof of Concept OpenID Connect;
* sviluppo di OAuth2 Client;
* sviluppo di Resource Server;
* test di Authorization Code Flow;
* test PKCE;
* test Client Credentials;
* test Refresh Token;
* test JWT;
* test JWKS;
* test OIDC Discovery;
* test OIDC Logout;
* integrazione con Spring Security;
* test manuali tramite Postman.

Non deve essere utilizzato come Identity Provider di produzione.

---

# Test

Il progetto può essere testato a più livelli:

### Unit test

Test della logica applicativa proprietaria, ad esempio:

* `CustomUserDetailsService`;
* gestione degli utenti;
* `OAuth2TokenCustomizer`;
* generazione dei claim custom.

### Integration test

Test degli endpoint OAuth2/OIDC e dei principali flow:

* OIDC Discovery;
* JWKS;
* Authorization Code + PKCE;
* Token endpoint;
* ID Token;
* Access Token;
* Refresh Token;
* Client Credentials;
* gestione degli errori OAuth2;
* Scope;
* OIDC Logout.

L'obiettivo è verificare il comportamento dell'IdP come Authorization Server/OIDC Provider reale, evitando di testare internamente il comportamento di Spring Security.

---

## Configurazione principale

Le principali proprietà utilizzate dal POC sono nei file seguenti files.

File `config-idp.yaml` per la configurazione dell'Authorization Server:
```yaml
authorization-server:
  issuer-url: http://localhost:${server.port}
  supported-authentication-methods: [none, client_secret_basic]
  supported-scopes: [openid, profile, email, address, phone]
  supported-grant-types: [authorization_code, refresh_token, client_credentials]
  supported-roles: [ADMIN, USER]
  supported-groups: [administrators, developers, managers, users]
  free-login: true

jwt:
  key-store: classpath:idp-keystore.p12
  key-store-type: PKCS12
  key-store-password: changeit
  key-alias: idp-signing
  key-password: changeit

claims:
  always: [roles, groups]

  scope-mappings:
    profile: [name, given_name, family_name, preferred_username]
    email: [email, email_verified]
    address: [address]
    phone: [phone_number]

  claim-mappings:
    roles: roles
    groups: groups
    name: fullName
    given_name: firstName
    family_name: lastName
    preferred_username: username
    email: email
    email_verified: emailVerified
    address: address
    phone_number: phoneNumber
```

File `config-oauth2-clients.yml` per la configurazione dei client Oauth2:
```yaml
oauth2-clients:
  - client-id: oidc-client
    client-secret: secret
    description: client test di default
    client-url: http://localhost:8080
    client-authentication-methods: [client_secret_basic]
    redirect-uris:
      - http://localhost:8080/login/oauth2/code/oidc-client
      - https://oauth.pstmn.io/v1/callback
    post-logout-redirect-uris:
      - http://localhost:8080/
    scopes: [openid, profile, email, address, phone]
    authorization-grant-types: [authorization_code, refresh_token, client_credentials]
    authorization-consent: true
    require-proof-key: true
    access-token-ttl: 5m
    refresh-token-ttl: 30d
    authorization-code-ttl: 5m
    reuse-refresh-tokens: false

  - client-id: test-client
    client-secret: test-secret
    description: secondo client test
    client-url: http://localhost:8081
    client-authentication-methods: [client_secret_basic]
    redirect-uris:
      - http://localhost:8081/login/oauth2/code/test-client
    post-logout-redirect-uris:
      - http://localhost:8081/
    scopes: [openid, profile, email]
    authorization-grant-types: [authorization_code, client_credentials]
    authorization-consent: true
    require-proof-key: false
    access-token-ttl: 5m
    refresh-token-ttl: 30d
    authorization-code-ttl: 5m
    reuse-refresh-tokens: false

  - client-id: public-client
    client-secret:
    client-url: http://localhost:8082
    description: client test pubblico
    client-authentication-methods: [none]
    redirect-uris:
      - http://localhost:8082/login/oauth2/code/public-client
    post-logout-redirect-uris:
      - http://localhost:8082/
    scopes: [openid, profile, email]
    authorization-grant-types: [authorization_code]
    authorization-consent: true
    require-proof-key: true
    access-token-ttl: 5m
    refresh-token-ttl: 30d
    authorization-code-ttl: 5m
    reuse-refresh-tokens: false
```

Gli utenti di test sono configurati nel file `config-users.yml`:
```yaml
users:
  - username: test
    password: password
    first-name: Mario
    last-name: Rossi
    email: mario.rossi@example.com
    address: Via Roma 1, Bologna
    phone-number: "+39 333 1234567"
    roles: [USER]
    groups: [users]

  - username: test2
    password: password
    first-name: John
    last-name: Doe
    email: john.doe@example.com
    address: 123 Main Street
    phone-number: "+1 555 1234567"
    roles: [ADMIN, USER]
    groups: [administrators, users]
```

Questi valori sono destinati esclusivamente all'ambiente locale di sviluppo e testing.
