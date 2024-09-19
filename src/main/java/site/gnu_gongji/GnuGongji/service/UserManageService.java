package site.gnu_gongji.GnuGongji.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.gnu_gongji.GnuGongji.dto.UserCreateDto;
import site.gnu_gongji.GnuGongji.entity.Authority;
import site.gnu_gongji.GnuGongji.entity.User;
import site.gnu_gongji.GnuGongji.repository.UserManageRepository;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.OAuth2Provider;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2UserPrincipal;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.Role;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserManageService {

    private final UserManageRepository userManageRepository;

    public UserCreateDto createOAuth2User(OAuth2UserPrincipal oAuth2UserPrincipal, String refreshToken) {

        String userEmail = oAuth2UserPrincipal.getUserInfo().getEmail();
        String oAuth2Provider = oAuth2UserPrincipal.getUserInfo().getProvider().getRegistrationId();
        Timestamp createDate = new Timestamp(System.currentTimeMillis());

        if (this.isUserAlreadyExist(userEmail)) {
            return new UserCreateDto(null, null, true, null);
        }

        User user = User.builder()
                .userEmail(userEmail)
                .isOAuth2(true)
                .oauth2Provider(oAuth2Provider)
                .oauth2Id(oAuth2UserPrincipal.getUserInfo().getId())
                .refreshToken(refreshToken)
                .authorities(new HashSet<>())
                .createDate(createDate)
                .build();

        Authority authority = Authority.builder()
                .role(Role.PREFIX.getValue() + Role.USER.getValue())
                .build();

        user.getAuthorities().add(authority);

        authority.setUser(user);

        userManageRepository.createUser(user);

        log.info("[USER CREATED] email={}, provider={}", userEmail, oAuth2Provider);

        return new UserCreateDto(userEmail, oAuth2Provider, false, createDate);
    }

    public boolean isUserAlreadyExist(String userEmail) {

        Optional<User> userOptional = userManageRepository.findUserByEmail(userEmail);

        return userOptional.isPresent();
    }

    public boolean deleteOAuth2User(String oauth2Id, OAuth2Provider provider) {

        return userManageRepository.deleteUser(oauth2Id, provider.getRegistrationId());
    }

    public Optional<User> findOAuth2User(String oauth2Id, String oauth2Provider) {
        return userManageRepository.findUserByOauth2IdAndOAuth2Provider(oauth2Id, oauth2Provider);
    }

    public boolean updateRefreshToken(String oauth2Id, String oauth2Provider, String newRefreshToken) {
        return userManageRepository.updateRefreshToke(oauth2Id, oauth2Provider, newRefreshToken);
    }
}
