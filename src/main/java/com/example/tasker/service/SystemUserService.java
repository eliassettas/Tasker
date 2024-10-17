package com.example.tasker.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.tasker.exception.CustomException;
import com.example.tasker.integration.EmailSender;
import com.example.tasker.model.dto.RegistrationRequest;
import com.example.tasker.model.dto.TypeDTO;
import com.example.tasker.model.dto.UserDataDTO;
import com.example.tasker.model.enums.SystemAuthorityName;
import com.example.tasker.model.persistence.JobTitle;
import com.example.tasker.model.persistence.RegistrationToken;
import com.example.tasker.model.persistence.SystemAuthority;
import com.example.tasker.model.persistence.SystemUser;
import com.example.tasker.model.persistence.UserProfile;
import com.example.tasker.repository.SystemUserRepository;
import com.example.tasker.repository.UserProfileRepository;
import com.example.tasker.util.MailValidator;
import com.example.tasker.util.SecurityUtils;

@Service
public class SystemUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemUserService.class);

    protected static final String USER_DETAILS_BY_ID_CACHE = "user_details_by_id";

    @Value("${constants.account-activation-url}")
    private String accountActivationUrl;

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RegistrationTokenService registrationTokenService;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private JobTitleService jobTitleService;

    @Autowired
    private FileSystemService fileSystemService;

    @Autowired
    private CacheManager cacheManager;

    @Cacheable(key = "#id", value = USER_DETAILS_BY_ID_CACHE)
    public SystemUser getUserById(Integer id) throws CustomException {
        return systemUserRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Cacheable(key = "#username", value = UserDetailsServiceImpl.USER_DETAILS_BY_USERNAME_CACHE)
    public SystemUser getUserByUsername(String username) throws CustomException {
        return systemUserRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<UserDataDTO> getAllUsers() {
        List<Object[]> usersMinimalInfo = systemUserRepository.getUsersMinimalInfo();
        List<UserDataDTO> userDataDTOs = new ArrayList<>();
        usersMinimalInfo.forEach(user -> {
            UserDataDTO userDataDTO = new UserDataDTO();
            userDataDTO.setUserId((Integer) user[0]);
            userDataDTO.setEmail((String) user[1]);
            userDataDTO.setFirstName((String) user[2]);
            userDataDTO.setLastName((String) user[3]);
            userDataDTOs.add(userDataDTO);
        });
        return userDataDTOs;
    }

    public UserDataDTO getUserData(Integer userId) throws CustomException {
        SystemUser systemUser = getUserById(userId);

        UserDataDTO userDataDTO = UserDataDTO.fromObject(systemUser);
        String imageURL = systemUser.getProfile().getImageURL();
        if (imageURL != null) {
            byte[] image = fileSystemService.loadUserImage(imageURL);
            userDataDTO.setImage(image);
        }

        return userDataDTO;
    }

    @Transactional(rollbackFor = CustomException.class)
    public void registerUser(RegistrationRequest registrationRequest) throws CustomException {
        LOGGER.info("Received registration request for mail: {} and username: {}", registrationRequest.getEmail(), registrationRequest.getUsername());
        validateUserFields(registrationRequest);

        SystemUser systemUser = processRegistrationRequest(registrationRequest);

        LOGGER.info("Creating registration token for user id: {}", systemUser.getId());
        RegistrationToken registrationToken = registrationTokenService.createToken(systemUser);
        emailSender.sendMailToBroker(systemUser.getEmail(), "Confirm your registration",
                String.format(accountActivationUrl, registrationToken.getToken()));
    }

    private static void validateUserFields(RegistrationRequest registrationRequest) throws CustomException {
        if (registrationRequest.getUsername() == null || registrationRequest.getUsername().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Provided username cannot not be empty");
        }
        if (registrationRequest.getEmail() == null || registrationRequest.getEmail().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Provided email cannot be empty");
        }
        if (registrationRequest.getPassword() == null || registrationRequest.getPassword().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Provided password cannot be empty");
        }
        if (!MailValidator.isMail(registrationRequest.getEmail())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Provided e-mail is invalid");
        }
    }

    private SystemUser processRegistrationRequest(RegistrationRequest registrationRequest) throws CustomException {
        SystemUser existingUserByUsername = systemUserRepository.findByUsername(registrationRequest.getUsername()).orElse(null);
        SystemUser existingUserByEmail = systemUserRepository.findByEmail(registrationRequest.getEmail()).orElse(null);

        if (existingUserByEmail != null && existingUserByEmail.isEnabled()) {
            throw new CustomException(HttpStatus.CONFLICT, "Email is already used by other account");
        }
        if (existingUserByUsername != null && !existingUserByUsername.getEmail().equals(registrationRequest.getEmail())) {
            throw new CustomException(HttpStatus.CONFLICT, "Username is not available");
        }

        if (existingUserByEmail != null) {
            if (!existingUserByEmail.getUsername().equals(registrationRequest.getUsername())) {
                LOGGER.info("Changing username of non activated user with id: {}", existingUserByEmail.getId());
                existingUserByEmail.setUsername(registrationRequest.getUsername());
                systemUserRepository.save(existingUserByEmail);
            }
            return existingUserByEmail;
        } else {
            LOGGER.info("Creating new system user with username: {}", registrationRequest.getUsername());
            SystemUser systemUser = buildNewUser(registrationRequest);
            SystemUser savedSystemUser = systemUserRepository.save(systemUser);

            LOGGER.info("Creating user profile for user id: {}", savedSystemUser.getId());
            UserProfile userProfile = new UserProfile();
            userProfile.setSystemUser(savedSystemUser);
            userProfileRepository.save(userProfile);
            return savedSystemUser;
        }
    }

    private SystemUser buildNewUser(RegistrationRequest registrationRequest) {
        SystemUser systemUser = new SystemUser();
        systemUser.setUsername(registrationRequest.getUsername());
        systemUser.setEmail(registrationRequest.getEmail());
        String encodedPassword = passwordEncoder.encode(registrationRequest.getPassword());
        systemUser.setPassword(encodedPassword);
        for (SystemAuthorityName systemAuthorityName : SystemAuthorityName.values()) {
            SystemAuthority systemAuthority = new SystemAuthority();
            systemAuthority.setSystemUser(systemUser);
            systemAuthority.setAuthority(systemAuthorityName.getValue());
            systemUser.getAuthorities().add(systemAuthority);
        }
        return systemUser;
    }

    @Transactional(rollbackFor = CustomException.class)
    public void activateUser(String token) throws CustomException {
        LOGGER.info("Received validation request for registration token: {}", token);
        RegistrationToken registrationToken = registrationTokenService.getByToken(token);

        boolean isExpired = LocalDateTime.now().isAfter(registrationToken.getExpirationDate());
        if (isExpired) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Registration token has expired");
        }

        SystemUser systemUser = registrationToken.getSystemUser();
        systemUser.setEnabled(true);
        LOGGER.info("Activating user: {}", systemUser.getUsername());
        systemUserRepository.save(systemUser);
    }

    @Transactional(rollbackFor = CustomException.class)
    public UserDataDTO updateUserProfile(UserDataDTO userDataDTO) throws CustomException {
        doBasicValidationChecks(userDataDTO);
        SecurityUtils.validateUser(userDataDTO.getUserId());
        SystemUser systemUser = getUserById(userDataDTO.getUserId());

        clearUserCache(systemUser);

        UserProfile userProfile = systemUser.getProfile();
        LocalDateTime lastUpdateDate = userDataDTO.getLastUpdateDate();
        if (lastUpdateDate == null || userProfile.getLastUpdateDate().isAfter(userDataDTO.getLastUpdateDate())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "User data are out-dated");
        }

        LOGGER.info("Updating user profile with id: {}", userProfile.getId());
        JobTitle jobTitle = userDataDTO.getJobTitle() != null
                ? jobTitleService.getJobTitleByName(userDataDTO.getJobTitle().getName())
                : null;
        populateUserProfile(userProfile, userDataDTO, jobTitle);
        userProfileRepository.saveAndFlush(userProfile);

        return UserDataDTO.fromObject(systemUser);
    }

    private void clearUserCache(SystemUser systemUser) {
        Cache userDetailsIdCache = cacheManager.getCache(USER_DETAILS_BY_ID_CACHE);
        if (userDetailsIdCache != null) {
            userDetailsIdCache.evictIfPresent(systemUser.getUsername());
        }
        Cache userDetailsUsernameCache = cacheManager.getCache(UserDetailsServiceImpl.USER_DETAILS_BY_USERNAME_CACHE);
        if (userDetailsUsernameCache != null) {
            userDetailsUsernameCache.evictIfPresent(systemUser.getUsername());
        }
    }

    private void doBasicValidationChecks(UserDataDTO userDataDTO) throws CustomException {
        String firstName = userDataDTO.getFirstName();
        if (!StringUtils.hasText(firstName)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Provided first name is empty");
        }

        String lastName = userDataDTO.getLastName();
        if (!StringUtils.hasText(lastName)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Provided last name is empty");
        }

        TypeDTO jobTitle = userDataDTO.getJobTitle();
        if (jobTitle != null && !StringUtils.hasText(jobTitle.getName())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Provided job title is empty");
        }
    }

    private void populateUserProfile(UserProfile userProfile, UserDataDTO userDataDTO, JobTitle jobTitle) {
        userProfile.setFirstName(userDataDTO.getFirstName());
        userProfile.setLastName(userDataDTO.getLastName());
        userProfile.setLocation(userDataDTO.getLocation());
        userProfile.setPhone(userDataDTO.getPhone());
        if (jobTitle != null) {
            userProfile.setJobTitle(jobTitle);
        }
    }

    public byte[] getUserImage(Integer userId) throws CustomException {
        SystemUser systemUser = getUserById(userId);

        String imageURL = systemUser.getProfile().getImageURL();
        if (imageURL == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, "User has no profile image");
        }

        return fileSystemService.loadUserImageBase64(imageURL);
    }

    @Transactional(rollbackFor = CustomException.class)
    public void updateUserImage(Integer userId, MultipartFile image) throws CustomException {
        SecurityUtils.validateUser(userId);
        SystemUser systemUser = getUserById(userId);

        LOGGER.info("Updating image of user with id: {}", systemUser.getId());
        String imageUrl = fileSystemService.storeUserImage(systemUser.getId(), image);
        UserProfile userProfile = systemUser.getProfile();
        userProfile.setImageURL(imageUrl);

        userProfileRepository.save(userProfile);
    }
}
