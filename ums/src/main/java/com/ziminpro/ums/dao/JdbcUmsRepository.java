package com.ziminpro.ums.dao;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ziminpro.ums.dtos.Constants;
import com.ziminpro.ums.dtos.LastSession;
import com.ziminpro.ums.dtos.Roles;
import com.ziminpro.ums.dtos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUmsRepository implements UmsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<UUID, User> findAllUsers() {
        Map<UUID, User> users = new HashMap<>();

        List<Object> oUsers = jdbcTemplate.query(Constants.GET_ALL_USERS,
                (rs, rowNum) -> new User(
                        DaoHelper.bytesArrayToUuid(rs.getBytes("users.id")),
                        rs.getString("users.name"),
                        rs.getString("users.email"),
                        rs.getString("users.password"),
                        rs.getInt("users.created"),
                        Arrays.asList(new Roles(
                                DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                                rs.getString("roles.name"),
                                rs.getString("roles.description")
                        )),
                        new LastSession(
                                rs.getInt("last_visit.in"),
                                rs.getInt("last_visit.out")
                        )
                ));

        for (Object oUser : oUsers) {
            User u = (User) oUser;
            if (!users.containsKey(u.getId())) {
                User user = new User();
                user.setId(u.getId());
                user.setName(u.getName());
                user.setEmail(u.getEmail());
                user.setPassword(u.getPassword());
                user.setCreated(u.getCreated());
                user.setLastSession(u.getLastSession());
                users.put(u.getId(), user);
            }
            users.get(u.getId()).addRole(u.getRoles().get(0));
        }
        return users;
    }

    @Override
    public User findUserByID(UUID userId) {
        User user = new User();
        List<Object> users = jdbcTemplate.query(Constants.GET_USER_BY_ID_FULL,
                (rs, rowNum) -> new User(
                        DaoHelper.bytesArrayToUuid(rs.getBytes("users.id")),
                        rs.getString("users.name"),
                        rs.getString("users.email"),
                        rs.getString("users.password"),
                        rs.getInt("users.created"),
                        Arrays.asList(new Roles(
                                DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                                rs.getString("roles.name"),
                                rs.getString("roles.description")
                        )),
                        new LastSession(
                                rs.getInt("last_visit.in"),
                                rs.getInt("last_visit.out")
                        )
                ),
                userId.toString());

        for (Object oUser : users) {
            User u = (User) oUser;
            if (user.getId() == null) {
                user.setId(u.getId());
                user.setName(u.getName());
                user.setEmail(u.getEmail());
                user.setPassword(u.getPassword());
                user.setCreated(u.getCreated());
                user.setLastSession(u.getLastSession());
            }
            user.addRole(u.getRoles().get(0));
        }
        return user;
    }

    @Override
    public User findUserByEmail(String email) {
        User user = new User();

        List<Object> users = jdbcTemplate.query(
                "SELECT users.id AS `users.id`, users.name AS `users.name`, users.email AS `users.email`, " +
                        "users.password AS `users.password`, users.created AS `users.created`, " +
                        "roles.id AS `roles.id`, roles.name AS `roles.name`, roles.description AS `roles.description`, " +
                        "last_visit.in AS `last_visit.in`, last_visit.out AS `last_visit.out` " +
                        "FROM users " +
                        "LEFT JOIN users_has_roles ON users.id = users_has_roles.users_id " +
                        "LEFT JOIN roles ON users_has_roles.roles_id = roles.id " +
                        "LEFT JOIN last_visit ON users.last_visit_id = last_visit.id " +
                        "WHERE users.email = ?",
                (rs, rowNum) -> new User(
                        DaoHelper.bytesArrayToUuid(rs.getBytes("users.id")),
                        rs.getString("users.name"),
                        rs.getString("users.email"),
                        rs.getString("users.password"),
                        rs.getInt("users.created"),
                        Arrays.asList(new Roles(
                                DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                                rs.getString("roles.name"),
                                rs.getString("roles.description")
                        )),
                        new LastSession(
                                rs.getInt("last_visit.in"),
                                rs.getInt("last_visit.out")
                        )
                ),
                email
        );

        for (Object oUser : users) {
            User u = (User) oUser;
            if (user.getId() == null) {
                user.setId(u.getId());
                user.setName(u.getName());
                user.setEmail(u.getEmail());
                user.setPassword(u.getPassword());
                user.setCreated(u.getCreated());
                user.setLastSession(u.getLastSession());
            }
            user.addRole(u.getRoles().get(0));
        }

        return user;
    }

    @Override
    public UUID createUser(User user) {
        long timestamp = Instant.now().getEpochSecond();
        Map<String, Roles> roles = this.findAllRoles();
        UUID userId = UUID.randomUUID();

        try {
            jdbcTemplate.update(Constants.CREATE_USER,
                    userId.toString(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    timestamp,
                    null);

            for (Roles role : user.getRoles()) {
                jdbcTemplate.update(Constants.ASSIGN_ROLE,
                        userId.toString(),
                        roles.get(role.getRole()).getRoleId().toString());
            }
        } catch (Exception e) {
            return null;
        }

        return userId;
    }

    @Override
    public int deleteUser(UUID userId) {
        return jdbcTemplate.update(Constants.DELETE_USER, userId.toString());
    }

    @Override
    public Map<String, Roles> findAllRoles() {
        Map<String, Roles> roles = new HashMap<>();
        jdbcTemplate.query(Constants.GET_ALL_ROLES, rs -> {
            Roles role = new Roles(
                    DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                    rs.getString("roles.name"),
                    rs.getString("roles.description"));
            roles.put(rs.getString("roles.name"), role);
        });
        return roles;
    }
}