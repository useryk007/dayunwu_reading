package org.example.service.impl;

import org.example.Mapper.FollowsMapper;
import org.example.Mapper.UsersMapper;
import org.example.Pojo.Users;
import org.example.common.BusinessException;
import org.example.common.LoginFailCounter;
import org.example.service.NotesService;
import org.example.service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UsersServiceImpl implements UsersService {

    private static final Logger log = LoggerFactory.getLogger(UsersServiceImpl.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final UsersMapper usersMapper;
    private final NotesService notesService;
    private final FollowsMapper followsMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginFailCounter failCounter;

    public UsersServiceImpl(UsersMapper usersMapper,
                            NotesService notesService,
                            FollowsMapper followsMapper,
                            BCryptPasswordEncoder passwordEncoder,
                            LoginFailCounter failCounter) {
        this.usersMapper = usersMapper;
        this.notesService = notesService;
        this.followsMapper = followsMapper;
        this.passwordEncoder = passwordEncoder;
        this.failCounter = failCounter;
    }

    @Override
    public void register(Users users) {
        validateRegister(users);
        if (usersMapper.findByUsername(users.getUsername()) != null) {
            throw new BusinessException("该账户名已被注册");
        }
        if (usersMapper.findByPhone(users.getPhone()) != null) {
            throw new BusinessException("该手机号已被注册");
        }
        String raw = users.getPassword();
        users.setPassword(passwordEncoder.encode(raw));
        users.setPasswordPlain(raw);
        users.setRegisterTime(LocalDateTime.now());
        if (users.getRole() == null) users.setRole("USER");
        if (users.getStatus() == null) users.setStatus(1);
        usersMapper.insert(users);
        log.info("新用户注册: username={}, phone={}", users.getUsername(), users.getPhone());
    }

    @Override
    public Users login(String account, String password) {
        if (failCounter.isLocked(account)) {
            long minutes = failCounter.remainingLockMillis(account) / 60000;
            throw new BusinessException("账户已被锁定，请 " + minutes + " 分钟后再试");
        }

        Users user = usersMapper.findByAccount(account);
        if (user == null) {
            failCounter.recordFail(account);
            throw new BusinessException("账户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            failCounter.recordFail(account);
            int remaining = LoginFailCounter.MAX_FAIL_COUNT - failCounter.failCount(account);
            String tip = failCounter.isLocked(account)
                    ? "密码错误，账户已锁定"
                    : "密码错误，剩余尝试次数 " + remaining;
            throw new BusinessException(tip);
        }
        failCounter.clear(account);
        log.info("用户登录: username={}, role={}", user.getUsername(), user.getRole());
        return user;
    }

    @Override
    public Users getUserById(Long id) {
        return usersMapper.findById(id);
    }

    @Override
    public Users getUserByUsername(String username) {
        return usersMapper.findByUsername(username);
    }

    @Override
    public void update(Users users) {
        if (users.getUsername() != null) {
            Users exist = usersMapper.findByUsername(users.getUsername());
            if (exist != null && !exist.getId().equals(users.getId())) {
                throw new BusinessException("该账户名已被占用");
            }
        }
        if (users.getPhone() != null) {
            if (!PHONE_PATTERN.matcher(users.getPhone()).matches()) {
                throw new BusinessException("手机号格式不正确");
            }
            Users exist = usersMapper.findByPhone(users.getPhone());
            if (exist != null && !exist.getId().equals(users.getId())) {
                throw new BusinessException("该手机号已被占用");
            }
        }
        if (users.getPassword() != null) {
            String raw = users.getPassword();
            users.setPassword(passwordEncoder.encode(raw));
            users.setPasswordPlain(raw);
        }
        usersMapper.updateById(users);
    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        followsMapper.deleteByUser(username);
        notesService.deleteByUser(username);
        usersMapper.deleteById(id);
    }

    @Override
    public List<Users> listAll() {
        return usersMapper.findAll();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        usersMapper.updateStatus(id, status);
    }

    @Override
    public int countAll() {
        return usersMapper.countAll();
    }

    private void validateRegister(Users users) {
        if (users.getUsername() == null || users.getUsername().isBlank()) {
            throw new BusinessException("账户名不能为空");
        }
        if (users.getPhone() == null || users.getPhone().isBlank()) {
            throw new BusinessException("手机号不能为空");
        }
        if (!PHONE_PATTERN.matcher(users.getPhone()).matches()) {
            throw new BusinessException("手机号格式不正确");
        }
        if (users.getPassword() == null || users.getPassword().isBlank()) {
            throw new BusinessException("密码不能为空");
        }
    }
}