package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "stores")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Integer storeId;

    @Column(name = "store_name", length = 255)
    private String storeName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "phone", length = 255)
    private String phone;

    @JsonManagedReference
    @OneToMany(mappedBy = "store")
    private List<User> users;

    @JsonManagedReference
    @OneToMany(mappedBy = "store")
    private List<Order> orders;

    @JsonManagedReference
    @OneToMany(mappedBy = "store")
    private List<QualityFeedback> qualityFeedbacks;
}