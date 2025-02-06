package Backend.Board.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import Backend.Board.model.User;
import Backend.Board.repository.UserRepository;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers(@RequestParam(required = false) Long id) {
        if (id != null) {
            return userRepository.findById(id).map(List::of).orElseThrow(() -> new RuntimeException("User not found"));
        }
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @DeleteMapping
    public void deleteUser(@RequestParam Long id) {
        userRepository.deleteById(id);
    }
}