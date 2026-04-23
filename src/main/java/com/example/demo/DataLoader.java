package com.example.demo;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class DataLoader implements CommandLineRunner {
//    private final UserRepository userRepository;
//
//    public DataLoader(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        User user = new User();
//        user.setName("Test user");
//
//        userRepository.save(user);
//        System.out.println("User saved in H2");
//        System.out.println("Overall user in DB" + userRepository.count());
//
//        System.out.println("List of users");
//        userRepository.findAll().forEach(u -> System.out.println(" - " + u.getName()));
//    }
//}
