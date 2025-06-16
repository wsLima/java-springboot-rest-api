package br.com.wslima.javaspringbootrestapi.persistence.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
public class User {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    @Getter @Setter
    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    private UUID uuid;

    private String name;

    private String email;

    private String password;

}
