package com.redflag.redflag.user.service;

import com.redflag.redflag.analysis.domain.User;
import com.redflag.redflag.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 새로운 사용자를 생성하고 UUID를 반환
     * UUID는 자동으로 생성됨 (GenerationType.UUID)
     */
    @Transactional
    public UUID createUser() {
        User user = User.builder().build();
        User savedUser = userRepository.save(user);
        
        log.info("새로운 사용자 생성: {}", savedUser.getId());
        return savedUser.getId();
    }

    /**
     * UUID로 사용자 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsUser(UUID userId) {
        return userRepository.existsById(userId);
    }

    /**
     * UUID로 사용자 조회
     */
    @Transactional(readOnly = true)
    public User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElse(null);
    }
}
