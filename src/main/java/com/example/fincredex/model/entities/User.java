package com.example.fincredex.model.entities;

import com.example.fincredex.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ColumnDefault("USER")
    @Column(name = "role", nullable = false, length = 80)
    private Role role;

    @Size(max = 30)
    @NotNull
    @Column(name = "username", nullable = false, length = 30)
    private String username;

    @Size(max = 80)
    @Column(name = "password", length = 80)
    private String password;

    @Size(max = 90)
    @Column(name = "email", length = 90)
    private String email;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created", nullable = false)
    private LocalDateTime created = LocalDateTime.now();

    @OneToMany(mappedBy = "owner")
    private List<Company> companies = new ArrayList<>();


}