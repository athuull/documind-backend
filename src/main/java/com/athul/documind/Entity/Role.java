package com.athul.documind.Entity;

import com.athul.documind.Enum.RoleType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JsonProperty("roleName")
    @Column(unique = true, nullable = false)
    private RoleType name;

    private String description;

    @Version
    private Long version;

    // Explicit getter (in case Lombok fails)
    public RoleType getName() {
        return name;
    }
}