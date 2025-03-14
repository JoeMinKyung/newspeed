package com.example.springbasicnewspeed.domain.auth.service;

import com.example.springbasicnewspeed.common.exception.*;
import com.example.springbasicnewspeed.config.JwtUtil;
import com.example.springbasicnewspeed.config.PasswordEncoder;
import com.example.springbasicnewspeed.domain.auth.dto.SigninRequest;
import com.example.springbasicnewspeed.domain.auth.dto.SigninResponse;
import com.example.springbasicnewspeed.domain.auth.dto.SignupRequest;
import com.example.springbasicnewspeed.domain.user.entity.User;
import com.example.springbasicnewspeed.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.existsByUserName(request.getUserName())) {
            throw new UserNameAlreadyExistsException("이미 존재하는 닉네임입니다.");
        }

        if (!Objects.equals(request.getPassword(), request.getPasswordCheck())) {
            throw new PasswordMismatchException("비밀번호와 비밀번호 확인이 같지 않습니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getEmail(), encodedPassword, request.getUserName());

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public SigninResponse signin(SigninRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new EmailNotFoundException("이메일이 존재하지 않습니다.")
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException("잘못된 비밀번호입니다.");
        }

        String bearerJwt = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserName());
        return new SigninResponse(bearerJwt);
    }
}
