package dio.security.jwt.controller;

import dio.security.jwt.dtos.Login;
import dio.security.jwt.dtos.Sessao;
import dio.security.jwt.model.User;
import dio.security.jwt.repository.UserRepository;
import dio.security.jwt.security.JWTCreator;
import dio.security.jwt.security.JWTObject;
import dio.security.jwt.security.JWTProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
public class LoginController {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository repository;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Sessao logar(@RequestBody Login login) {

        User user = repository.findByUsername(login.getUsername());

        if (user == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        if (!encoder.matches(login.getPassword(), user.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject(user.getUsername()); // ✅ CRÍTICO
        jwtObject.setIssuedAt(new Date());
        jwtObject.setExpiration(
                new Date(System.currentTimeMillis() + JWTProperties.EXPIRATION)
        );
        jwtObject.setRoles(user.getRoles());

        Sessao sessao = new Sessao();
        sessao.setLogin(user.getUsername());
        sessao.setToken(
                JWTCreator.create(
                        JWTProperties.PREFIX,
                        JWTProperties.KEY,
                        jwtObject
                )
        );

        return sessao;
    }
}
