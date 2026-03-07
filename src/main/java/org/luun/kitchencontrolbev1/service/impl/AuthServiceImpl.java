package org.luun.kitchencontrolbev1.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.luun.kitchencontrolbev1.dto.request.AuthenticationRequest;
import org.luun.kitchencontrolbev1.dto.request.IntrospectRequest;
import org.luun.kitchencontrolbev1.dto.request.LoginRequest;
import org.luun.kitchencontrolbev1.dto.response.AuthenticationResponse;
import org.luun.kitchencontrolbev1.dto.response.IntrospectResponse;
import org.luun.kitchencontrolbev1.entity.User;
import org.luun.kitchencontrolbev1.repository.UserRepository;
import org.luun.kitchencontrolbev1.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${jwt.signerKey}")
    private String signerKey;
    @Autowired
    private UserRepository userRepository;

    @Override
    public User login(LoginRequest loginRequest) {
        // Find the user by username
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // IMPORTANT: Plain text password comparison (INSECURE!)
        // In a real app, you would use a password encoder to compare hashes.
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    @Override
    public AuthenticationResponse loginV2(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean authenticated = user.getPassword().equals(authenticationRequest.getPassword());

        if(!authenticated) {
            throw new RuntimeException("Invalid password");
        }

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(authenticated)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken();

        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier) && expiredTime.after(new Date());

        return IntrospectResponse.builder()
                .valid(verified)
                .build();
    }


    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("Kitchen Control BE")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("userId", user.getUserId())
                .claim("fullName", user.getFullName())
                .claim("roleName", user.getRole() != null ? user.getRole().getRoleName() : null)
                .claim("storeId", user.getStore() != null ? user.getStore().getStoreId() : null)
                .claim("storeName", user.getStore() != null ? user.getStore().getStoreName() : null)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader,payload);

        try{
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
