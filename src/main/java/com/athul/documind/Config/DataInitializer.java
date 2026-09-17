package com.athul.documind.Config;

import com.athul.documind.Entity.Role;
import com.athul.documind.Enum.RoleType;
import com.athul.documind.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        if (roleRepository.count() == 0) {

            Role admin = new Role();
            admin.setName(RoleType.ADMIN);

            Role user = new Role();
            user.setName(RoleType.USER);

            roleRepository.save(admin);
            roleRepository.save(user);

            System.out.println("Default roles created.");
        }
    }
}