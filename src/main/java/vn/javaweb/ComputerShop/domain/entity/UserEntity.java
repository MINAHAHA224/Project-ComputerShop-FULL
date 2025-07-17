package vn.javaweb.ComputerShop.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated
@Entity
@Table(name = "users")
public class UserEntity implements Serializable, UserDetails  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long id;
    private String email;
    private String password;
    private String fullName;
    private String address;
    private String phone;
    private String avatar;


    // roleId
    // User many -> to one -> role
    @OneToMany(mappedBy = "user")
    private List<OrderEntity> orders;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    @OneToMany(mappedBy = "user")
    private List<CartEntity> cart;
    @OneToOne(mappedBy = "user")
    private AuthMethodEntity authMethodEntity;

    @OneToMany(mappedBy = "user")
    private List<UserOtpEntity>  userOtpEntities;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add( new SimpleGrantedAuthority("ROLE_" + this.role.getName().toUpperCase()));
        return authorityList;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
