# Keycloak Testing Guide

## 1. Generate Access Token
Run the following command to generate a new access token for user `ousama`:

```bash
curl -X POST http://localhost:8090/realms/supplychainx/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=supplychainx-backend" \
  -d "username=ousama" \
  -d "password=ousama" \
  -d "grant_type=password"
```

Copy the value of the `access_token` field from the JSON response.

## 2. Test API Endpoint
Use the generated token to access the protected `supply-orders` endpoint. Replace `YOUR_ACCESS_TOKEN_HERE` with the actual token string.

```bash
curl -X GET http://localhost:8080/api/procurement/supply-orders \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

### Expected Response
If successful, you should receive a JSON response with a list of supply orders (HTTP 200).
