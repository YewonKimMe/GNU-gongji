package site.gnu_gongji.GnuGongji.entity;


import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email")
    private String userEmail;

    @Column(name = "is_oauth2")
    private boolean isOAuth2;

    @Column(name = "oauth2_provider")
    private String oauth2Provider;

    @Column(name = "oauth2_id")
    private String oauth2Id;

    @Column(name = "refresh_token", length = 1500)
    private String refreshToken;

    @Column(name = "create_date")
    private Timestamp createDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Authority> authorities = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserSub> subList = new LinkedHashSet<>();

}