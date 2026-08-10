package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository userRepository;



}
