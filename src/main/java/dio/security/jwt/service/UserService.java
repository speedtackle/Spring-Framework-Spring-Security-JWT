package dio.security.jwt.service;

import dio.security.jwt.model.User;
import dio.security.jwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public void createUser(User user){
        String pass = user.getPassword();
        //criptografando senha antes de salvar o user no db
        user.setPassword(passwordEncoder.encode(pass));
        repository.save(user);
    }
}
