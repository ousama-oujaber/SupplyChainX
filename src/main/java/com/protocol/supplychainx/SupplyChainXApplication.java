package com.protocol.supplychainx;

import com.protocol.supplychainx.common.enums.RoleUtilisateur;
import com.protocol.supplychainx.user.entity.User;
import com.protocol.supplychainx.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class SupplyChainXApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupplyChainXApplication.class, args);
    }

//    @Bean
    CommandLineRunner commandLineRunner(PasswordEncoder ctx, UserRepository  userRepository) {
        return args -> {
            User user = new User();
            user.setPassword("oussama");
            user.setEmail("oussama@oujaber.com");
            user.setRole(RoleUtilisateur.ADMIN);
            user.setLastName("admin");
            user.setFirstName("admin");
            user.setPassword(ctx.encode(user.getPassword()));
            userRepository.save(user);
        };
    }

}
