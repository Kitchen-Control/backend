package org.luun.kitchencontrolbev1.service;

import com.nimbusds.jose.JOSEException;
import org.luun.kitchencontrolbev1.dto.request.AuthenticationRequest;
import org.luun.kitchencontrolbev1.dto.request.IntrospectRequest;
import org.luun.kitchencontrolbev1.dto.request.LoginRequest;
import org.luun.kitchencontrolbev1.dto.response.AuthenticationResponse;
import org.luun.kitchencontrolbev1.dto.response.IntrospectResponse;
import org.luun.kitchencontrolbev1.entity.User;

import java.text.ParseException;

public interface AuthService {
    User login(LoginRequest loginRequest);
    AuthenticationResponse loginV2(AuthenticationRequest authenticationRequest);
    IntrospectResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException;
}
