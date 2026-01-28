package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @JsonManagedReference
    @OneToMany(mappedBy = "shipper")
    private List<Delivery> deliveries;

    @JsonManagedReference
    @OneToMany(mappedBy = "user")
    private List<Report> reports;
}
